package com.spider.common.request.identity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleRequest {

    private Long id;
    private String name;
    private Long parentId;
    List<ModuleAccessRequest> moduleAccess;

}
