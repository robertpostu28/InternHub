package com.internhub.internhub.common.api;

import lombok.Getter;

@Getter
public class FieldValidationError {
    private final String field;
    private final String message;

    public FieldValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
