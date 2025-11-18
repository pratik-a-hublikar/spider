package com.spider.auth.repository;

import com.spider.auth.model.RoleDepartmentMappingMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDepartmentMappingMasterRepository extends ParentRepository<RoleDepartmentMappingMaster,Long> {
}
