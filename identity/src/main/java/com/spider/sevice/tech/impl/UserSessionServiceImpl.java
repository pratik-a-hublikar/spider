package com.spider.sevice.tech.impl;


import com.spider.common.dto.UserSessionDTO;
import com.spider.common.repository.ParentRepository;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.User;
import com.spider.enity.tech.UserSession;
import com.spider.repository.tech.UserSessionRepository;
import com.spider.repository.core.UserRepository;
import com.spider.sevice.tech.UserSessionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Log4j2
@Service
public class UserSessionServiceImpl extends CommonServiceImpl<UserSession,Long> implements UserSessionService {

    private final UserSessionRepository repository;
    private final CriteriaUtil<UserSession> criteriaUtil;
    private final UserRepository userRepository;
    @Autowired
    public UserSessionServiceImpl(UserSessionRepository repository,
                                  CriteriaUtil<UserSession> criteriaUtil,
                                  UserRepository userRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.userRepository = userRepository;
    }


    @Override
    protected ParentRepository<UserSession, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<UserSession> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public UserSessionDTO findOneByTokenAndActive(String token) {
        Optional<UserSession> oneByTokenAndIsActive = repository.findOneByTokenAndIsActive(token, true);
        if(oneByTokenAndIsActive.isPresent()){
            UserSession session = oneByTokenAndIsActive.get();
            UserSessionDTO sessionDTO = new UserSessionDTO();
            sessionDTO.setUuid(token);
            sessionDTO.setUserId(session.getUserId());
            sessionDTO.setEmail(session.getEmail());
            sessionDTO.setLoginDate(session.getLoginDate());
            sessionDTO.setTimeout(session.getTimeout()); // minutes
            sessionDTO.setStayLoggedIn(session.isStayLoggedIn());
            User user = userRepository.findOneActiveById(session.getUserId());
            sessionDTO.setSuperAdmin(user != null && user.isSuperAdmin());
            return sessionDTO;
        }
        return null;
    }
}
