package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ForgotPasswordForm;
import com.epam.rd.autocode.spring.project.dto.LoginForm;
import com.epam.rd.autocode.spring.project.dto.ResetPasswordForm;
import com.epam.rd.autocode.spring.project.exception.EmailAuthenticationException;
import com.epam.rd.autocode.spring.project.exception.EmailSendingException;
import com.epam.rd.autocode.spring.project.exception.EmailTimeoutException;
import com.epam.rd.autocode.spring.project.exception.InvalidEmailAddressException;
import com.epam.rd.autocode.spring.project.security.JwtTokenProvider;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientService clientService;
    private final EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MS = 10_000L;

    private static final Map<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          ClientService clientService, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.clientService = clientService;
        this.emailService = emailService;
    }

    private static class LoginAttemptInfo {
        int attempts;
        long lastFailTime;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        log.debug("Rendering login page");
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model) {

        String email = loginForm.getEmail();
        if (isBlocked(email)) {
            log.warn("Login blocked for {} due to too many failed attempts", email);
            model.addAttribute("errorMessage", "login.error.blocked.to.many.attempts");
            return "login";
        }

        if (bindingResult.hasErrors()) {
            log.debug("Validation errors during login: {}", bindingResult.getAllErrors());
            return "login";
        }

        try {
            Authentication authentication = authenticationManager.authenticate(loginForm.toAuthenticationToken());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            jwtCookie.setSecure(true);
            response.addCookie(jwtCookie);

            Cookie refreshCookie = new Cookie("JWT_REFRESH", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setSecure(true);
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshCookie);

            loginAttempts.remove(email);
            log.info("User '{}' successfully authenticated", userDetails.getUsername());
            return "redirect:/";

        } catch (LockedException e) {
            model.addAttribute("errorMessage", "login.error.blocked");
            return "login";
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email '{}': {}", loginForm.getEmail(), ex.getMessage());
            recordFailedAttempt(email);
            bindingResult.reject("login.error.invalid");
            return "login";
        }
    }

    @GetMapping("/forgotPassword")
    public String showForgotPasswordForm(Model model) {
        log.info("Opening password reset request page");
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "forgot_password";
    }

    @PostMapping("/forgotPassword")
    public String processForgotPassword(@ModelAttribute("forgotPasswordForm") @Valid ForgotPasswordForm form,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttrs,
                                        HttpServletRequest request) {
        String email = form.getEmail();
        log.info("Processing password reset request for email: {}", email);

        if (bindingResult.hasErrors()) {
            log.warn("Forgot Password form validation errors: {}", bindingResult.getAllErrors());
            return "forgot_password";
        }

        ClientDTO client = clientService.getClientByEmail(email);
        if (client == null) {
            log.warn("User with email {} not found in system", email);
            bindingResult.rejectValue("email", "forgot.email.notFound", "Email not found");
            return "forgot_password";
        }

        String token = jwtTokenProvider.generateResetToken(email);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String resetLink = baseUrl + "/resetPassword?token=" + token;

        try {
            emailService.sendPasswordResetEmail(email, resetLink);
            log.info("Sent password reset link to {}: {}", email, resetLink);
        } catch (InvalidEmailAddressException ex) {
            log.error("Failed to send reset email: invalid address for {}", email);
            bindingResult.rejectValue("email", "forgot.email.invalidAddress", "Адрес электронной почты недействителен.");
            return "forgot_password";
        } catch (EmailAuthenticationException ex) {
            log.error("Failed to send reset email: authentication error for {}", email);
            bindingResult.reject("password.reset.error.auth", "Не удалось отправить письмо: ошибка аутентификации почтового сервера.");
            return "forgot_password";
        } catch (EmailTimeoutException ex) {
            log.error("Failed to send reset email: mail server timeout for {}", email);
            bindingResult.reject("password.reset.error.timeout", "Не удалось отправить письмо: время ожидания сервера истекло. Попробуйте позже.");
            return "forgot_password";
        } catch (EmailSendingException ex) {
            log.error("Failed to send reset email to {}: unexpected error: {}", email, ex.getMessage());
            bindingResult.reject("password.reset.error", "Не удалось отправить письмо. Пожалуйста, попробуйте позже.");
            return "forgot_password";
        }

        redirectAttrs.addFlashAttribute("successMessage", "password.reset.linkSent");
        return "redirect:/login";
    }

    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        RedirectAttributes redirectAttrs,
                                        Model model) {
        log.info("Opening reset password page for token {}", token);
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid or expired reset token: {}", token);
            redirectAttrs.addFlashAttribute("errorMessage", "password.reset.invalidToken");
            return "redirect:/forgotPassword";
        }
        String email = null;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtTokenProvider.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            email = claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
        }

        ResetPasswordForm form = new ResetPasswordForm();
        form.setEmail(email);
        model.addAttribute("resetPasswordForm", form);
        model.addAttribute("token", token);
        return "reset_password";
    }


    @PostMapping("/resetPassword")
    public String processResetPassword(@RequestParam("token") String token,
                                       @ModelAttribute("resetPasswordForm") @Valid ResetPasswordForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttrs,
                                       Model model,
                                       HttpServletResponse response) {
        log.info("Processing password reset form for token {}", token);
        String email = null;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtTokenProvider.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            email = claims.getSubject();
        } catch (ExpiredJwtException ex) {
            log.warn("Reset token expired: {}", token);
            bindingResult.reject("password.reset.error", "Reset link expired");
        } catch (JwtException ex) {
            log.warn("Invalid reset token: {}", token);
            bindingResult.reject("password.reset.error", "Invalid reset token");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Reset Password form errors or invalid token: {}", bindingResult.getAllErrors());
            model.addAttribute("token", token);
            return "reset_password";
        }

        ClientDTO client = clientService.getClientByEmail(email);
        if (client == null) {
            log.error("User with email {} not found for reset (token {})", email, token);
            bindingResult.reject("password.reset.error", "Failed to reset password.");
            model.addAttribute("token", token);
            return "reset_password";
        }

        client.setPassword(form.getPassword());
        try {
            clientService.updateClientByEmail(email, client);
        } catch (Exception e) {
            log.error("Error updating password for {}: {}", email, e.getMessage());
            bindingResult.reject("password.reset.error", "Failed to reset password.");
            model.addAttribute("token", token);
            return "reset_password";
        }

        log.info("Password successfully reset for user: {}", email);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setHttpOnly(true);
        Cookie refreshCookie = new Cookie("JWT_REFRESH", "");
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        refreshCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);
        response.addCookie(refreshCookie);

        redirectAttrs.addFlashAttribute("successMessage", "password.reset.success");
        return "redirect:/login";
    }


    @PostMapping("/logout")
    public String performLogout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Performing logout");

        Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);

        SecurityContextHolder.clearContext();

        log.info("JWT token cleared and security context reset");
        return "redirect:/login?logout";
    }

    private boolean isBlocked(String email) {
        LoginAttemptInfo info = loginAttempts.get(email);
        if (info == null) return false;
        if (info.attempts < MAX_FAILED_ATTEMPTS) return false;
        long sinceLastFail = System.currentTimeMillis() - info.lastFailTime;
        if (sinceLastFail < BLOCK_DURATION_MS) {
            return true;
        } else {
            loginAttempts.remove(email);
            return false;
        }
    }

    private void recordFailedAttempt(String email) {
        LoginAttemptInfo info = loginAttempts.get(email);
        if (info == null) {
            info = new LoginAttemptInfo();
            info.attempts = 1;
        } else {
            info.attempts++;
        }
        info.lastFailTime = System.currentTimeMillis();
        loginAttempts.put(email, info);
        if (info.attempts >= MAX_FAILED_ATTEMPTS) {
            log.warn("User {} is temporarily locked due to {} failed login attempts", email, info.attempts);
        }
    }
}
