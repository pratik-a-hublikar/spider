package com.spider.auth.service.impl;

import com.spider.auth.exception.AuthenticationException;
import com.spider.auth.model.ApiMaster;
import com.spider.auth.model.UserMaster;
import com.spider.auth.repository.UserMasterRepository;
import com.spider.auth.request.AuthRequest;
import com.spider.auth.response.UserMasterResponse;
import com.spider.auth.service.UserMasterService;
import com.spider.common.AppConstants;
import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.response.CommonResponse;
import com.spider.common.service.impl.ParentServiceImpl;
import com.spider.common.utils.CriteriaUtil;
import com.spider.common.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
public class UserMasterServiceImpl extends ParentServiceImpl<UserMaster,Long> implements UserMasterService {

    private final UserMasterRepository repository;
    private final CriteriaUtil<UserMaster> criteriaUtil;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserMasterServiceImpl(UserMasterRepository repository,
                                 CriteriaUtil<UserMaster> criteriaUtil,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenUtil jwtTokenUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected ParentRepository<UserMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<UserMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public boolean hasAuthorization(String uri, String method, String uuid) {
        log.info("Started validating the user uuid:{} access for URI:{}, method:{}",uuid , uri, method);
        UserMaster userMaster = repository.findOneActiveByUUID(uuid);
        if(null == userMaster){
            return false;
        }
        if(userMaster.getIsSuperAdmin()) {
            return true;
        }else if(uri.startsWith("/master")){
            return false;
        }
        Optional<ApiMaster> first = userMaster.getDepartmentMasters().stream()
                .filter(p -> !CollectionUtils.isEmpty(p.getModuleMaster()))
                .flatMap(p -> p.getModuleMaster().stream())
                .flatMap(p->p.getApiMasterList().stream())
                .filter(Objects::nonNull)
                .filter(p -> p.getPath().equalsIgnoreCase(uri) && method.equalsIgnoreCase(p.getMethod()))
                .findFirst();

        return first.isPresent();
    }

    @Override
    public CommonPayLoad<String> validateCredentials(AuthRequest authRequest,
                                                     HttpServletResponse response) {

        UserMaster userMaster = repository.findOneByEmailAndIsActiveAndIsDeleted(authRequest.getUsername(),true,false);
        if(userMaster == null ||
                !passwordEncoder.matches(authRequest.getPassword(),userMaster.getPassword())){
            throw new AuthenticationException(AppConstants.LOGIN_FAILED);}
        String authToken = jwtTokenUtil.encode(userMaster.getUuid(), AppConstants.LOGIN);
        response.setHeader(AppConstants.AUTHORIZATION,authToken);
        return CommonPayLoad.of(AppConstants.LOGIN_SUCCESS);
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid) {
        UserMaster master = getRepository().findOneActiveByUUID(uuid);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, UserMasterResponse.class));
    }



    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId) {
        UserMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the user",objectMapper.convertValue(oneActiveByUUID, UserMasterResponse.class));
    }

}

