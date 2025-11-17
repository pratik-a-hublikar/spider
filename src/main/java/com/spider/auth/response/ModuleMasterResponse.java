package com.spider.auth.response;

import com.spider.common.response.CommonResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ModuleMasterResponse extends CommonResponse {

    private String moduleName;

    private String description;
    private String parentId;

}
