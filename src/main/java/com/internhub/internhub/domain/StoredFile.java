package com.internhub.internhub.domain;

import com.internhub.internhub.domain.enums.FileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
    File-User Relationship Mapping Guidelines:
    - @OneToOne: Use when a file and a user have a strict 1:1 relationship (each file maps to exactly one user and that
                 user has at most one such file). The owning side stores the FK (or you can share the PK)

    - @OneToMany: Use on the parent (user) when a user logically owns a collection of files. This side is usually the
                  inverse (mappedBy) — the child stores the FK

    - @ManyToOne: Use on the child (file) when many files belong to one user. This is the owning side and stores the
                  owner_id FK. Prefer fetch = FetchType.LAZY and optional = false when every file must have an owner

    - @ManyToMany: Use when users and files have a true many-to-many relationship (e.g., shared files or collaborators).
                   This creates a join table unless you model an explicit association entity — use an association entity
                   when you need extra metadata (permissions, role, timestamps). It's often better to model this as
                   two @ManyToOne relationships in an association entity (e.g., UserFile) to keep things simple and flexible.

     FetchType Guidelines:
    - FetchType.LAZY: The related entity (e.g., User) is not loaded from the database until it is accessed for the first time.
                      This can improve performance by avoiding unnecessary data loading, especially in cases where the related
                      entity is large or not always needed.

    - FetchType.EAGER: The related entity is loaded immediately along with the parent entity.

    The optional attribute in fetching:
    - optional = false: Indicates that the relationship is mandatory. The related entity must exist, and the foreign key cannot
                        be null. This is appropriate when every file must have an owner.

    - optional = true: Indicates that the relationship is optional. The related entity may or may not exist, and the foreign key
                       can be null. This is appropriate when a file may not have an owner (e.g., a temporary file that hasn't
                       been associated with a user yet).

    In practice, LAZY and EAGER are hints:
    - @ManyToOne and @OneToOne default to EAGER, but it's often better to set them to LAZY to avoid performance issues.
    - @OneToMany and @ManyToMany default to LAZY, which is usually appropriate since they can involve collections of entities.
*/

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
public class StoredFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches BIGSERIAL
    private Long id;

    // file.owner_id -> user.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // many files can belong to one user, but every file must have an owner
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // the user who uploaded the file

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private FileType fileType; // CV or COVER_LETTER

    @Column(name = "filename", nullable = false, length = 255)
    private String fileName; // original file name

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long fileSize; // size in bytes

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey; // unique key / path (local path or S3 key later)
    // Amazon S3 (Simple Storage Service) — object storage from AWS for storing and retrieving large amounts of data (files, images, backups).

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // timestamp for when the file was uploaded
}
