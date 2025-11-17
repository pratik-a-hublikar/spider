package com.spider.auth.response;

import com.spider.common.response.CommonResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DepartmentMasterResponse extends CommonResponse {

    private String departmentName;
    private String description;

}
