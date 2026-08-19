package com.spider.common.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonPayLoad<T> implements Serializable {
    private String message;
    private T payload;

    private HttpStatusCode httpStatusCode;
    private int statusCode;
    Map<String, String> validationErrors;
    public CommonPayLoad(String message, T payload, HttpStatusCode httpStatusCode) {
        this.message = message;
        this.payload = payload;
        this.statusCode = httpStatusCode.value();
    }

    public CommonPayLoad(String message, T payload, int statusCode) {
        this.message = message;
        this.payload = payload;
        this.statusCode = statusCode;
    }

    public static <T> CommonPayLoad<T> of(T payload){
        return new CommonPayLoad<>("Success",payload,HttpStatusCode.valueOf(200));
    }
    public static <T> CommonPayLoad<T> of(String message, T payload){
        return new CommonPayLoad<>(message,payload,HttpStatusCode.valueOf(200));
    }
    public static <T> CommonPayLoad<T> of(String message, T payload, HttpStatusCode code){
        return new CommonPayLoad<>(message,payload,code);
    }
    public static <T> CommonPayLoad<T> of(String message, HttpStatusCode httpStatusCode, Map<String, String> validationErrors){
        return new CommonPayLoad<>(message,httpStatusCode,validationErrors);
    }
    public CommonPayLoad(String message, HttpStatusCode httpStatusCode, Map<String, String> validationErrors) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.statusCode = httpStatusCode.value();
        this.validationErrors = validationErrors;
    }
}