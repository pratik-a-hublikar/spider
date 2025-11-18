package com.spider.auth.service.impl;

import com.spider.auth.model.RoleMaster;
import com.spider.auth.model.UserMaster;
import com.spider.auth.model.UserRoleMappingMaster;
import com.spider.auth.repository.RoleMasterRepository;
import com.spider.auth.repository.UserMasterRepository;
import com.spider.auth.repository.UserRoleMappingMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.request.UserRoleMappingMasterRequest;
import com.spider.auth.response.UserRoleMappingMasterResponse;
import com.spider.auth.service.UserRoleMappingMasterService;
import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.response.CommonResponse;
import com.spider.common.service.impl.ParentServiceImpl;
import com.spider.common.utils.CriteriaUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class UserRoleMappingMasterServiceImpl extends ParentServiceImpl<UserRoleMappingMaster,Long> implements UserRoleMappingMasterService {


    private final UserRoleMappingMasterRepository repository;
    private final CriteriaUtil<UserRoleMappingMaster> criteriaUtil;
    private final RoleMasterRepository roleMasterRepository;
    private final UserMasterRepository userMasterRepository;

    @Autowired
    public UserRoleMappingMasterServiceImpl(UserRoleMappingMasterRepository repository,
                                            CriteriaUtil<UserRoleMappingMaster> criteriaUtil,
                                            RoleMasterRepository roleMasterRepository,
                                            UserMasterRepository userMasterRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.roleMasterRepository = roleMasterRepository;
        this.userMasterRepository = userMasterRepository;
    }



    @Override
    protected ParentRepository<UserRoleMappingMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<UserRoleMappingMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {
        UserRoleMappingMasterRequest request = objectMapper.convertValue(commonRequest, UserRoleMappingMasterRequest.class);
        RoleMaster roleMaster = roleMasterRepository.findOneActiveByUUIDOptional(request.getRoleId(),orgId).orElseThrow(() -> new ValidationException("Role not found"));
        UserMaster userMaster = userMasterRepository.findOneActiveByUUIDOptional(request.getUserId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        //TODO: We may add validation to check if both role and user belongs to same department or not
        UserRoleMappingMaster master = new UserRoleMappingMaster();
        master.setUserMaster(userMaster);
        master.setRoleMaster(roleMaster);
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Created the Role User mapping",objectMapper.convertValue(master, UserRoleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        UserRoleMappingMaster master = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, UserRoleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId,Long orgId) {
        UserRoleMappingMaster master = getRepository().findOneActiveByUUID(uuid,orgId);
        if(master == null){
            throw new ValidationException("No Data found!");
        }


        UserRoleMappingMasterRequest request = objectMapper.convertValue(commonRequest, UserRoleMappingMasterRequest.class);
        RoleMaster roleMaster = roleMasterRepository.findOneActiveByUUIDOptional(request.getRoleId(),orgId).orElseThrow(() -> new ValidationException("Role not found"));
        UserMaster userMaster = userMasterRepository.findOneActiveByUUIDOptional(request.getUserId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        //TODO: We may add validation to check if both role and user belongs to same department or not
        master.setUserMaster(userMaster);
        master.setRoleMaster(roleMaster);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Updated the Role Department mapping",objectMapper.convertValue(master, UserRoleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        UserRoleMappingMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the Role User mapping",objectMapper.convertValue(oneActiveByUUID, UserRoleMappingMasterResponse.class));
    }


}
