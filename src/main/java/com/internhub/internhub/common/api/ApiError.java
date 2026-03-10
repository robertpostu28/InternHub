package com.internhub.internhub.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private final String code;
    private final String message;
    private final Instant timestamp;
    private final String path;
    private final List<FieldValidationError> fieldErrors;

    public ApiError(String code, String message, Instant timestamp, String path, List<FieldValidationError> fieldErrors) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }
}
