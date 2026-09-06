package com.spider.repository.core;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.core.Organization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface OrganizationRepository  extends ParentRepository<Organization,Long> {

    @Query("SELECT COUNT(o) FROM Organization o WHERE o.parentOrg = :parentOrgId AND o.isActive = true AND o.isDeleted = false")
    long countActiveChildOrganizations(@Param("parentOrgId") Long parentOrgId);

    @Query("""
            select organization.id
            from Organization organization
            where organization.parentOrg in :parentOrgIds
              and organization.isActive = true
              and organization.isDeleted = false
            """)
    Set<Long> findActiveChildOrganizationIds(
            @Param("parentOrgIds") Collection<Long> parentOrgIds);

    Organization findOneByAppNameAndIsActiveAndIsDeleted(String appName, boolean active, boolean deleted);
    Organization findOneByAppNameAndIdNotAndIsActiveAndIsDeleted(String appName, Long id, boolean active, boolean deleted);

}
