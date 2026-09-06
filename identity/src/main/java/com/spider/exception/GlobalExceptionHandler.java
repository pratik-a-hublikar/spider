package com.spider.exception;

import com.spider.common.exception.ValidationException;
import com.spider.common.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CommonResponse<Object>> handleIllegalArgumentException(
            ValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.of(null, resolve(exception.getMessage(), exception.getArgs())));
    }

    @ExceptionHandler(com.spider.common.exception.FilterException.class)
    public ResponseEntity<CommonResponse<Object>> handleFilterException(
            com.spider.common.exception.FilterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.of(null, resolve(exception.getMessage(), exception.getArgs())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Object>> handleValidationError(
            MethodArgumentNotValidException exception) {
        String validationErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .map(message -> resolve(message))
                .distinct()
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.of(null, validationErrors));
    }

    @ExceptionHandler(ShipmntsIntegrationException.class)
    public ResponseEntity<CommonResponse<Object>> handleShipmntsIntegrationException(
            ShipmntsIntegrationException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(CommonResponse.of(null, exception.getMessage()));
    }

    private String resolve(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
