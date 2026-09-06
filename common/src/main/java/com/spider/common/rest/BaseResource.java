package com.spider.common.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public abstract class BaseResource {
    @Autowired
    private MessageSource messageSource;

    public String resolve(String key, String... arg) {
        return messageSource.getMessage(key, arg, LocaleContextHolder.getLocale());
    }
}