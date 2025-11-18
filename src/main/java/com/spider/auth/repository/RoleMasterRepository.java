package com.spider.auth.repository;

import com.spider.auth.model.RoleMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleMasterRepository extends ParentRepository<RoleMaster,Long> {
}
