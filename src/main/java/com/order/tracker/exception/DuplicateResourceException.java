package com.order.tracker.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(final String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
