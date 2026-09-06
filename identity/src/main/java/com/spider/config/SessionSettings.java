package com.spider.config;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SessionSettings {

    private final int timeout;
    public SessionSettings(@Value("${spider.session.timeout:-1}") int timeout) {
        this.timeout = timeout;
    }


}
