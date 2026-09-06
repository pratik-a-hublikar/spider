package com.spider.repository.core;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.core.UserLocationLink;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Optional;
@Repository
public interface UserLocationLinkRepository  extends ParentRepository<UserLocationLink,Long> {

    @Query("""
            select link.locationId
            from UserLocationLink link
            where link.userId = :userId
              and link.isActive = true
              and link.isDeleted = false
              and link.locationId in (
                  select location.id
                  from Location location
                  where location.isActive = true
                    and location.isDeleted = false
              )
            """)
    Set<Long> findActiveLocationIdsByUserId(@Param("userId") Long userId);

    @Query("""
            select distinct link.userId
            from UserLocationLink link
            where link.locationId in :locationIds
              and link.isActive = true
              and link.isDeleted = false
            """)
    Set<Long> findActiveUserIdsByLocationIds(
            @Param("locationIds") Collection<Long> locationIds);

    List<UserLocationLink> findByUserId(Long userId);

    Optional<UserLocationLink> findByUserIdAndLocationIdAndIsActiveAndIsDeleted(
            Long userId, Long locationId, boolean active, boolean deleted);

    @Query("""
            select link.locationId, count(distinct link.userId)
            from UserLocationLink link
            where link.locationId in :locationIds
              and link.isActive = true
              and link.isDeleted = false
              and link.userId in (
                  select user.id
                  from User user
                  where user.isActive = true
                    and user.isDeleted = false
              )
            group by link.locationId
            """)
    List<Object[]> countActiveUsersByLocationIds(@Param("locationIds") Collection<Long> locationIds);
}
