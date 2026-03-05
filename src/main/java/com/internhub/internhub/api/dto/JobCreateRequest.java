package com.internhub.internhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobCreateRequest (
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        String requirements,
        @Size(max = 255) String location
) {}
