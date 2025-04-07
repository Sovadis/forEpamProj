package com.epam.rd.autocode.spring.project.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class UserRoleRefreshFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    public UserRoleRefreshFilter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {

            String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();

            UserDetails refreshedUserDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    refreshedUserDetails,
                    authentication.getCredentials(),
                    refreshedUserDetails.getAuthorities()
            );

            newAuth.setDetails(authentication.getDetails());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            log.debug("User roles refreshed for user: {}", username);
        }
        filterChain.doFilter(request, response);
    }
}
