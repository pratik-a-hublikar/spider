package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.PrivilegeMaster;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivilegeMasterRepository extends ParentRepository<PrivilegeMaster,Long> {

    List<PrivilegeMaster> findAllByOrderByUriAsc();

    PrivilegeMaster findByUriAndMethod(String uri, String method);
    PrivilegeMaster getById(Long privilegeId);

}
