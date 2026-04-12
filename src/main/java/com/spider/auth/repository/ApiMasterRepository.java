package com.spider.auth.repository;

import com.spider.auth.model.ApiMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiMasterRepository extends ParentRepository<ApiMaster,Long> {

    @Query(value = "select m.* from m_api m where lower(m.path) = lower(:uri) and lower(m.path) = lower(:method)",nativeQuery = true)
    Optional<ApiMaster> findOneByUriAndMethod(@Param("uri") String uri,@Param("method") String method);
}
