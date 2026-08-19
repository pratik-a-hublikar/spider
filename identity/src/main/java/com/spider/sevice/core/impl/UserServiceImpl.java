package com.spider.dao.sevice.core.impl;


import com.spider.common.repository.ParentRepository;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.dao.enity.core.User;
import com.spider.dao.repository.core.UserRepository;
import com.spider.dao.sevice.core.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class UserServiceImpl extends CommonServiceImpl<User,Long> implements UserService {

    private final UserRepository userRepository;
    private final CriteriaUtil<User> criteriaUtil;
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CriteriaUtil<User> criteriaUtil) {
        this.userRepository = userRepository;
        this.criteriaUtil = criteriaUtil;
    }


    @Override
    protected ParentRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected CriteriaUtil<User> getCriteriaUtil() {
        return criteriaUtil;
    }
}
