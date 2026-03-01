package com.internhub.internhub.security;

// Import the Bean annotation (@Bean)
import org.springframework.context.annotation.Bean;

// Import the Configuration annotation (@Configuration)
import org.springframework.context.annotation.Configuration;

// Import the BCryptPasswordEncoder class for password encoding
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Import the PasswordEncoder interface for defining a password encoder bean
import org.springframework.security.crypto.password.PasswordEncoder;

/*
    BCrypt is one-way hashing algorithm that is designed to be computationally expensive, making it resistant to
    brute-force attacks. By using BCryptPasswordEncoder, we ensure that user passwords are securely hashed before being stored
    in the database, enhancing the overall security of the application.
*/

@Configuration
public class SecurityBeans {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Return a new instance of BCryptPasswordEncoder
    }
}
