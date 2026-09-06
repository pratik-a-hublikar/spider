package com.spider.common.exception;

public class ValidationException  extends RuntimeException{
    private final Object[] args;

    public ValidationException(String message) {
        super(message);
        this.args = new Object[0];
    }

    public ValidationException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}