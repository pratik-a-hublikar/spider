package com.spider.auth.repository;

import com.spider.auth.model.UserRoleMappingMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleMappingMasterRepository extends ParentRepository<UserRoleMappingMaster,Long> {
}
