package com.spider.auth.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DepartmentMasterRequest extends CommonRequest{

    private String departmentName;
    private String description;
    private Boolean isActive;

}
