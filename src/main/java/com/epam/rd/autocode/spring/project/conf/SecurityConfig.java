package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.security.*;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOidcUserService customOidcUserService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService userDetailsService, CustomOidcUserService customOidcUserService, JwtTokenProvider jwtTokenProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.customOidcUserService = customOidcUserService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register",
                                "/register/**", "/resources/**", "/h2-console/**",
                                "/forgotPassword", "/auth/refresh",
                                "/oauth2/**", "/login/oauth2/**", "/resetPassword").permitAll()
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/client/**").hasRole("CLIENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout"))
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                        .successHandler((request, response, authentication) -> {
                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                            String email = oidcUser.getEmail();
                            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                            String token = jwtTokenProvider.generateToken(userDetails);
                            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
                            Cookie accessCookie = new Cookie("JWT_TOKEN", token);
                            accessCookie.setHttpOnly(true);
                            accessCookie.setSecure(true);
                            accessCookie.setPath("/");
                            accessCookie.setMaxAge(3600);
                            Cookie refreshCookie = new Cookie("JWT_REFRESH", refreshToken);
                            refreshCookie.setHttpOnly(true);
                            refreshCookie.setSecure(true);
                            refreshCookie.setPath("/");
                            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
                            response.addCookie(accessCookie);
                            response.addCookie(refreshCookie);
                            log.info("OAuth2 login successful for {}, JWT cookies set", email);
                            response.sendRedirect("/");
                        })
                        .failureUrl("/login?error"))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();
                            if ("/login".equals(requestURI)) {
                                response.sendError(401, "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new UserRoleRefreshFilter(userDetailsService),
                org.springframework.security.web.context.SecurityContextPersistenceFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
