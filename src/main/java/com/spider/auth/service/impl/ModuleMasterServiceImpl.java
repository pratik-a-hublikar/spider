package com.spider.auth.service.impl;

import com.spider.auth.model.ModuleMaster;
import com.spider.auth.repository.ModuleMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.request.ModuleMasterRequest;
import com.spider.auth.response.ModuleMasterResponse;
import com.spider.auth.service.ModuleMasterService;
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
public class ModuleMasterServiceImpl extends ParentServiceImpl<ModuleMaster,Long> implements ModuleMasterService {

    private final ModuleMasterRepository repository;
    private final CriteriaUtil<ModuleMaster> criteriaUtil;

    @Autowired
    public ModuleMasterServiceImpl(ModuleMasterRepository repository,
                                   CriteriaUtil<ModuleMaster> criteriaUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
    }

    @Override
    protected ParentRepository<ModuleMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<ModuleMaster> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest,String userId,Long orgId) {
        ModuleMasterRequest newObj = objectMapper.convertValue(commonRequest, ModuleMasterRequest.class);
        ModuleMaster parentModule = getRepository().findOneActiveByUUIDOptional(newObj.getParentId(),orgId).orElseThrow(() -> new ValidationException("Parent module not found"));
        ModuleMaster moduleMaster = new ModuleMaster();
        moduleMaster.setModuleName(newObj.getModuleName());
        moduleMaster.setDescription(newObj.getDescription());
        moduleMaster.setParentId(parentModule.getId());
        moduleMaster.setCreatedBy(userId);
        moduleMaster.setUpdatedBy(userId);
        moduleMaster.setOrgId(orgId);
        moduleMaster = getRepository().save(moduleMaster);
        return CommonPayLoad.of("Successfully Created the module",objectMapper.convertValue(moduleMaster, ModuleMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        ModuleMaster moduleMaster = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(moduleMaster, ModuleMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId,Long orgId) {
        ModuleMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        ModuleMasterRequest newObj = objectMapper.convertValue(commonRequest, ModuleMasterRequest.class);
        ModuleMaster moduleMaster = getRepository().findOneActiveByUUIDOptional(newObj.getParentId(),orgId).orElseThrow(() -> new ValidationException("Parent module not found"));
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID.setOrgId(orgId);
        oneActiveByUUID.setParentId(moduleMaster.getId());
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Updated the module",objectMapper.convertValue(oneActiveByUUID, ModuleMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        ModuleMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        boolean exists = repository.existsByParentIdAndIsActiveAndIsDeleted(oneActiveByUUID.getId(), true, false);
        if(exists){
            throw new ValidationException("Can not directly delete a parent module in which active child modules exists.");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the module",objectMapper.convertValue(oneActiveByUUID, ModuleMasterResponse.class));
    }
}
