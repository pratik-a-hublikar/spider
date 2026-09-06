package com.spider.exception;

import org.springframework.http.HttpStatus;

public class ShipmntsIntegrationException extends RuntimeException {
    private final HttpStatus status;

    public ShipmntsIntegrationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
