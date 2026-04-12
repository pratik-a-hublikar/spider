package com.spider.auth.service.impl;

import com.spider.auth.enums.ACCESS_TYPE;
import com.spider.auth.model.AccessTypeMaster;
import com.spider.auth.model.ApiMaster;
import com.spider.auth.model.ModuleMaster;
import com.spider.auth.model.view.ModuleAPIMappingView;
import com.spider.auth.repository.AccessTypeMasterRepository;
import com.spider.auth.repository.ApiMasterRepository;
import com.spider.auth.repository.ModuleMasterRepository;
import com.spider.auth.repository.view.ModuleAPIMappingViewRepository;
import com.spider.auth.request.ApiModuleMappingMasterRequest;
import com.spider.auth.request.CommonRequest;
import com.spider.auth.response.ApiModuleMappingMasterResponse;
import com.spider.auth.service.ModuleAPIMappingViewService;
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
public class ModuleAPIMappingViewServiceImpl extends ParentServiceImpl<ModuleAPIMappingView,Long> implements ModuleAPIMappingViewService {

    private final ModuleAPIMappingViewRepository repository;
    private final CriteriaUtil<ModuleAPIMappingView> criteriaUtil;

    private final ApiMasterRepository apiMasterRepository;
    private final ModuleMasterRepository moduleMasterRepository;

    private final AccessTypeMasterRepository accessTypeMasterRepository;
    @Autowired
    public ModuleAPIMappingViewServiceImpl(ModuleAPIMappingViewRepository repository,
                                           CriteriaUtil<ModuleAPIMappingView> criteriaUtil,
                                           ApiMasterRepository apiMasterRepository,
                                           ModuleMasterRepository moduleMasterRepository,
                                           AccessTypeMasterRepository accessTypeMasterRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.apiMasterRepository = apiMasterRepository;
        this.moduleMasterRepository = moduleMasterRepository;
        this.accessTypeMasterRepository = accessTypeMasterRepository;
    }

    @Override
    protected ParentRepository<ModuleAPIMappingView, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<ModuleAPIMappingView> getCriteriaUtil() {
        return criteriaUtil;
    }



    @Override
    public CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId,Long orgId) {

        ApiModuleMappingMasterRequest request = objectMapper.convertValue(commonRequest, ApiModuleMappingMasterRequest.class);
        ApiMaster apiMaster = apiMasterRepository.findOneActiveByUUIDOptional(request.getApiId(),orgId).orElseThrow(() -> new ValidationException("API not found"));
        ModuleMaster moduleMaster = moduleMasterRepository.findOneActiveByUUIDOptional(request.getModuleId(),orgId).orElseThrow(() -> new ValidationException("Module not found"));
        Optional<AccessTypeMaster> accessTypeMaster = accessTypeMasterRepository.findOneByEntityIdAndAccessTypeAndRefIdAndOrgId(moduleMaster.getId(),apiMaster.getId(), ACCESS_TYPE.MODULE_API,orgId);
        AccessTypeMaster master = accessTypeMaster.orElseGet(AccessTypeMaster::new);
        master.setEntityId(moduleMaster.getId());
        master.setRefId(apiMaster.getId());
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
        ModuleAPIMappingView master = getRepository().findOneActiveByUUID(uuid,orgId);
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
        return CommonPayLoad.of("Successfully Deleted the Module API relationship",objectMapper.convertValue(oneActiveByUUID, ApiModuleMappingMasterResponse.class));
    }


}
