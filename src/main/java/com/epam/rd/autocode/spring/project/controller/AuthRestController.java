package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.security.CustomUserDetailsService;
import com.epam.rd.autocode.spring.project.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthRestController {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public AuthRestController(JwtTokenProvider jwtTokenProvider,
                              CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JWT_REFRESH".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token not found");
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            Cookie refreshCookie = new Cookie("JWT_REFRESH", "");
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(0);
            refreshCookie.setHttpOnly(true);
            response.addCookie(refreshCookie);
            return ResponseEntity.status(401).body("Refresh token invalid");
        }
        String username = Jwts.parser()
                .setSigningKey(jwtTokenProvider.getKey())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", newAccessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        response.addCookie(jwtCookie);
        log.info("Issued new access token for {}", username);
        return ResponseEntity.ok().body("{\"accessToken\": \"" + newAccessToken + "\"}");
    }
}

