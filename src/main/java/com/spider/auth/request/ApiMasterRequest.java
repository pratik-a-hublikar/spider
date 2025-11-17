package com.spider.auth.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiMasterRequest extends CommonRequest{

    @NotNull(message = "Name Can not be null or empty")
    @NotEmpty(message = "Name Can not be null or empty")
    private String name;

    @NotNull(message = "Path Can not be null or empty")
    @NotEmpty(message = "Path Can not be null or empty")
    private String path;

    @NotNull(message = "Method Can not be null or empty")
    @NotEmpty(message = "Method Can not be null or empty")
    private String method;


    @NotNull(message = "Description Can not be null or empty")
    @NotEmpty(message = "Description Can not be null or empty")
    private String description;


}
