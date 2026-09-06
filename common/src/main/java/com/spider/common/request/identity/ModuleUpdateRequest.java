package com.spider.common.request.identity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleUpdateRequest {
    private Long id;
    private String name;
    private String moduleUiName;
    private Long parentId;
    List<ModuleAccessRequest> moduleAccess;

}
