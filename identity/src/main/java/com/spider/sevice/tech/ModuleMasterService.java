package com.spider.sevice.tech;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.ModuleRequest;
import com.spider.common.request.identity.ModuleUpdateRequest;
import com.spider.common.response.identity.ModuleManagementDTO;
import com.spider.common.response.identity.ModuleDetailDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.tech.ModuleMaster;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Collection;

public interface ModuleMasterService extends CommonService<ModuleMaster,Long> {
    void createModule(ModuleRequest request);

    Page<ModuleMaster> getAllModule(RecordFilter filter);

    ModuleManagementDTO getModuleById(Long moduleId);

    void updateModule(ModuleUpdateRequest request,Long userId);

    List<ModuleMaster> getChildModule(Long id);

    void deleteModuleById(Long id);

    List<ModuleDetailDTO> getAccessibleModules(Long userId);

    List<ModuleMaster> getParentModuleList();

    List<ModuleDetailDTO> getRoleModule(Collection<Long> roleIds);

    List<ModuleDetailDTO> getUserModules(Long userId, Long appId, Long orgId);

}
