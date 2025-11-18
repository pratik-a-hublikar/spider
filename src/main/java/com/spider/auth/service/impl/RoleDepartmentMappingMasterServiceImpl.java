package com.spider.auth.service.impl;

import com.spider.auth.model.DepartmentMaster;
import com.spider.auth.model.RoleDepartmentMappingMaster;
import com.spider.auth.model.RoleMaster;
import com.spider.auth.repository.DepartmentMasterRepository;
import com.spider.auth.repository.RoleDepartmentMappingMasterRepository;
import com.spider.auth.repository.RoleMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.request.RoleDeptMappingMasterRequest;
import com.spider.auth.response.RoleDepartmentMappingMasterResponse;
import com.spider.auth.service.RoleDepartmentMappingMasterService;
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
public class RoleDepartmentMappingMasterServiceImpl extends ParentServiceImpl<RoleDepartmentMappingMaster,Long> implements RoleDepartmentMappingMasterService {

    private final RoleDepartmentMappingMasterRepository repository;
    private final CriteriaUtil<RoleDepartmentMappingMaster> criteriaUtil;

    private final RoleMasterRepository roleMasterRepository;

    private final DepartmentMasterRepository departmentMasterRepository;

    @Autowired
    public RoleDepartmentMappingMasterServiceImpl(RoleDepartmentMappingMasterRepository repository,
                                                  CriteriaUtil<RoleDepartmentMappingMaster> criteriaUtil,
                                                  RoleMasterRepository roleMasterRepository,
                                                  DepartmentMasterRepository departmentMasterRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.roleMasterRepository = roleMasterRepository;
        this.departmentMasterRepository = departmentMasterRepository;
    }

    @Override
    protected ParentRepository<RoleDepartmentMappingMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<RoleDepartmentMappingMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {
        RoleDeptMappingMasterRequest request = objectMapper.convertValue(commonRequest, RoleDeptMappingMasterRequest.class);
        RoleMaster roleMaster = roleMasterRepository.findOneActiveByUUIDOptional(request.getRoleId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        DepartmentMaster departmentMaster = departmentMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId(),orgId).orElseThrow(() -> new ValidationException("Department not found"));
        RoleDepartmentMappingMaster master = new RoleDepartmentMappingMaster();
        master.setDepartmentMaster(departmentMaster);
        master.setRoleMaster(roleMaster);
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Created the Role Department mapping",objectMapper.convertValue(master, RoleDepartmentMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        RoleDepartmentMappingMaster master = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, RoleDepartmentMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId,Long orgId) {
        RoleDepartmentMappingMaster master = getRepository().findOneActiveByUUID(uuid,orgId);
        if(master == null){
            throw new ValidationException("No Data found!");
        }


        RoleDeptMappingMasterRequest request = objectMapper.convertValue(commonRequest, RoleDeptMappingMasterRequest.class);
        RoleMaster roleMaster = roleMasterRepository.findOneActiveByUUIDOptional(request.getRoleId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        DepartmentMaster departmentMaster = departmentMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId(),orgId).orElseThrow(() -> new ValidationException("Department not found"));
        master.setDepartmentMaster(departmentMaster);
        master.setRoleMaster(roleMaster);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Updated the Role Department mapping",objectMapper.convertValue(master, RoleDepartmentMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        RoleDepartmentMappingMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the Role Department mapping",objectMapper.convertValue(oneActiveByUUID, RoleDepartmentMappingMasterResponse.class));
    }


}
