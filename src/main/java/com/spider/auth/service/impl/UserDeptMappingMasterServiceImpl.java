package com.spider.auth.service.impl;

import com.spider.auth.model.DepartmentMaster;
import com.spider.auth.model.UserDeptMappingMaster;
import com.spider.auth.model.UserMaster;
import com.spider.auth.repository.DepartmentMasterRepository;
import com.spider.auth.repository.UserDeptMappingMasterRepository;
import com.spider.auth.repository.UserMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.request.UserDeptMappingMasterRequest;
import com.spider.auth.response.UserDeptMappingMasterResponse;
import com.spider.auth.service.UserDeptMappingMasterService;
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
public class UserDeptMappingMasterServiceImpl extends ParentServiceImpl<UserDeptMappingMaster,Long> implements UserDeptMappingMasterService {

    private final UserDeptMappingMasterRepository repository;
    private final CriteriaUtil<UserDeptMappingMaster> criteriaUtil;
    private final UserMasterRepository userMasterRepository;
    private final DepartmentMasterRepository departmentMasterRepository;

    @Autowired
    public UserDeptMappingMasterServiceImpl(UserDeptMappingMasterRepository repository,
                                            CriteriaUtil<UserDeptMappingMaster> criteriaUtil,
                                            UserMasterRepository userMasterRepository,
                                            DepartmentMasterRepository departmentMasterRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.userMasterRepository = userMasterRepository;
        this.departmentMasterRepository = departmentMasterRepository;
    }

    @Override
    protected ParentRepository<UserDeptMappingMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<UserDeptMappingMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {
        UserDeptMappingMasterRequest request = objectMapper.convertValue(commonRequest, UserDeptMappingMasterRequest.class);
        UserMaster userMaster = userMasterRepository.findOneActiveByUUIDOptional(request.getUserId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        DepartmentMaster departmentMaster = departmentMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId(),orgId).orElseThrow(() -> new ValidationException("Department not found"));
        UserDeptMappingMaster master = new UserDeptMappingMaster();
        master.setDepartmentMaster(departmentMaster);
        master.setUserMaster(userMaster);
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Created the Dept Module Relationship",objectMapper.convertValue(master, UserDeptMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        UserDeptMappingMaster master = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, UserDeptMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId,Long orgId) {
        UserDeptMappingMaster master = getRepository().findOneActiveByUUID(uuid,orgId);
        if(master == null){
            throw new ValidationException("No Data found!");
        }
        UserDeptMappingMasterRequest request = objectMapper.convertValue(commonRequest, UserDeptMappingMasterRequest.class);
        UserMaster userMaster = userMasterRepository.findOneActiveByUUIDOptional(request.getUserId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        DepartmentMaster departmentMaster = departmentMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId(),orgId).orElseThrow(() -> new ValidationException("Department not found"));
        master.setDepartmentMaster(departmentMaster);
        master.setUserMaster(userMaster);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Updated the Dept Module Relationship",objectMapper.convertValue(master, UserDeptMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        UserDeptMappingMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID.setOrgId(orgId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the Dept Module Relationship",objectMapper.convertValue(oneActiveByUUID, UserDeptMappingMasterResponse.class));
    }


}
