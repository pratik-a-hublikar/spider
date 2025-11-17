package com.spider.auth.service.impl;

import com.spider.auth.model.DepartmentMaster;
import com.spider.auth.repository.DepartmentMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.response.DepartmentMasterResponse;
import com.spider.auth.service.DepartmentMasterService;
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
public class DepartmentMasterServiceImpl extends ParentServiceImpl<DepartmentMaster,Long> implements DepartmentMasterService {

    @Autowired
    private DepartmentMasterRepository repository;
    @Autowired
    private CriteriaUtil<DepartmentMaster> criteriaUtil;


    @Override
    protected ParentRepository<DepartmentMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<DepartmentMaster> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId) {
        DepartmentMaster departmentMaster = objectMapper.convertValue(commonRequest, DepartmentMaster.class);
        departmentMaster.setCreatedBy(userId);
        departmentMaster.setUpdatedBy(userId);
        departmentMaster = getRepository().save(departmentMaster);
        return CommonPayLoad.of("Successfully Created the Department",objectMapper.convertValue(departmentMaster, DepartmentMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid) {
        DepartmentMaster departmentMaster = getRepository().findOneActiveByUUID(uuid);
        return CommonPayLoad.of("Success",objectMapper.convertValue(departmentMaster, DepartmentMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId) {
        DepartmentMaster departmentMaster = getRepository().findOneActiveByUUID(uuid);
        if(departmentMaster == null){
            throw new ValidationException("No Data found!");
        }
        DepartmentMaster newObj = objectMapper.convertValue(commonRequest, DepartmentMaster.class);
        newObj.setUuid(uuid);
        newObj.setId(departmentMaster.getId());
        newObj.setCreatedAt(departmentMaster.getCreatedAt());
        newObj.setCreatedBy(departmentMaster.getCreatedBy());
        newObj.setUpdatedBy(userId);
        newObj.setIsActive(departmentMaster.getIsActive());
        newObj = getRepository().save(newObj);
        return CommonPayLoad.of("Successfully Updated the Department",objectMapper.convertValue(newObj, DepartmentMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId) {
        DepartmentMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the Department",objectMapper.convertValue(oneActiveByUUID, DepartmentMasterResponse.class));
    }

}
