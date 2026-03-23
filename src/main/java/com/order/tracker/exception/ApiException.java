package com.order.tracker.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(final HttpStatus status, final String message) {
        super(message);
        this.status = status;
    }

    protected ApiException(final HttpStatus status, final String message, final Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
