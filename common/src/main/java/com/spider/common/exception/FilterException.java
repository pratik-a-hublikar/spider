package com.spider.common.exception;

public class FilterException extends RuntimeException{
    private final Object[] args;

    public FilterException(String message) {
        super(message);
        this.args = new Object[0];
    }

    public FilterException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
