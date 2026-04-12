package com.spider.auth.service.impl;

import com.spider.auth.enums.ACCESS_TYPE;
import com.spider.auth.model.AccessTypeMaster;
import com.spider.auth.model.RoleMaster;
import com.spider.auth.model.UserMaster;
import com.spider.auth.model.view.UserRoleMappingView;
import com.spider.auth.repository.AccessTypeMasterRepository;
import com.spider.auth.repository.RoleMasterRepository;
import com.spider.auth.repository.UserMasterRepository;
import com.spider.auth.repository.view.UserRoleMappingViewRepository;
import com.spider.auth.request.ApiModuleMappingMasterRequest;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.response.ApiModuleMappingMasterResponse;
import com.spider.auth.service.UserRoleMappingViewService;
import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.response.CommonResponse;
import com.spider.common.service.impl.ParentServiceImpl;
import com.spider.common.utils.CriteriaUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class UserRoleMappingViewServiceImpl extends ParentServiceImpl<UserRoleMappingView,Long> implements UserRoleMappingViewService {


    private final UserRoleMappingViewRepository repository;
    private final CriteriaUtil<UserRoleMappingView> criteriaUtil;
    private final RoleMasterRepository roleMasterRepository;
    private final UserMasterRepository userMasterRepository;
    private final AccessTypeMasterRepository accessTypeMasterRepository;

    @Autowired
    public UserRoleMappingViewServiceImpl(UserRoleMappingViewRepository repository,
                                          CriteriaUtil<UserRoleMappingView> criteriaUtil,
                                          RoleMasterRepository roleMasterRepository,
                                          UserMasterRepository userMasterRepository,
                                          AccessTypeMasterRepository accessTypeMasterRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.roleMasterRepository = roleMasterRepository;
        this.userMasterRepository = userMasterRepository;
        this.accessTypeMasterRepository = accessTypeMasterRepository;
    }



    @Override
    protected ParentRepository<UserRoleMappingView, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<UserRoleMappingView> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {

        ApiModuleMappingMasterRequest request = objectMapper.convertValue(commonRequest, ApiModuleMappingMasterRequest.class);
        UserMaster userMaster = userMasterRepository.findOneActiveByUUIDOptional(request.getApiId(),orgId).orElseThrow(() -> new ValidationException("Role Group not found"));
        RoleMaster roleMaster = roleMasterRepository.findOneActiveByUUIDOptional(request.getModuleId(),orgId).orElseThrow(() -> new ValidationException("Role not found"));
        Optional<AccessTypeMaster> accessTypeMaster = accessTypeMasterRepository.findOneByEntityIdAndAccessTypeAndRefIdAndOrgId(userMaster.getId(),roleMaster.getId(), ACCESS_TYPE.USER_ROLE,orgId);
        AccessTypeMaster master = accessTypeMaster.orElseGet(AccessTypeMaster::new);
        master.setEntityId(userMaster.getId());
        master.setRefId(roleMaster.getId());
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master.setIsActive(true);
        master.setIsDeleted(false);
        master = accessTypeMasterRepository.save(master);
        return CommonPayLoad.of("Successfully Created the API",objectMapper.convertValue(master, ApiModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        UserRoleMappingView master = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, ApiModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        AccessTypeMaster oneActiveByUUID = accessTypeMasterRepository.findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = accessTypeMasterRepository.save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the Role Group to Role relationship",objectMapper.convertValue(oneActiveByUUID, ApiModuleMappingMasterResponse.class));
    }



}
