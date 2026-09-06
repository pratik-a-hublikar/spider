package com.spider.sevice.tech.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.identity.ModuleAccessRequest;
import com.spider.common.response.identity.ModuleAccessDTO;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.tech.ModuleAccessMaster;
import com.spider.repository.tech.ModuleAccessMasterRepository;
import com.spider.repository.tech.RoleModuleAccessLinkRepository;
import com.spider.repository.tech.UserRoleLinkRepository;
import com.spider.sevice.tech.ModuleAccessMasterService;
import com.spider.sevice.tech.ModuleAccessPrivilegeLinkService;
import com.spider.sevice.tech.RoleModuleAccessLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ModuleAccessMasterServiceImpl extends CommonServiceImpl<ModuleAccessMaster,Long> implements ModuleAccessMasterService {

    private final ModuleAccessMasterRepository moduleAccessMasterRepository;
    private final RoleModuleAccessLinkService roleModuleAccessLinkService;
    private final CriteriaUtil<ModuleAccessMaster> criteriaUtil;
    private final ModuleAccessPrivilegeLinkService moduleAccessPrivilegeLinkService;
    private final RoleModuleAccessLinkRepository roleModuleAccessLinkRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;
    @Autowired
    public ModuleAccessMasterServiceImpl(ModuleAccessMasterRepository moduleAccessMasterRepository,
                                         CriteriaUtil<ModuleAccessMaster> criteriaUtil,
                                         RoleModuleAccessLinkService roleModuleAccessLinkService,
                                         ModuleAccessPrivilegeLinkService moduleAccessPrivilegeLinkService,
                                         RoleModuleAccessLinkRepository roleModuleAccessLinkRepository,
                                         UserRoleLinkRepository userRoleLinkRepository) {
        this.moduleAccessMasterRepository = moduleAccessMasterRepository;
        this.criteriaUtil = criteriaUtil;
        this.roleModuleAccessLinkService = roleModuleAccessLinkService;
        this.moduleAccessPrivilegeLinkService = moduleAccessPrivilegeLinkService;
        this.roleModuleAccessLinkRepository = roleModuleAccessLinkRepository;
        this.userRoleLinkRepository = userRoleLinkRepository;
    }


    @Override
    protected ParentRepository<ModuleAccessMaster, Long> getRepository() {
        return moduleAccessMasterRepository;
    }

    @Override
    protected CriteriaUtil<ModuleAccessMaster> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public Set<Long> assignModule(Long id, List<ModuleAccessRequest> moduleAccessList) {
        Set<Long> moduleAccessIds = new HashSet<>();
        for (ModuleAccessRequest moduleAccessRequest : moduleAccessList) {
            ModuleAccessMaster moduleAccessExist = moduleAccessMasterRepository.getByModuleIdAndName(id, moduleAccessRequest.getName());
            if (moduleAccessExist == null) {
                ModuleAccessMaster moduleAccess = new ModuleAccessMaster();
                moduleAccess.setModuleId(id);
                moduleAccess.setName(moduleAccessRequest.getName());
                moduleAccessMasterRepository.save(moduleAccess);
                moduleAccessIds.add(moduleAccess.getId());
                roleModuleAccessLinkService.addModuleAccessLink(moduleAccess.getId(), moduleAccessRequest.getRoleIds());
            } else {
                roleModuleAccessLinkService.addModuleAccessLink(moduleAccessExist.getId(), moduleAccessRequest.getRoleIds());
            }
            moduleAccessMasterRepository.flush();
        }
        return moduleAccessIds;
    }

    @Override
    public Set<Long> getRoleOfModuleAccess(Collection<Long> moduleAccessIds) {
        if (CollectionUtils.isEmpty(moduleAccessIds)) {
            return new HashSet<>();
        }
        return roleModuleAccessLinkService.findRoleIdOfModuleAccess(moduleAccessIds);
    }

    @Override
    @Transactional
    public Set<Long> assignModuleUpdate(Long id, List<ModuleAccessRequest> moduleAccessList) {
        if (CollectionUtils.isEmpty(moduleAccessList)) {
            return new HashSet<>();
        }
        Set<Long> moduleAccessIds = new HashSet<>();

        for (ModuleAccessRequest moduleAccessRequest : moduleAccessList) {
            if(moduleAccessRequest.getRoleIds() == null || moduleAccessRequest.getRoleIds().isEmpty()){
                moduleAccessRequest.setRoleIds(roleModuleAccessLinkService.findRoleIdsByModuleId(id));
            }
            roleModuleAccessLinkService.deleteByModuleId(moduleAccessRequest.getModuleId());
            ModuleAccessMaster moduleAccessExist = moduleAccessMasterRepository.getByModuleIdAndName(id, moduleAccessRequest.getName());
            if (moduleAccessExist == null) {
                ModuleAccessMaster moduleAccess = new ModuleAccessMaster();
                moduleAccess.setModuleId(id);
                moduleAccess.setName(moduleAccessRequest.getName());
                moduleAccessMasterRepository.save(moduleAccess);
                moduleAccessIds.add(moduleAccess.getId());
                roleModuleAccessLinkService.addModuleAccessLink(moduleAccess.getId(), moduleAccessRequest.getRoleIds());
            } else {
                roleModuleAccessLinkService.addModuleAccessLink(moduleAccessExist.getId(), moduleAccessRequest.getRoleIds());
            }
            moduleAccessMasterRepository.flush();
        }
        return moduleAccessIds;
    }

    @Override
    public List<ModuleAccessMaster> getByModuleId(Long moduleId) {
        return moduleAccessMasterRepository.getByModuleId(moduleId);
    }

    @Override
    @Transactional
    public void deleteByModuleId(Long id) {
        roleModuleAccessLinkService.deleteByModuleId(id);
        moduleAccessPrivilegeLinkService.deleteByModuleId(id);
        moduleAccessMasterRepository.deleteByModuleId(id);
    }

    @Override
    public Set<Long> getModuleAccessOfRole(Long roleId) {
        return roleModuleAccessLinkRepository.findModuleAccessByRole(roleId);
    }

    private Set<Long> getUserModuleAccessIds(Long userId, Long appId, Long orgId) {
        Collection<Long> roles = userRoleLinkRepository.findRoleIdOfUserByOrg(userId,  orgId);
        if (CollectionUtils.isEmpty(roles)) {
            return new HashSet<>();
        }
        return roleModuleAccessLinkRepository.findModuleAccessByRole(roles);
    }
    @Override
    public Map<Long, List<ModuleAccessDTO>> getRoleModuleAccessGroup(Collection<Long> roleIds) {
        Set<Long> moduleAccessIds = roleModuleAccessLinkRepository.findModuleAccessByRole(roleIds);
        return groupModuleAccess(moduleAccessIds);
    }

    @Override
    public Map<Long, List<ModuleAccessDTO>> getUserModuleAccessGroup(Long userId, Long appId, Long orgId) {
        Set<Long> moduleAccessIds = getUserModuleAccessIds(userId, appId, orgId);
        return groupModuleAccess(moduleAccessIds);
    }

    @Override
    public Map<Long, List<ModuleAccessDTO>> getUserModuleAccessGroup(Long userId) {
        Set<Long> roleIds = userRoleLinkRepository.findRoleIdOfUser(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        return groupModuleAccess(roleModuleAccessLinkRepository.findModuleAccessByRole(roleIds));
    }

    private Map<Long, List<ModuleAccessDTO>> groupModuleAccess(Set<Long> moduleAccessIds) {
        if (CollectionUtils.isEmpty(moduleAccessIds)) {
            return Collections.emptyMap();
        }
        List<ModuleAccessMaster> moduleAccess = get(moduleAccessIds);
//        moduleAccess.stream().
        TypeReference<List<ModuleAccessDTO>> type = new TypeReference<>() {
        };
        List<ModuleAccessDTO> moduleAccessDTOS = objectMapper.convertValue(moduleAccess, type);
        return moduleAccessDTOS.stream().collect(Collectors.groupingBy(ModuleAccessDTO::getModuleId));
    }

}
