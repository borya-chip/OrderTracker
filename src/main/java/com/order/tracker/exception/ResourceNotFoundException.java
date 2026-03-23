package com.order.tracker.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(final String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
