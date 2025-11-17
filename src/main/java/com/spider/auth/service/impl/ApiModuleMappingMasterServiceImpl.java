package com.spider.auth.service.impl;

import com.spider.auth.model.ApiMaster;
import com.spider.auth.model.ApiModuleMappingMaster;
import com.spider.auth.model.ModuleMaster;
import com.spider.auth.repository.ApiMasterRepository;
import com.spider.auth.repository.ApiModuleMappingMasterRepository;
import com.spider.auth.repository.ModuleMasterRepository;
import com.spider.auth.request.ApiModuleMappingMasterRequest;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.response.ApiModuleMappingMasterResponse;
import com.spider.auth.service.ApiModuleMappingMasterService;
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
public class ApiModuleMappingMasterServiceImpl extends ParentServiceImpl<ApiModuleMappingMaster,Long> implements ApiModuleMappingMasterService {

    @Autowired
    private ApiModuleMappingMasterRepository repository;
    @Autowired
    private CriteriaUtil<ApiModuleMappingMaster> criteriaUtil;

    @Autowired
    private ApiMasterRepository apiMasterRepository;
    @Autowired
    private ModuleMasterRepository moduleMasterRepository;


    @Override
    protected ParentRepository<ApiModuleMappingMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<ApiModuleMappingMaster> getCriteriaUtil() {
        return criteriaUtil;
    }



    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId) {

        ApiModuleMappingMasterRequest request = objectMapper.convertValue(commonRequest, ApiModuleMappingMasterRequest.class);
        ApiMaster apiMaster = apiMasterRepository.findOneActiveByUUIDOptional(request.getApiId()).orElseThrow(() -> new ValidationException("API not found"));
        ModuleMaster moduleMaster = moduleMasterRepository.findOneActiveByUUIDOptional(request.getModuleId()).orElseThrow(() -> new ValidationException("Module not found"));
        ApiModuleMappingMaster master = new ApiModuleMappingMaster();
        master.setApiMaster(apiMaster);
        master.setModuleMaster(moduleMaster);
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Created the API",objectMapper.convertValue(master, ApiModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid) {
        ApiModuleMappingMaster master = getRepository().findOneActiveByUUID(uuid);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, ApiModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId) {
        ApiModuleMappingMaster master = getRepository().findOneActiveByUUID(uuid);
        if(master == null){
            throw new ValidationException("No Data found!");
        }

        ApiModuleMappingMasterRequest newObj = objectMapper.convertValue(commonRequest, ApiModuleMappingMasterRequest.class);
        ApiMaster apiMaster = apiMasterRepository.findOneActiveByUUIDOptional(newObj.getApiId()).orElseThrow(() -> new ValidationException("API not found"));
        ModuleMaster moduleMaster = moduleMasterRepository.findOneActiveByUUIDOptional(newObj.getModuleId()).orElseThrow(() -> new ValidationException("Module not found"));
        master.setApiMaster(apiMaster);
        master.setModuleMaster(moduleMaster);
        master.setApiMaster(apiMaster);
        master.setModuleMaster(moduleMaster);
        master.setUuid(uuid);
        master.setId(master.getId());
        master.setCreatedAt(master.getCreatedAt());
        master.setCreatedBy(master.getCreatedBy());
        master.setUpdatedBy(userId);
        master.setIsActive(master.getIsActive());
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Updated the API",objectMapper.convertValue(newObj, ApiModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId) {
        ApiModuleMappingMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the API",objectMapper.convertValue(oneActiveByUUID, ApiModuleMappingMasterResponse.class));
    }


}
