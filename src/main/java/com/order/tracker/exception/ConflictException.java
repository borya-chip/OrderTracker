package com.order.tracker.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(final String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
