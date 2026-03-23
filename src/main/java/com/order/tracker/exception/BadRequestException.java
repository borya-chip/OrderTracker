package com.order.tracker.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
