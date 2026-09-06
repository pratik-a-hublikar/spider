package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.RoleReportingRoleLink;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collection;
import java.util.Set;

@Repository
public interface RoleReportingRoleLinkRepository extends ParentRepository<RoleReportingRoleLink, Long> {

    @Query("select reportsToRoleId from RoleReportingRoleLink where roleId = :roleId and isActive = true and isDeleted = false")
    List<Long> findReportsToRoleIds(@Param("roleId") Long roleId);

    @Query("""
            select distinct link.reportsToRoleId
            from RoleReportingRoleLink link
            where link.roleId in :roleIds
              and link.isActive = true
              and link.isDeleted = false
              and link.reportsToRoleId in (
                  select role.id
                  from RoleMaster role
                  where role.isActive = true
                    and role.isDeleted = false
              )
            """)
    Set<Long> findActiveParentRoleIds(@Param("roleIds") Collection<Long> roleIds);

    @Query("select distinct roleId from RoleReportingRoleLink where reportsToRoleId = :reportsToRoleId and isActive = true and isDeleted = false")
    List<Long> findRoleIdsReportingTo(@Param("reportsToRoleId") Long reportsToRoleId);

    @Query("""
            select distinct link.roleId
            from RoleReportingRoleLink link
            where link.reportsToRoleId in :reportsToRoleIds
              and link.isActive = true
              and link.isDeleted = false
              and link.roleId in (
                  select role.id
                  from RoleMaster role
                  where role.isActive = true
                    and role.isDeleted = false
              )
            """)
    Set<Long> findActiveChildRoleIds(
            @Param("reportsToRoleIds") Collection<Long> reportsToRoleIds);

    @Transactional
    @Modifying
    @Query("delete from RoleReportingRoleLink where roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
