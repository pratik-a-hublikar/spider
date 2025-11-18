package com.spider.auth.response;

import com.spider.common.response.CommonResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleDepartmentMappingMasterResponse extends CommonResponse {


    private String roleId;
    private String departmentId;

}
