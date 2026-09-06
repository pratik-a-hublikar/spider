package com.spider.sevice.tech.impl;


import com.spider.common.repository.ParentRepository;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.tech.ModuleAccessPrivilegeLink;
import com.spider.repository.tech.ModuleAccessPrivilegeLinkRepository;
import com.spider.sevice.tech.ModuleAccessPrivilegeLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

@Log4j2
@Service
public class ModuleAccessPrivilegeLinkServiceImpl extends CommonServiceImpl<ModuleAccessPrivilegeLink,Long> implements ModuleAccessPrivilegeLinkService {

    private final ModuleAccessPrivilegeLinkRepository repository;
    private final CriteriaUtil<ModuleAccessPrivilegeLink> criteriaUtil;
    @Autowired
    public ModuleAccessPrivilegeLinkServiceImpl(ModuleAccessPrivilegeLinkRepository repository,
                                                CriteriaUtil<ModuleAccessPrivilegeLink> criteriaUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
    }


    @Override
    protected ParentRepository<ModuleAccessPrivilegeLink, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<ModuleAccessPrivilegeLink> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    @Transactional
    public void deleteByModuleId(Long moduleId) {
        repository.deleteByModuleId(moduleId);
    }

    @Override
    public void deleteByPrivilegeId(Long id) {
        repository.deleteByPrivilegeId(id);
    }

    @Override
    @Transactional
    public void assignPrivilege(Long privilegeId, Set<Long> moduleAccessIds) {
        moduleAccessIds.forEach(id -> repository.save(ModuleAccessPrivilegeLink.builder().moduleAccessId(id).privilegeId(privilegeId).build()));
    }

    @Override
    public Set<Long> getPrivilegeIdOfModuleAccess(Set<Long> moduleAccess) {
        if (CollectionUtils.isEmpty(moduleAccess)) {
            return new HashSet<>();
        }
        return repository.findPrivilegeOfModuleAccess(moduleAccess);
    }

}
