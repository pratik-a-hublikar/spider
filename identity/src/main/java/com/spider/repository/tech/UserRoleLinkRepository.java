package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.UserRoleLink;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface UserRoleLinkRepository  extends ParentRepository<UserRoleLink,Long> {

    List<UserRoleLink> findByUserId(Long userId);

    List<UserRoleLink> findByRoleId(Long roleId);

    @Query("""
            select count(distinct link.userId)
            from UserRoleLink link
            where link.roleId = :roleId
              and link.isActive = true
              and link.isDeleted = false
              and link.userId in (
                  select account.id
                  from User account
                  where account.isActive = true
                    and account.isDeleted = false
              )
            """)
    long countActiveUsersByRoleId(@Param("roleId") Long roleId);

    @Query("""
            select link.roleId, count(distinct link.userId)
            from UserRoleLink link
            where link.roleId in :roleIds
              and link.isActive = true
              and link.isDeleted = false
              and link.userId in (
                  select account.id
                  from User account
                  where account.isActive = true
                    and account.isDeleted = false
              )
            group by link.roleId
            """)
    List<Object[]> countActiveUsersByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    @Query(value = "select userId from UserRoleLink where roleId in :roleIds and isActive = true and isDeleted = false")
    Set<Long> getUserIdsByRoleId(@Param("roleIds") Collection<Long> roleIds);
    @Query(value = "select userId from UserRoleLink")
    Set<Long> getAllUserIds();

    @Query(value = "select roleId from UserRoleLink where userId = :userId and isActive = true")
    Set<Long> findRoleIdOfUser(@Param("userId") Long userId);

    @Query("""
            select link.roleId
            from UserRoleLink link
            where link.userId = :userId
              and link.isActive = true
              and link.isDeleted = false
            """)
    Set<Long> findActiveRoleIdsByUserId(@Param("userId") Long userId);

    @Query(value = "select roleId from UserRoleLink where userId = :userId and orgId = :orgId and roleId in (select id from RoleMaster where orgId = :orgId) and isActive = true")
    Collection<Long> findRoleIdOfUserByOrg(@Param("userId") Long userId, @Param("orgId") Long orgId);

    @Query(value = "from UserRoleLink where userId = :userId and roleId in (select id from RoleMaster where orgId = :orgId) and isActive = :active")
    List<UserRoleLink> findByUserIdAndActive(@Param("userId") Long userId, @Param("active") boolean active, @Param("orgId") Long orgId);

    @Query(value = "select userId from UserRoleLink where orgId = :orgId and roleId in (select id from RoleMaster where orgId = :orgId and systemDefined = false) and isActive = true")
    Set<Long> findUserIdByOrg(@Param("orgId") Long orgId);

    @Query(value = "select userId from UserRoleLink where orgId = :orgId and roleId = :roleId and roleId in (select id from RoleMaster where orgId = :orgId and systemDefined = false) and isActive = true")
    Set<Long> findUserIdByRole(@Param("orgId") Long orgId, @Param("roleId") Long roleId);


}
