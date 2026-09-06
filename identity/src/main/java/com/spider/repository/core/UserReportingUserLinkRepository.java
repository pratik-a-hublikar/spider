package com.spider.repository.core;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.core.UserReportingUserLink;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserReportingUserLinkRepository extends ParentRepository<UserReportingUserLink, Long> {

    List<UserReportingUserLink> findByUserId(Long userId);

    Optional<UserReportingUserLink> findByUserIdAndReportsToUserId(Long userId, Long reportsToUserId);

    @Query("""
            select link.reportsToUserId
            from UserReportingUserLink link
            where link.userId = :userId
              and link.isActive = true
              and link.isDeleted = false
            """)
    Set<Long> findActiveReportsToUserIds(@Param("userId") Long userId);

    @Query("""
            select distinct link.userId
            from UserReportingUserLink link
            where link.reportsToUserId in :managerIds
              and link.isActive = true
              and link.isDeleted = false
              and link.userId in (
                  select user.id from User user
                  where user.isActive = true and user.isDeleted = false
              )
            """)
    Set<Long> findActiveDirectReportIds(@Param("managerIds") Collection<Long> managerIds);

    List<UserReportingUserLink> findByUserIdOrReportsToUserId(Long userId, Long reportsToUserId);
}
