package com.spider.repository.core;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.core.UserOrganizationLink;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Repository
public interface UserOrganizationLinkRepository  extends ParentRepository<UserOrganizationLink,Long> {

    @Query("""
            select link.orgId
            from UserOrganizationLink link
            where link.userId = :userId
              and link.isActive = true
              and link.isDeleted = false
              and link.orgId in (
                  select organization.id
                  from Organization organization
                  where organization.isActive = true
                    and organization.isDeleted = false
              )
            """)
    Set<Long> findActiveOrganizationIdsByUserId(@Param("userId") Long userId);

    Optional<UserOrganizationLink> findByUserIdAndOrgId(Long userId, Long orgId);

    List<UserOrganizationLink> findByUserId(Long userId);

    boolean existsByUserIdAndOrgIdAndIsActiveAndIsDeleted(
            Long userId, Long orgId, boolean active, boolean deleted);

    Optional<UserOrganizationLink> findByUserIdAndOrgIdAndIsActiveAndIsDeleted(
            Long userId, Long orgId, boolean active, boolean deleted);

    @Query("""
            select link.userId
            from UserOrganizationLink link
            where link.orgId = :orgId
              and link.isActive = true
              and link.isDeleted = false
            """)
    List<Long> findActiveUserIdsByOrganizationId(@Param("orgId") Long orgId);

    @Query("""
            select distinct link.userId
            from UserOrganizationLink link
            where link.orgId in :orgIds
              and link.isActive = true
              and link.isDeleted = false
            """)
    Set<Long> findActiveUserIdsByOrganizationIds(@Param("orgIds") Collection<Long> orgIds);
}
