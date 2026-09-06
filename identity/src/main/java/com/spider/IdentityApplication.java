package com.spider;

import com.spider.sevice.CommonUsableService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = { "com.spider"})
@EnableAutoConfiguration
@EntityScan(basePackages = { "com.spider"})
@EnableJpaRepositories(basePackages = { "com.spider"})
@RestController
public class IdentityApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(IdentityApplication.class, args);
        CommonUsableService commonUsableService = run.getBean(CommonUsableService.class);
        commonUsableService.refreshACL();
    }
}