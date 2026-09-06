package com.spider.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class IdentityConfiguration {

    @Value("${language.path:classpath:i18n/language}")
    private String languageFilePath;

    @Bean
    public static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }
    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public String getLanguageFilePath() {
        return this.languageFilePath;
    }
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(getLanguageFilePath());
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
//    @Bean
//    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties properties) {
//        SpringLiquibase liquibase = new SpringLiquibase();
//        liquibase.setDataSource(dataSource);
//        liquibase.setChangeLog(properties.getChangeLog());
//        if (properties.getContexts() != null && !properties.getContexts().isEmpty()) {
//            liquibase.setContexts(String.join(",", properties.getContexts()));
//        }
//        liquibase.setDefaultSchema(properties.getDefaultSchema());
//        // Force it false initially so it doesn't auto-run at default Spring startup
//        liquibase.setShouldRun(false);
//        return liquibase;
//    }
//
//    // 2. Intercept the lifecycle to run Liquibase right after Hibernate completes
//    @Bean
//    public BeanPostProcessor liquibaseRunner(SpringLiquibase liquibase) {
//        return new BeanPostProcessor() {
//            @Override
//            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//                if (bean instanceof EntityManagerFactory) {
//                    try {
//                        // Hibernate has now finished ddl-auto: create/update
//                        // Now we manually trigger the Liquibase SQL queries
//                        liquibase.setShouldRun(true);
//                        liquibase.afterPropertiesSet();
//                    } catch (Exception e) {
//                        throw new RuntimeException("Failed to run Liquibase post-JPA initialization", e);
//                    }
//                }
//                return bean;
//            }
//        };
//    }


}
