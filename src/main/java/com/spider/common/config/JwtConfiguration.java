package com.spider.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class JwtConfiguration {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.prefix}")
    private String prefix;
    @Value("${jwt.login.ttl}")
    private Long loginTTL;
    @Value("${jwt.otp.ttl}")
    private Long otpTTL;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getLoginTTL() {
        return loginTTL;
    }

    public void setLoginTTL(Long loginTTL) {
        this.loginTTL = loginTTL;
    }

    public Long getOtpTTL() {
        return otpTTL;
    }

    public void setOtpTTL(Long otpTTL) {
        this.otpTTL = otpTTL;
    }
}
