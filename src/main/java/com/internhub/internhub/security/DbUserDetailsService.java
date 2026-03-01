package com.internhub.internhub.security;

import com.internhub.internhub.domain.User;
import com.internhub.internhub.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
    Spring Security needs a way to load users from the database for authentification.

    During authentication (Basic Auth login), Spring Security calls:
        UserDetails loadUserByUsername(username)

    We treat 'username' as the user's email.
    We return a Spring Security User object containing:
    - username (email)
    - password (the stored BCrypt hash from users.password_hash)
    - authorities / roles (ROLE_CANDIDATE / ROLE_RECRUITER)

    Even though the method is called loadUserByUsername, we are using the email as the unique identifier for users in our
    application. This is a common practice, as emails are often used as usernames in modern applications. The method will
    look up the user by their email and return the necessary details for Spring Security to perform authentication and
    authorization checks.
   */

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public DbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Look up the user by email in the database using the UserRepository
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name()) // .roles("CANDIDATE") becomes authority "ROLE_CANDIDATE" automatically
                .build();
    }
}
