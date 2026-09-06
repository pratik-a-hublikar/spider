package com.spider.sevice.tech.impl;


import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.PrivilegeCreateRequest;
import com.spider.common.request.identity.PrivilegeUpdateRequest;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.dto.PrivilegeDTO;
import com.spider.enity.tech.ModuleAccessPrivilegeLinkView;
import com.spider.enity.tech.ModuleMaster;
import com.spider.enity.tech.PrivilegeMaster;
import com.spider.repository.tech.PrivilegeMasterRepository;
import com.spider.repository.tech.UserRoleLinkRepository;
import com.spider.sevice.CommonUsableService;
import com.spider.sevice.RedisService;
import com.spider.sevice.tech.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Log4j2
@Service
public class PrivilegeMasterServiceImpl extends CommonServiceImpl<PrivilegeMaster,Long> implements PrivilegeMasterService {

    private final PrivilegeMasterRepository repository;
    private final CriteriaUtil<PrivilegeMaster> criteriaUtil;
    private final ModuleAccessPrivilegeLinkViewService accessPrivilegeLinkViewService;
    private final ModuleMasterService moduleMasterService;
    private final ModuleAccessPrivilegeLinkService moduleAccessPrivilegeLinkService;

    private final CommonUsableService commonUsableService;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final RedisService redisService;
    private final ModuleAccessMasterService moduleAccessMasterService;
    @Autowired
    public PrivilegeMasterServiceImpl(PrivilegeMasterRepository repository,
                                      CriteriaUtil<PrivilegeMaster> criteriaUtil,
                                      ModuleAccessPrivilegeLinkViewService accessPrivilegeLinkViewService,
                                      ModuleMasterService moduleMasterService,
                                      ModuleAccessPrivilegeLinkService moduleAccessPrivilegeLinkService,
                                      CommonUsableService commonUsableService,
                                      UserRoleLinkRepository userRoleLinkRepository,
                                      RedisService redisService,
                                      ModuleAccessMasterService moduleAccessMasterService) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.accessPrivilegeLinkViewService = accessPrivilegeLinkViewService;
        this.moduleMasterService = moduleMasterService;
        this.moduleAccessPrivilegeLinkService = moduleAccessPrivilegeLinkService;
        this.commonUsableService = commonUsableService;
        this.userRoleLinkRepository = userRoleLinkRepository;
        this.redisService = redisService;
        this.moduleAccessMasterService = moduleAccessMasterService;
    }


    @Override
    protected ParentRepository<PrivilegeMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<PrivilegeMaster> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public Page<PrivilegeMaster> getAllPrivilege(RecordFilter filter) {
        return filter(filter);
    }

    @Override
    public PrivilegeDTO getPrivilegeById(Long privilegeId) {
        PrivilegeMaster privilege = repository.getById(privilegeId);
        PrivilegeDTO privilegeDTO = PrivilegeDTO.builder().id(privilegeId).name(privilege.getName()).uri(privilege.getUri()).method(privilege.getMethod()).build();
        List<ModuleAccessPrivilegeLinkView> lstModuleAccessPrvlgLinkView = accessPrivilegeLinkViewService.getByPrivilegeId(privilegeId);
        Map<Long, Map<Long, String>> moduleAccessMap = new HashMap<>();
        Map<Long, String> map = new HashMap<>();
        Map<Long, ModuleAccessPrivilegeLinkView> mapModuleAccessPrvlgLinkView = new HashMap<>();
        ArrayList<ModuleAccessPrivilegeLinkView> moduleAccessPrvlgLinkViewList = new ArrayList<>();
        for (ModuleAccessPrivilegeLinkView moduleAccessPrvlgLinkView : lstModuleAccessPrvlgLinkView) {
            if (!mapModuleAccessPrvlgLinkView.containsKey(moduleAccessPrvlgLinkView.getModuleId())) {
                moduleAccessPrvlgLinkViewList.add(moduleAccessPrvlgLinkView);
                mapModuleAccessPrvlgLinkView.put(moduleAccessPrvlgLinkView.getModuleId(), moduleAccessPrvlgLinkView);
            }
            if (!moduleAccessMap.containsKey(moduleAccessPrvlgLinkView.getModuleId())) {
                map = new HashMap<>();
                map.put(moduleAccessPrvlgLinkView.getModuleAccessId(), moduleAccessPrvlgLinkView.getModuleAccessName()+"||"+moduleAccessPrvlgLinkView.getRoleName());
                moduleAccessMap.put(moduleAccessPrvlgLinkView.getModuleId(), map);
            } else {
                map.put(moduleAccessPrvlgLinkView.getModuleAccessId(), moduleAccessPrvlgLinkView.getModuleAccessName()+"||"+moduleAccessPrvlgLinkView.getRoleName());
                moduleAccessMap.put(moduleAccessPrvlgLinkView.getModuleId(), map);
            }
        }
        for (ModuleAccessPrivilegeLinkView moduleAccessPrvlgLinkView : moduleAccessPrvlgLinkViewList) {
            ArrayList<ModuleMaster> lstModule = new ArrayList<>();
            ModuleMaster module = moduleMasterService.get(moduleAccessPrvlgLinkView.getModuleId());
            lstModule.add(module);
            while (module.getParentId() != null) {
                module = moduleMasterService.get(module.getParentId());
                ModuleMaster build = ModuleMaster.builder().moduleName(module.getModuleName()).
                        parentId(module.getParentId()).build();
                build.setId(module.getId());
                lstModule.add(build);
            }
            if (moduleAccessMap.containsKey(moduleAccessPrvlgLinkView.getModuleId())) {
                moduleAccessPrvlgLinkView.setModuleAccess(moduleAccessMap.get(moduleAccessPrvlgLinkView.getModuleId()));
            }
            Collections.reverse(lstModule);
            moduleAccessPrvlgLinkView.setParentModuleList(lstModule);

        }
        privilegeDTO.setLstModuleAccess(moduleAccessPrvlgLinkViewList);
        return privilegeDTO;
    }

    @Override
    public void updatePrivilege(PrivilegeUpdateRequest request) {
        PrivilegeMaster privilege = new PrivilegeMaster();
        privilege.setId(request.getId());
        privilege.setName(request.getName());
        privilege.setUri(request.getUrl());
        privilege.setMethod(request.getMethod());
        privilege = repository.save(privilege);
        moduleAccessPrivilegeLinkService.deleteByPrivilegeId(privilege.getId());
        moduleAccessPrivilegeLinkService.assignPrivilege(privilege.getId(), request.getModuleAccessIds());
        commonUsableService.refreshACLByModuleAccess(request.getModuleAccessIds());
    }

    @Override
    public Page<PrivilegeMaster> getAllPrevilege(RecordFilter filter) {
        return filter(filter);
    }
    @Transactional
    @Override
    public void deletePrivilegeByPrivilegeId(Long id) {
        moduleAccessPrivilegeLinkService.deleteByPrivilegeId(id);
        repository.deleteById(id);
        commonUsableService.refreshACL();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public List<PrivilegeDTO> getAllPermissions() {
        List<PrivilegeMaster> privileges = repository.findAllByOrderByUriAsc();
        return privileges.stream().map(privilege -> new PrivilegeDTO(privilege.getId(), privilege.getUri(), privilege.getMethod())).toList();
    }

    @Override
    public void createPrivilege(PrivilegeCreateRequest request) {
        PrivilegeMaster exist = get(request.getUrl(), request.getMethod());
        if (exist != null) {
            throw new ValidationException("api.already.exist");
        }
        PrivilegeMaster privilege = new PrivilegeMaster();
        privilege.setName(request.getName());
        privilege.setUri(request.getUrl());
        privilege.setMethod(HttpMethod.valueOf(request.getMethod().toUpperCase()).name());
        privilege = repository.save(privilege);
        moduleAccessPrivilegeLinkService.assignPrivilege(privilege.getId(), request.getModuleAccessIds());
        commonUsableService.refreshACLByModuleAccess(request.getModuleAccessIds());
    }

    @Override
    public PrivilegeMaster get(String uri, String method) {
        return repository.findByUriAndMethod(uri, method);
    }

    @Override
    public Collection<Long> getPrivilegesByUserId(Long userId) {
        Set<Long> roleIds = userRoleLinkRepository.findRoleIdOfUser(userId);
        return getPrivileges(roleIds);
    }

    private Set<Long> getPrivileges(Set<Long> roleIds) {
        Set<Long> privileges = new HashSet<>();
        for (Long id : roleIds) {
            privileges.addAll(getPrivileges(id));
        }
        return privileges;
    }

    private Collection<Long> getPrivileges(Long roleId) {
        Collection<Long> privilegesIds = redisService.getPrivilege(roleId);
        if (CollectionUtils.isEmpty(privilegesIds)) {
            privilegesIds = getPrivilegeIdsOfRole(roleId);
            redisService.putPrivilege(roleId, privilegesIds);
        }
        return privilegesIds;
    }

    private Collection<Long> getPrivilegeIdsOfRole(Long roleId) {
        Set<Long> moduleAccessIds = moduleAccessMasterService.getModuleAccessOfRole(roleId);
        return moduleAccessPrivilegeLinkService.getPrivilegeIdOfModuleAccess(moduleAccessIds);
    }

}
