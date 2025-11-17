package com.spider.auth.repository;

import com.spider.auth.model.DepartmentModuleMappingMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentModuleMappingMasterRepository extends ParentRepository<DepartmentModuleMappingMaster,Long> {
}
