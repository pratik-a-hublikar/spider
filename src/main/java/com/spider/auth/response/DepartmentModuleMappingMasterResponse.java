package com.spider.auth.response;

import com.spider.common.response.CommonResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DepartmentModuleMappingMasterResponse extends CommonResponse {

    private String moduleId;
    private String departmentId;

}
