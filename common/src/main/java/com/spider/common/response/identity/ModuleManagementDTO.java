package com.spider.common.response.identity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleManagementDTO {
    private ModuleDetailDTO moduleDetailDTO;
    private List<ModuleDetailDTO> parentModule;
    private List<String> moduleAccess;
}
