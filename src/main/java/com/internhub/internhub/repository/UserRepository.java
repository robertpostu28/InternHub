package com.internhub.internhub.repository;

import com.internhub.internhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Optional to handle case where user may not be found - otherwise, it
                                              // would throw an exception if no user is found with the given email
                                              // for authentication purposes

    // This method is used in the UserService to check if an email is already registered before allowing a new user to sign up.
    // Avoids loading the entire User entity when we only need to check for existence, improving performance.
    boolean existsByEmail(String email);
}
