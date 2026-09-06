package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.RoleModuleAccessLink;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.List;

@Repository
public interface RoleModuleAccessLinkRepository  extends ParentRepository<RoleModuleAccessLink,Long> {

    @Query("select moduleAccessId from RoleModuleAccessLink where roleId=:roleId")
    Set<Long> findModuleAccessByRole(@Param("roleId") Long roleId);

    @Query("""
            select moduleAccessId
            from RoleModuleAccessLink
            where roleId = :roleId
              and isActive = true
              and isDeleted = false
            """)
    Set<Long> findActiveModuleAccessIdsByRoleId(@Param("roleId") Long roleId);

    @Query("""
            select distinct moduleAccessId
            from RoleModuleAccessLink
            where roleId in :roleIds
              and isActive = true
              and isDeleted = false
            """)
    Set<Long> findActiveModuleAccessIdsByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    @Transactional
    @Modifying
    @Query("delete from RoleModuleAccessLink where roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);


    @Query("select roleId from RoleModuleAccessLink where moduleAccessId in :moduleAccessIds")
    Set<Long> findRoleIdOfModuleAccess(@Param("moduleAccessIds") Collection<Long> moduleAccessIds);
    @Query("select moduleAccessId from RoleModuleAccessLink where roleId in :roles")
    Set<Long> findModuleAccessByRole(@Param("roles") Collection<Long> roles);

    @Transactional
    @Modifying
    @Query("delete from RoleModuleAccessLink where moduleAccessId in (select id from ModuleAccessMaster where moduleId = :moduleId)")
    void deleteByModuleId(@Param("moduleId") Long moduleId);

    @Query("select distinct(roleId) from RoleModuleAccessLink where moduleAccessId in (select id from ModuleAccessMaster where moduleId = :moduleId)")
    Set<Long> findRoleIdsByModuleId(@Param("moduleId") Long moduleId);
}
