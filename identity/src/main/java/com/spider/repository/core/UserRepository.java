package com.spider.repository.core;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.core.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository  extends ParentRepository<User,Long> {

    User findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<User> findByEmailIn(Collection<String> emails);

    @Query("""
            select user
            from User user
            where user.isActive = true
              and user.isDeleted = false
              and user.id in (
                  select link.userId
                  from UserLocationLink link
                  where link.locationId = :locationId
                    and link.isActive = true
                    and link.isDeleted = false
              )
            order by lower(user.fname), lower(user.lname), user.id
            """)
    List<User> findActiveUsersByLocationId(@Param("locationId") Long locationId);

    @Query("""
            select user
            from User user
            where user.isActive = true
              and user.isDeleted = false
              and user.id in (
                  select link.userId
                  from UserRoleLink link
                  where link.roleId = :roleId
                    and link.isActive = true
                    and link.isDeleted = false
              )
            order by lower(user.fname), lower(user.lname), user.id
            """)
    List<User> findActiveUsersByRoleId(@Param("roleId") Long roleId);
}
