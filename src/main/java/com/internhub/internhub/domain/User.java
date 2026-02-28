package com.internhub.internhub.domain;

import com.internhub.internhub.domain.enums.Role;
import jakarta.persistence.*;    // JPA annotations for entity mapping
import lombok.Getter;            // Lombok annotation to generate getters
import lombok.NoArgsConstructor; // Lombok annotation to generate a no-args constructor
import lombok.Setter;            // Lombok annotation to generate setters

import java.time.LocalDateTime;

// JPA is Java Persistence API, used for ORM (Object-Relational Mapping) to map Java classes to database tables

/*
    This class represents the "users" table in the database.
    Each instance of User corresponds to a row in the "users" table.
    The fields in this class correspond to columns in the table.

    We use JPA annotations to define how this class maps to the database:
    - @Entity indicates that this is a JPA entity (a table).
    - @Table(name = "users") specifies the name of the table in the database.
    - @Id and @GeneratedValue specify that 'id' is the primary key and is auto-generated.
    - @Column specifies details about each column (e.g., nullable, unique, length).
    - @Enumerated(EnumType.STRING) tells JPA to store the enum as a string in the database.

    Lombok annotations (@Getter, @Setter, @NoArgsConstructor) automatically generate
    getter and setter methods for all fields, and a no-argument constructor, reducing boilerplate code.
*/

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches BIGSERIAL in Postgres
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email; // unique email for login

    @Column(nullable = false, length = 255, name = "password_hash")
    private String passwordHash; // store hashed password

    @Enumerated(EnumType.STRING) // stores "CANDIDATE" or "RECRUITER" as text, not ordinal (0, 1)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_file_id")
    private StoredFile cvFile; // optional CV file uploaded by the user

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // timestamp for when the user was created

    /*
        user.id ->    Long id
        user.email -> String email
        user.password_hash -> String passwordHash
        user.role -> Role role (CANDIDATE or RECRUITER)
        user.fullName -> String fullName
        user.cvFileId -> Long cvFileId (FK to StoredFile for CV)
        user.createdAt -> LocalDateTime createdAt
    */
}
