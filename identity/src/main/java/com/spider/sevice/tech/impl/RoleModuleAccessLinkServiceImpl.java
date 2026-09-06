package com.spider.sevice.tech.impl;


import com.spider.common.repository.ParentRepository;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.tech.RoleModuleAccessLink;
import com.spider.repository.tech.RoleModuleAccessLinkRepository;
import com.spider.sevice.tech.RoleModuleAccessLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Log4j2
@Service
public class RoleModuleAccessLinkServiceImpl extends CommonServiceImpl<RoleModuleAccessLink,Long> implements RoleModuleAccessLinkService {

    private final RoleModuleAccessLinkRepository repository;
    private final CriteriaUtil<RoleModuleAccessLink> criteriaUtil;
    @Autowired
    public RoleModuleAccessLinkServiceImpl(RoleModuleAccessLinkRepository repository,
                                           CriteriaUtil<RoleModuleAccessLink> criteriaUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
    }


    @Override
    protected ParentRepository<RoleModuleAccessLink, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<RoleModuleAccessLink> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public void addModuleAccessLink(Long moduleAccessId, Set<Long> roleIds) {
        if(roleIds != null) {
            roleIds.forEach(id -> repository.save(RoleModuleAccessLink.builder().moduleAccessId(moduleAccessId).roleId(id).build()));
        }
    }

    @Override
    public Set<Long> findRoleIdOfModuleAccess(Collection<Long> moduleAccessIds) {
        return null;
    }

    @Override
    public void deleteByModuleId(Long moduleId) {
        repository.deleteByModuleId(moduleId);
    }

    @Override
    public Set<Long> findRoleIdsByModuleId(Long moduleId) {
        return repository.findRoleIdsByModuleId(moduleId);
    }
}
