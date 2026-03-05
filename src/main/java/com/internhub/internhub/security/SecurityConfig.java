package com.internhub.internhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Import the Customizer class for customizing HTTP security configuration
import org.springframework.security.config.Customizer;

// Import the EnableWebSecurity annotation to enable Spring Security's web security support
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// Import the SessionCreationPolicy enum for configuring session management
import org.springframework.security.config.http.SessionCreationPolicy;

// Import the SecurityFilterChain interface for defining the security filter chain
import org.springframework.security.web.SecurityFilterChain;

// Import the HttpSecurity class for configuring web-based security for specific HTTP requests
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

// Import the HttpMethod enum for specifying HTTP methods in request matchers
import org.springframework.http.HttpMethod;

/*
    This configuration defines how Spring Security protects our API.

    MVP choice: HTTP Basic Auth
    - simple to set up
    - good enough for a backend portfolio MVP
    - easy to test with tools like Postman or curl
    Later we can replace it with JWT
    (Jason Web Tokens - a more modern and secure authentication method for stateless APIs) without changing the API endpoints
    or the database, just by changing the security configuration and the login endpoint to return a JWT instead of relying
    on HTTP Basic Auth headers.

    Rules:
    - /auth/** endpoints are PUBLIC (register/login endpoints)
    - everything else requires authentication (user must be logged in with valid credentials to access any other endpoints)
    - stateless session (REST style - no server-side session, each request must include authentication credentials)
    - CSRF disabled (we are not using browser cookies/sessions for auth in this MVP)
*/

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/jobs/**").permitAll()
                        .requestMatchers("/auth/**").permitAll() // ** means that all endpoints under /auth/ are accessible without authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Enable HTTP Basic authentication

        return http.build(); // Build and return the SecurityFilterChain
    }
}
