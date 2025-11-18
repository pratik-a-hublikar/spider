package com.spider.auth.controller;

import com.spider.auth.model.DepartmentModuleMappingMaster;
import com.spider.auth.request.DepartmentModuleMappingMasterRequest;
import com.spider.auth.service.DepartmentModuleMappingMasterService;
import com.spider.common.AppConstants;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.response.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.DEPT_MODULE_MASTER)
public class DepartmentModuleMappingMasterController {

    private final DepartmentModuleMappingMasterService departmentModuleMappingMasterService;

    @Autowired
    public DepartmentModuleMappingMasterController(DepartmentModuleMappingMasterService departmentModuleMappingMasterService) {
        this.departmentModuleMappingMasterService = departmentModuleMappingMasterService;
    }

    @PostMapping()
    public ResponseEntity<CommonPayLoad<CommonResponse>> create(@Valid @RequestBody DepartmentModuleMappingMasterRequest request,
                                                                @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = departmentModuleMappingMasterService.create(request,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> get(@PathVariable("uuid") String uuid,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = departmentModuleMappingMasterService.get(uuid,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> update(@PathVariable("uuid") String uuid,
                                                                @Valid @RequestBody DepartmentModuleMappingMasterRequest request,@RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = departmentModuleMappingMasterService.update(uuid,request,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> softDelete(@PathVariable("uuid") String uuid, @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = departmentModuleMappingMasterService.softDelete(uuid,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/filter")
    public ResponseEntity<Page<DepartmentModuleMappingMaster>> filter(@Valid @RequestBody RecordFilter recordFilter) {
        Page<DepartmentModuleMappingMaster> filter = departmentModuleMappingMasterService.filter(recordFilter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}

