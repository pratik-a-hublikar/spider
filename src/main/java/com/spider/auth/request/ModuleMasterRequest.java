package com.spider.auth.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ModuleMasterRequest extends CommonRequest{

    @NotNull(message = "Module name Can not be null or empty")
    @NotEmpty(message = "Module name Can not be null or empty")
    private String moduleName;
    private String description;
    private String parentId;


}
