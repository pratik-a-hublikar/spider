package com.spider.common.request.identity;

import com.spider.common.enums.EModuleAccessType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ModuleAccessRequest {
    private Long id;
    private EModuleAccessType name;
    private Long moduleId;
    private Set<Long> roleIds;
}
