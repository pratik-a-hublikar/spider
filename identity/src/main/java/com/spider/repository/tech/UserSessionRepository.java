package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.UserSession;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserSessionRepository  extends ParentRepository<UserSession,Long> {

    List<UserSession> findByUserIdAndIsActive(Long userId, boolean active);

    List<UserSession> findByIdAndUserIdAndIsActive(Long id, long userId, boolean b);
    List<UserSession> findByEmailAndIsActive(String email, boolean b);

    List<UserSession> findByEmailAndUserIdAndIsActive(String email, long userId, boolean b);
    Optional<UserSession> findOneByTokenAndIsActive(String email, boolean b);

}
