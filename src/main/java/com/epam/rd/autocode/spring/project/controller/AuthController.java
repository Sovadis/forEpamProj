package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.LoginForm;
import com.epam.rd.autocode.spring.project.security.JwtTokenProvider;
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

@Controller
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
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
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            log.debug("Validation errors during login: {}", bindingResult.getAllErrors());
            return "login";
        }

        try {
            Authentication authentication = authenticationManager.authenticate(loginForm.toAuthenticationToken());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtTokenProvider.generateToken(userDetails);

            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            log.info("User '{}' successfully authenticated", userDetails.getUsername());
            return "redirect:/";

        } catch (LockedException e) {
            model.addAttribute("errorMessage", "login.error.blocked");
            return "login";
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email '{}': {}", loginForm.getEmail(), ex.getMessage());
            bindingResult.reject("login.error.invalid");
            return "login";
        }
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
}
