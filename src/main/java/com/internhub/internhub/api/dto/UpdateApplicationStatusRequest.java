package com.internhub.internhub.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationStatusRequest (
        @NotBlank String status
) {}
