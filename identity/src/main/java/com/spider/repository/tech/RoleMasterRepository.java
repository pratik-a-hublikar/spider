package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.RoleMaster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMasterRepository  extends ParentRepository<RoleMaster,Long> {
    @Query(value = "from RoleMaster where id in (select roleId from RoleModuleAccessLink where moduleAccessId = :moduleAccessId)")
    List<RoleMaster> getByModuleAccessId(@Param("moduleAccessId") Long moduleAccessId);

}

