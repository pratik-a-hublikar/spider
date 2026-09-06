package com.spider.sevice.tech;

import com.spider.common.request.identity.ModuleAccessRequest;
import com.spider.common.response.identity.ModuleAccessDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.tech.ModuleAccessMaster;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModuleAccessMasterService extends CommonService<ModuleAccessMaster,Long> {
    Set<Long> assignModule(Long id, List<ModuleAccessRequest> moduleAccess);

    Set<Long> getRoleOfModuleAccess(Collection<Long> moduleAccessIds);

    Set<Long> assignModuleUpdate(Long id, List<ModuleAccessRequest> moduleAccess);

    List<ModuleAccessMaster> getByModuleId(Long moduleId);

    void deleteByModuleId(Long id);

    Set<Long> getModuleAccessOfRole(Long roleId);

    Map<Long, List<ModuleAccessDTO>> getRoleModuleAccessGroup(Collection<Long> roleIds);

    Map<Long, List<ModuleAccessDTO>> getUserModuleAccessGroup(Long userId, Long appId, Long orgId);

    Map<Long, List<ModuleAccessDTO>> getUserModuleAccessGroup(Long userId);
}
