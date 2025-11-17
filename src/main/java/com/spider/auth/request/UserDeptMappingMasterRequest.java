package com.spider.auth.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDeptMappingMasterRequest extends CommonRequest{

    private String userId;
    private String departmentId;

}
