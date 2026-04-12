package com.spider.auth.service.impl;

import com.spider.auth.model.ApiMaster;
import com.spider.auth.repository.ApiMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.response.ApiMasterResponse;
import com.spider.auth.service.ApiMasterService;
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
public class ApiMasterServiceImpl extends ParentServiceImpl<ApiMaster,Long> implements ApiMasterService {

    private final ApiMasterRepository repository;
    private final CriteriaUtil<ApiMaster> criteriaUtil;

    @Autowired
    public ApiMasterServiceImpl(ApiMasterRepository repository, CriteriaUtil<ApiMaster> criteriaUtil) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
    }

    @Override
    protected ParentRepository<ApiMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<ApiMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {
        ApiMaster apiMaster = objectMapper.convertValue(commonRequest, ApiMaster.class);
        apiMaster.setCreatedBy(userId);
        apiMaster.setUpdatedBy(userId);
        apiMaster.setOrgId(orgId);
        apiMaster = getRepository().save(apiMaster);
        return CommonPayLoad.of("Successfully Created the API",objectMapper.convertValue(apiMaster, ApiMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid,Long orgId) {
        ApiMaster apiMaster = getRepository().findOneActiveByUUID(uuid,orgId);
        return CommonPayLoad.of("Success",objectMapper.convertValue(apiMaster, ApiMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId,Long orgId) {
        ApiMaster apiMaster = getRepository().findOneActiveByUUID(uuid,orgId);
        if(apiMaster == null){
            throw new ValidationException("No Data found!");
        }
        ApiMaster newObj = objectMapper.convertValue(commonRequest, ApiMaster.class);
        newObj.setUuid(uuid);
        newObj.setId(apiMaster.getId());
        newObj.setCreatedAt(apiMaster.getCreatedAt());
        newObj.setCreatedBy(apiMaster.getCreatedBy());
        newObj.setUpdatedBy(userId);
        newObj.setIsActive(apiMaster.getIsActive());
        newObj.setOrgId(orgId);
        newObj = getRepository().save(newObj);
        return CommonPayLoad.of("Successfully Updated the API",objectMapper.convertValue(newObj, ApiMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId) {
        ApiMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid,orgId);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the API",objectMapper.convertValue(oneActiveByUUID, ApiMasterResponse.class));
    }


    @Override
    public ApiMaster findOneByUriAndMethod(String uri, String method) {
        Optional<ApiMaster> apiMaster = repository.findOneByUriAndMethod(uri, method);
        if(apiMaster.isEmpty()){
            log.error("No API found for the URI: {} and Method: {}",uri,method);
            throw new ValidationException("No Data found for the API in teh Database");
        }
        return apiMaster.get();
    }
}
