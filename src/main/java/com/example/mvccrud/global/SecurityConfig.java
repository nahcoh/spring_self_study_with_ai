package com.example.mvccrud.global;

import com.example.mvccrud.global.security.JwtAuthenticationFilter;
import com.example.mvccrud.global.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(
                exception -> exception.authenticationEntryPoint(new HttpStatusEntryPoint(
                    HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth.requestMatchers(
                        "/actuator/health/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/h2-console/**",
                        "/auth/login",
                        "/members"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/orders").authenticated()
                    .requestMatchers(HttpMethod.GET, "/orders/my").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/orders/*/cancel").authenticated()
                    .anyRequest().permitAll()
            )
            .headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider),
                AuthorizationFilter.class
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
