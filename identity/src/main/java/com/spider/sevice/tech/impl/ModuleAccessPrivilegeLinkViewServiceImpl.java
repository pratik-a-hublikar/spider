package com.spider.sevice.tech.impl;


import com.spider.common.repository.ParentRepository;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.tech.ModuleAccessPrivilegeLinkView;
import com.spider.repository.tech.ModuleAccessPrivilegeLinkViewRepository;
import com.spider.sevice.tech.ModuleAccessPrivilegeLinkViewService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class ModuleAccessPrivilegeLinkViewServiceImpl extends CommonServiceImpl<ModuleAccessPrivilegeLinkView,Long> implements ModuleAccessPrivilegeLinkViewService {

    private final ModuleAccessPrivilegeLinkViewRepository repository;
    private final CriteriaUtil<ModuleAccessPrivilegeLinkView> criteriaUtil;
    @Autowired
    public ModuleAccessPrivilegeLinkViewServiceImpl(ModuleAccessPrivilegeLinkViewRepository repository,
                                                    CriteriaUtil<ModuleAccessPrivilegeLinkView> criteriaUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
    }


    @Override
    protected ParentRepository<ModuleAccessPrivilegeLinkView, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<ModuleAccessPrivilegeLinkView> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public List<ModuleAccessPrivilegeLinkView> getByPrivilegeId(Long privilegeId) {
        return repository.getByPrivilegeId(privilegeId);
    }
}
