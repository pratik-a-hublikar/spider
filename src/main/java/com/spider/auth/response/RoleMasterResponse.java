package com.spider.auth.response;

import com.spider.common.response.CommonResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleMasterResponse extends CommonResponse {


    private String name;
    private String description;

}
