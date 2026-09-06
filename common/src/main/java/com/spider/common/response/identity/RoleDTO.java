package com.spider.common.response.identity;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String uuid;
    private String name;
    private Long orgId;
    private List<Long> reportsToRoleIds;
    private List<RoleModuleAccessDTO> moduleAccess;
    private Boolean useMyAccesses;
    private List<Long> userIds;
    private Boolean wholeCompany;
    private Boolean canManage;

    public static RoleDTO of(Long id, String name, Object o) {
        return RoleDTO.builder().id(id).name(name).orgId((Long) o).build();
    }
}
