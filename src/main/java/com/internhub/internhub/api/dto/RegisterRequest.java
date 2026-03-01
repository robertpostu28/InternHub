package com.internhub.internhub.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
  Record means that this class is immutable and has a concise syntax for defining data carriers.
  It automatically generates constructor, getters, equals, hashCode, and toString methods.
  The fields are defined in the record header, and the validation annotations ensure that the incoming data meets the specified
  constraints before processing.
*/

public record RegisterRequest (
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String fullName,
    @NotBlank String role
) {}
