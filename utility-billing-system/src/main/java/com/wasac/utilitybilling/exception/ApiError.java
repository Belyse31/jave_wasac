package com.wasac.utilitybilling.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(boolean success, String message, Map<String, String> errors, Instant timestamp) {
    public static ApiError of(String message, Map<String, String> errors) {
        return new ApiError(false, message, errors, Instant.now());
    }
}
