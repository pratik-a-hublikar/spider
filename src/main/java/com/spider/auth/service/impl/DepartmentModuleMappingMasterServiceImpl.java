package com.spider.auth.service.impl;

import com.spider.auth.model.DepartmentMaster;
import com.spider.auth.model.DepartmentModuleMappingMaster;
import com.spider.auth.model.ModuleMaster;
import com.spider.auth.repository.DepartmentMasterRepository;
import com.spider.auth.repository.DepartmentModuleMappingMasterRepository;
import com.spider.auth.repository.ModuleMasterRepository;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.request.DepartmentModuleMappingMasterRequest;
import com.spider.auth.response.DepartmentModuleMappingMasterResponse;
import com.spider.auth.service.DepartmentModuleMappingMasterService;
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
public class DepartmentModuleMappingMasterServiceImpl extends ParentServiceImpl<DepartmentModuleMappingMaster,Long> implements DepartmentModuleMappingMasterService {

    @Autowired
    private DepartmentModuleMappingMasterRepository repository;
    @Autowired
    private CriteriaUtil<DepartmentModuleMappingMaster> criteriaUtil;


    @Autowired
    private DepartmentMasterRepository departmentMasterRepository;

    @Autowired
    private ModuleMasterRepository moduleMasterRepository;

    @Override
    protected ParentRepository<DepartmentModuleMappingMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<DepartmentModuleMappingMaster> getCriteriaUtil() {
        return criteriaUtil;
    }



    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId) {
        DepartmentModuleMappingMasterRequest request = objectMapper.convertValue(commonRequest, DepartmentModuleMappingMasterRequest.class);
        DepartmentMaster department = departmentMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId()).orElseThrow(() -> new ValidationException("Department not found"));
        ModuleMaster moduleMaster = moduleMasterRepository.findOneActiveByUUIDOptional(request.getModuleId()).orElseThrow(() -> new ValidationException("Module not found"));
        DepartmentModuleMappingMaster master = new DepartmentModuleMappingMaster();
        master.setCreatedBy(userId);
        master.setUpdatedBy(userId);
        master.setDepartmentMaster(department);
        master.setModuleMaster(moduleMaster);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Created the API",objectMapper.convertValue(master, DepartmentModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> get(String uuid) {
        DepartmentModuleMappingMaster master = getRepository().findOneActiveByUUID(uuid);
        return CommonPayLoad.of("Success",objectMapper.convertValue(master, DepartmentModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> update(String uuid, CommonRequest commonRequest, String userId) {
        DepartmentModuleMappingMaster master = getRepository().findOneActiveByUUID(uuid);
        if(master == null){
            throw new ValidationException("No Data found!");
        }

        DepartmentModuleMappingMasterRequest request = objectMapper.convertValue(commonRequest, DepartmentModuleMappingMasterRequest.class);
        DepartmentMaster department = departmentMasterRepository.findOneActiveByUUIDOptional(request.getDepartmentId()).orElseThrow(() -> new ValidationException("Department not found"));
        ModuleMaster moduleMaster = moduleMasterRepository.findOneActiveByUUIDOptional(request.getModuleId()).orElseThrow(() -> new ValidationException("Module not found"));
        master.setModuleMaster(moduleMaster);
        master.setDepartmentMaster(department);
        master.setUpdatedBy(userId);
        master = getRepository().save(master);
        return CommonPayLoad.of("Successfully Updated the Module Department mappings",objectMapper.convertValue(master, DepartmentModuleMappingMasterResponse.class));
    }

    @Override
    public CommonPayLoad<CommonResponse> softDelete(String uuid,String userId) {
        DepartmentModuleMappingMaster oneActiveByUUID = getRepository().findOneActiveByUUID(uuid);
        if(oneActiveByUUID == null){
            throw new ValidationException("No Data found!");
        }
        oneActiveByUUID.setIsDeleted(true);
        oneActiveByUUID.setUpdatedBy(userId);
        oneActiveByUUID = getRepository().save(oneActiveByUUID);
        return CommonPayLoad.of("Successfully Deleted the API",objectMapper.convertValue(oneActiveByUUID, DepartmentModuleMappingMasterResponse.class));
    }


}
