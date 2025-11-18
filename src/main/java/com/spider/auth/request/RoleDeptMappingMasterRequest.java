package com.spider.auth.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleDeptMappingMasterRequest extends CommonRequest{

    private String roleId;
    private String departmentId;

}
