package com.spider.auth.service.impl;

import com.spider.auth.model.RoleMaster;
import com.spider.auth.repository.RoleMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.response.RoleMasterResponse;
import com.spider.auth.service.RoleMasterService;
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
public class RoleMasterServiceImpl extends ParentServiceImpl<RoleMaster,Long> implements RoleMasterService {

    private final RoleMasterRepository repository;
    private final CriteriaUtil<RoleMaster> criteriaUtil;
    @Autowired
    public RoleMasterServiceImpl(RoleMasterRepository repository,
                                 CriteriaUtil<RoleMaster> criteriaUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
    }

    @Override
    protected ParentRepository<RoleMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<RoleMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {
        RoleMaster apiMaster = objectMapper.convertValue(commonRequest, RoleMaster.class);
        apiMaster.setCreatedBy(userId);
        apiMaster.setUpdatedBy(userId);
        apiMaster.setOrgId(orgId);
        apiMaster = getRepository().save(apiMaster);
        return CommonPayLoad.of("Successfully Created the Role",objectMapper.convertValue(apiMaster, RoleMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        RoleMaster roleMaster = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(roleMaster, RoleMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId,Long orgId) {
        RoleMaster roleMaster = getRepository().findOneActiveByUUID(uuid,orgId);
        if(roleMaster == null){
            throw new ValidationException("No Data found!");
        }
        RoleMaster newObj = objectMapper.convertValue(commonRequest, RoleMaster.class);
        newObj.setUuid(uuid);
        newObj.setId(roleMaster.getId());
        newObj.setCreatedAt(roleMaster.getCreatedAt());
        newObj.setCreatedBy(roleMaster.getCreatedBy());
        newObj.setUpdatedBy(userId);
        newObj.setIsActive(roleMaster.getIsActive());
        newObj.setOrgId(orgId);
        newObj = getRepository().save(newObj);
        return CommonPayLoad.of("Successfully Updated the Role",objectMapper.convertValue(newObj, RoleMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        RoleMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the Role",objectMapper.convertValue(oneActiveByUUID, RoleMasterResponse.class));
    }


}
