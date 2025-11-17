package com.spider.auth.repository;

import com.spider.auth.model.UserMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMasterRepository extends ParentRepository<UserMaster,Long> {
    UserMaster findOneByEmailAndIsActiveAndIsDeleted(String username, boolean active, boolean deleted);

}
