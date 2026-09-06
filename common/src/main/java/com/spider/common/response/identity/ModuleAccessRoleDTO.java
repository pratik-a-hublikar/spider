package com.spider.common.response.identity;

import com.spider.common.enums.EModuleAccessType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ModuleAccessRoleDTO {
    private Long id;
    private EModuleAccessType name;
    private List<RoleDTO> roles;

    public static ModuleAccessRoleDTO of(Long id, EModuleAccessType name, List<RoleDTO> roleDTOs) {
        return ModuleAccessRoleDTO.builder().id(id).name(name).roles(roleDTOs).build();
    }
}
