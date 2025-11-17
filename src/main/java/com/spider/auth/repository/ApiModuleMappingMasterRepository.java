package com.spider.auth.repository;

import com.spider.auth.model.ApiModuleMappingMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiModuleMappingMasterRepository extends ParentRepository<ApiModuleMappingMaster,Long> {
}
