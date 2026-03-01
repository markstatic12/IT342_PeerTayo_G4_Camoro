package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown on duplicate entries (HTTP 409).
 * Maps to error code DB-002.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(String message) {
        super(message);
    }
}
