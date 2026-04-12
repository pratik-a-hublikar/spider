package com.spider.auth.service.impl;

import com.spider.auth.enums.ACCESS_TYPE;
import com.spider.auth.model.AccessTypeMaster;
import com.spider.auth.model.ApiMaster;
import com.spider.auth.model.RoleMaster;
import com.spider.auth.model.view.RoleModuleMappingView;
import com.spider.auth.repository.AccessTypeMasterRepository;
import com.spider.auth.repository.ApiMasterRepository;
import com.spider.auth.repository.RoleMasterRepository;
import com.spider.auth.repository.view.RoleModuleMappingViewRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.request.RoleDeptMappingMasterRequest;
import com.spider.auth.response.RoleDepartmentMappingMasterResponse;
import com.spider.auth.service.RoleModuleMappingViewService;
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
public class RoleModuleMappingViewServiceImpl extends ParentServiceImpl<RoleModuleMappingView,Long> implements RoleModuleMappingViewService {

    private final RoleModuleMappingViewRepository repository;
    private final CriteriaUtil<RoleModuleMappingView> criteriaUtil;

    private final RoleMasterRepository roleMasterRepository;

    private final ApiMasterRepository apiMasterRepository;
    private final AccessTypeMasterRepository accessTypeMasterRepository;

    @Autowired
    public RoleModuleMappingViewServiceImpl(RoleModuleMappingViewRepository repository,
                                            CriteriaUtil<RoleModuleMappingView> criteriaUtil,
                                            RoleMasterRepository roleMasterRepository,
                                            ApiMasterRepository apiMasterRepository,
                                            AccessTypeMasterRepository accessTypeMasterRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.roleMasterRepository = roleMasterRepository;
        this.apiMasterRepository = apiMasterRepository;
        this.accessTypeMasterRepository = accessTypeMasterRepository;
    }

    @Override
    protected ParentRepository<RoleModuleMappingView, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<RoleModuleMappingView> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {
        RoleDeptMappingMasterRequest request = objectMapper.convertValue(commonRequest, RoleDeptMappingMasterRequest.class);
        RoleMaster roleMaster = roleMasterRepository.findOneActiveByUUIDOptional(request.getRoleId(),orgId).orElseThrow(() -> new ValidationException("User not found"));
        ApiMaster apiMaster = apiMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId(),orgId).orElseThrow(() -> new ValidationException("Department not found"));
        Optional<AccessTypeMaster> accessTypeMaster = accessTypeMasterRepository.findOneByEntityIdAndAccessTypeAndRefIdAndOrgId(roleMaster.getId(),apiMaster.getId(), ACCESS_TYPE.ROLE_MODULE,orgId);
        AccessTypeMaster master = accessTypeMaster.orElseGet(AccessTypeMaster::new);
        master.setEntityId(roleMaster.getId());
        master.setRefId(apiMaster.getId());
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master.setOrgId(orgId);
        master.setIsActive(true);
        master.setIsDeleted(false);
        master = accessTypeMasterRepository.save(master);
        return CommonPayLoad.of("Successfully Created the Role Department mapping",objectMapper.convertValue(master, RoleDepartmentMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        RoleModuleMappingView master = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, RoleDepartmentMappingMasterResponse.class));
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
        return CommonPayLoad.of("Successfully Deleted the Role Module mapping",objectMapper.convertValue(oneActiveByUUID, RoleDepartmentMappingMasterResponse.class));
    }


}
