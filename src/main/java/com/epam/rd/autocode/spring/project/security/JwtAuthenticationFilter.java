package com.epam.rd.autocode.spring.project.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = getTokenFromCookie(request, "JWT_TOKEN");
        if (accessToken != null) {
            try {
                if (jwtTokenProvider.validateToken(accessToken)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException eje) {
                log.info("Access token expired, attempting to refresh...");
                String refreshToken = getTokenFromCookie(request, "JWT_REFRESH");
                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                    try {
                        String username = Jwts.parser()
                                .setSigningKey(jwtTokenProvider.getKey())
                                .build()
                                .parseClaimsJws(refreshToken)
                                .getBody()
                                .getSubject();
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
                        Authentication auth = jwtTokenProvider.getAuthentication(newAccessToken);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        Cookie jwtCookie = new Cookie("JWT_TOKEN", newAccessToken);
                        jwtCookie.setHttpOnly(true);
                        jwtCookie.setSecure(true);
                        jwtCookie.setPath("/");
                        jwtCookie.setMaxAge(3600);
                        response.addCookie(jwtCookie);
                        log.info("Issued new access token for user: {}", username);
                    } catch (JwtException ex) {
                        log.error("Failed to refresh access token: {}", ex.getMessage());
                    }
                }
            } catch (JwtException ex) {
                log.error("Error processing JWT token: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
