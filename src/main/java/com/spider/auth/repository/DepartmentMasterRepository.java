package com.spider.auth.repository;

import com.spider.auth.model.DepartmentMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentMasterRepository extends ParentRepository<DepartmentMaster,Long> {
}
