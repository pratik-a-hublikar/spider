package com.spider.sevice.tech.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.ModuleRequest;
import com.spider.common.request.identity.ModuleUpdateRequest;
import com.spider.common.response.identity.*;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.User;
import com.spider.enity.tech.ModuleAccessMaster;
import com.spider.enity.tech.ModuleMaster;
import com.spider.enity.tech.RoleMaster;
import com.spider.repository.core.UserRepository;
import com.spider.repository.tech.ModuleMasterRepository;
import com.spider.sevice.CommonUsableService;
import com.spider.sevice.tech.ModuleAccessMasterService;
import com.spider.sevice.tech.ModuleMasterService;
import com.spider.sevice.tech.RoleMasterService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ModuleMasterServiceImpl extends CommonServiceImpl<ModuleMaster,Long> implements ModuleMasterService {

    private final ModuleMasterRepository moduleMasterRepository;
    private final CriteriaUtil<ModuleMaster> criteriaUtil;
    private final ModuleAccessMasterService moduleAccessMasterService;
    private final CommonUsableService commonUsableService;

    private final RoleMasterService roleMasterService;
    private final UserRepository userRepository;

    @Autowired
    public ModuleMasterServiceImpl(ModuleMasterRepository moduleMasterRepository,
                                   CriteriaUtil<ModuleMaster> criteriaUtil,
                                   ModuleAccessMasterService moduleAccessMasterService,
                                   CommonUsableService commonUsableService,
                                   RoleMasterService roleMasterService,
                                   UserRepository userRepository) {
        this.moduleMasterRepository = moduleMasterRepository;
        this.criteriaUtil = criteriaUtil;
        this.moduleAccessMasterService = moduleAccessMasterService;
        this.commonUsableService = commonUsableService;
        this.roleMasterService = roleMasterService;
        this.userRepository = userRepository;
    }

    @Override
    protected ParentRepository<ModuleMaster, Long> getRepository() {
        return moduleMasterRepository;
    }

    @Override
    protected CriteriaUtil<ModuleMaster> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public void createModule(ModuleRequest request) {
        ModuleMaster module = new ModuleMaster();
        module.setModuleName(request.getName());
        module.setParentId(request.getParentId());
        module = moduleMasterRepository.save(module);
        if (!CollectionUtils.isEmpty(request.getModuleAccess())) {
            Set<Long> moduleAccessIds = moduleAccessMasterService.assignModule(module.getId(), request.getModuleAccess());
            commonUsableService.refreshACLByModuleAccess(moduleAccessIds);
        }
    }

    @Override
    public Page<ModuleMaster> getAllModule(RecordFilter filter) {
        return filter(filter);
    }

    @Override
    public ModuleManagementDTO getModuleById(Long moduleId) {
        ModuleManagementDTO moduleManagementDTO = new ModuleManagementDTO();
        ModuleDetailDTO moduleDetailDTO = getModuleDetailDTO(moduleId);
        moduleManagementDTO.setModuleDetailDTO(moduleDetailDTO);
        List<ModuleDetailDTO> parentModule = new ArrayList<>();
        while (moduleDetailDTO.getParentId() != null) {
            moduleDetailDTO = getModuleDetailDTO(moduleDetailDTO.getParentId());
            parentModule.add(moduleDetailDTO);
        }
        Collections.reverse(parentModule);
        moduleManagementDTO.setParentModule(parentModule);
        List<ModuleAccessRoleDTO> accessAndRoleForModule = getAccessAndRoleForModule(moduleId);
        moduleManagementDTO.setModuleAccess(accessAndRoleForModule.stream().map(p->p.getName().toString()).toList());
        return moduleManagementDTO;
    }

    private List<ModuleAccessRoleDTO> getAccessAndRoleForModule(Long moduleId) {
        List<ModuleAccessMaster> accesses = moduleAccessMasterService.getByModuleId(moduleId);
        return accesses.stream().map(this::toACLModuleAccessDTO).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private ModuleAccessRoleDTO toACLModuleAccessDTO(ModuleAccessMaster moduleAccess) {
        List<RoleMaster> roles = roleMasterService.getByModuleAccess(moduleAccess.getId());
        List<RoleDTO> roleDTOs = roles.stream().map(r -> RoleDTO.of(r.getId(), r.getName(),null)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(roleDTOs)) {
            return ModuleAccessRoleDTO.of(moduleAccess.getId(), moduleAccess.getName(), roleDTOs);
        } else {
            return null;
        }
    }

    private ModuleDetailDTO getModuleDetailDTO(Long moduleId) {
        ModuleMaster module = get(moduleId);
        return ModuleDetailDTO.builder()
                .id(module.getId())
                .name(module.getModuleName())
                .parentId(module.getParentId())
                .moduleUiName(module.getModuleUiName())
                .url(module.getUrl())
                .build();
    }
    @Override
    public void updateModule(ModuleUpdateRequest request,Long userId) {
        ModuleMaster module = get(request.getId());
        module.setModuleName(request.getName());
        module.setParentId(request.getParentId());
        module.setModuleUiName(request.getModuleUiName());
        module.setUpdatedBy(userId);
        module.setUpdatedAt(new Date());
        module = moduleMasterRepository.save(module);
        Set<Long> moduleAccessIds = moduleAccessMasterService.assignModuleUpdate(module.getId(), request.getModuleAccess());
        commonUsableService.refreshACLByModuleAccess(moduleAccessIds);
    }

    @Override
    public List<ModuleMaster> getChildModule(Long id) {
        return moduleMasterRepository.getByParentId(id);
    }

    @Override
    @Transactional
    public void deleteModuleById(Long id) {
        moduleAccessMasterService.deleteByModuleId(id);
        moduleMasterRepository.deleteById(id);
        commonUsableService.refreshACL();
    }

    public List<ModuleMaster> getParentModuleList() {
        return moduleMasterRepository.getParentModuleList();
    }

    public List<ModuleDetailDTO> getRoleModule(Collection<Long> roleIds) {
        Map<Long, List<ModuleAccessDTO>> moduleAccessGroup = moduleAccessMasterService.getRoleModuleAccessGroup(roleIds);
        return getModule(moduleAccessGroup.keySet(), moduleAccessGroup);
    }

    @Override
    public List<ModuleDetailDTO> getAccessibleModules(Long userId) {
        Map<Long, List<ModuleAccessDTO>> moduleAccessGroup = moduleAccessMasterService.getUserModuleAccessGroup(userId);
        return getModule(moduleAccessGroup.keySet(), moduleAccessGroup);
    }

    @Override
    public List<ModuleDetailDTO> getUserModules(Long userId, Long appId, Long orgId) {
        Map<Long, List<ModuleAccessDTO>> moduleAccessGroup =
                moduleAccessMasterService.getUserModuleAccessGroup(userId, appId, orgId);
        return getModule(moduleAccessGroup.keySet(), moduleAccessGroup);
    }


    private List<ModuleDetailDTO> getModule(Set<Long> moduleIds, Map<Long, List<ModuleAccessDTO>> moduleAccessGroup) {
        List<ModuleDetailDTO> result = new ArrayList<>();
        List<ModuleMaster> modules = get(moduleIds);
        if (modules.isEmpty()) {
            return result;
        }

        // build a map of module id -> ModuleMaster
        Map<Long, ModuleMaster> moduleMap = modules.stream().collect(Collectors.toMap(ModuleMaster::getId, Function.identity()));
        moduleIds.forEach(moduleId -> {
            ModuleMaster module = moduleMap.get(moduleId);
            if (module != null) {
                List<ModuleAccessDTO> accessList = moduleAccessGroup.get(module.getId());
                ModuleDetailDTO moduleDetailDTO = ModuleDetailDTO.builder()
                        .id(module.getId())
                        .name(module.getModuleName())
                        .parentId(module.getParentId())
                        .url(module.getUrl())
                        .moduleUiName(module.getModuleUiName())
                        .moduleAccessStr(accessList != null ? accessList.stream().map(p->p.getName().toString()).toList() : List.of())
                        .build();
                result.add(moduleDetailDTO);
            }
        });

        return getGroupedModules(result, null);
    }

    List<ModuleDetailDTO> getGroupedModules(List<ModuleDetailDTO> modules,Long parentId) {
        return modules.stream().filter(p-> Objects.equals(p.getParentId(), parentId)).peek(p-> p.setSubModules(getGroupedModules(modules,p.getId()))).toList();
    }
    public List<ModuleDetailDTO> getUserOrgModule(String authorization, Long appId, Long orgId) {
//        String email = authorizationUtil.getEmail(authorization);
        User appUser = userRepository.findByEmail("");
        Map<Long, List<ModuleAccessDTO>> moduleAccessGroup = moduleAccessMasterService.getUserModuleAccessGroup(appUser.getId(), appId, orgId);
        return getModule(moduleAccessGroup.keySet(), moduleAccessGroup);
    }
}
