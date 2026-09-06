package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.ModuleAccessPrivilegeLink;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Repository
public interface ModuleAccessPrivilegeLinkRepository  extends ParentRepository<ModuleAccessPrivilegeLink,Long> {
    @Transactional
    @Modifying
    @Query("delete from ModuleAccessPrivilegeLink where moduleAccessId in (select id from ModuleAccessMaster where moduleId = :moduleId)")
    void deleteByModuleId(@Param("moduleId") Long moduleId);

    void deleteByPrivilegeId(Long id);

    @Query("select privilegeId from ModuleAccessPrivilegeLink where moduleAccessId in :moduleAccess")
    Set<Long> findPrivilegeOfModuleAccess(@Param("moduleAccess") Set<Long> moduleAccess);

}
