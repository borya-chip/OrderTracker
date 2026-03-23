package com.order.tracker.exception;

import org.springframework.http.HttpStatus;

public class LoggingException extends ApiException {

    public LoggingException(final String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public LoggingException(final String message, final Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
