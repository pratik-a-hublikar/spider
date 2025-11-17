package com.spider.auth.repository;

import com.spider.auth.model.ApiMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiMasterRepository extends ParentRepository<ApiMaster,Long> {
}
