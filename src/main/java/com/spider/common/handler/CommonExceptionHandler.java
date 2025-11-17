package com.spider.common.handler;

import com.spider.auth.exception.AuthenticationException;
import com.spider.common.exception.ValidationException;
import com.spider.common.response.CommonPayLoad;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.spider")
@Log4j2
public class CommonExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<CommonPayLoad<String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.info("handleMethodArgumentNotValidException: {}",ex.getMessage(),ex);
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return new ResponseEntity<>(CommonPayLoad.of("Validation failed", HttpStatus.BAD_REQUEST, validationErrors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<CommonPayLoad<String>> internalError(
            Exception e) {
        log.error("internalError: {}",e.getMessage(),e);
        return new ResponseEntity<>(CommonPayLoad.of("Something went wrong, Please try again later.",null, HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<CommonPayLoad<String>> handleAuthenticationException(
            AuthenticationException e) {
        log.info("handleAuthenticationException: {}",e.getMessage(),e);
        return new ResponseEntity<>(CommonPayLoad.of(e.getMessage(),null, HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<CommonPayLoad<String>> handleValidationException(
            ValidationException e) {
        log.info("handleValidationException: {}",e.getMessage(),e);
        return new ResponseEntity<>(CommonPayLoad.of(e.getMessage(),null, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }


}
