package com.spider.sevice.tech;

import com.spider.common.service.CommonService;
import com.spider.enity.tech.ModuleAccessPrivilegeLink;

import java.util.Set;

public interface ModuleAccessPrivilegeLinkService extends CommonService<ModuleAccessPrivilegeLink,Long> {
    void deleteByModuleId(Long id);

    void deleteByPrivilegeId(Long id);

    void assignPrivilege(Long id, Set<Long> moduleAccessIds);

    Set<Long> getPrivilegeIdOfModuleAccess(Set<Long> moduleAccess);
}
