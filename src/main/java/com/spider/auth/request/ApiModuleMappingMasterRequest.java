package com.spider.auth.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiModuleMappingMasterRequest extends CommonRequest{

    @NotNull(message = "moduleId Can not be null or empty")
    @NotEmpty(message = "moduleId Can not be null or empty")
    private String moduleId;

    @NotNull(message = "apiId Can not be null or empty")
    @NotEmpty(message = "apiId Can not be null or empty")
    private String apiId;

}
