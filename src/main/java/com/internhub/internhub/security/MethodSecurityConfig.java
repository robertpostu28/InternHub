package com.internhub.internhub.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/*
    Enables annotations like:
    @PreAuthorize("hasRole('RECRUITER')") so we can restrict endpoints by role cleanly.
*/

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
