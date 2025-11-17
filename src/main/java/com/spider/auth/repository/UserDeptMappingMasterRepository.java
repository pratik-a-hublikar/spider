package com.spider.auth.repository;

import com.spider.auth.model.UserDeptMappingMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDeptMappingMasterRepository extends ParentRepository<UserDeptMappingMaster,Long> {
}
