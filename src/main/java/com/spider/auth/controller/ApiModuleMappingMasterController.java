package com.spider.auth.controller;

import com.spider.auth.model.ApiModuleMappingMaster;
import com.spider.auth.request.ApiModuleMappingMasterRequest;
import com.spider.auth.service.ApiModuleMappingMasterService;
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
@RequestMapping(AppConstants.API_MODULE_MASTER)
public class ApiModuleMappingMasterController {


    private final ApiModuleMappingMasterService service;

    @Autowired
    public ApiModuleMappingMasterController(ApiModuleMappingMasterService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<CommonPayLoad<CommonResponse>> create(@Valid @RequestBody ApiModuleMappingMasterRequest apiMasterRequest,@RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {

        CommonPayLoad<CommonResponse> response = service.create(apiMasterRequest,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> get(@PathVariable("uuid") String uuid,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = service.get(uuid,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> update(@PathVariable("uuid") String uuid,
                                                                @Valid @RequestBody ApiModuleMappingMasterRequest apiMasterRequest,
                                                                @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = service.update(uuid,apiMasterRequest,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> softDelete(@PathVariable("uuid") String uuid, @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = service.softDelete(uuid,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/filter")
    public ResponseEntity<Page<ApiModuleMappingMaster>> filter(@Valid @RequestBody RecordFilter recordFilter) {
        Page<ApiModuleMappingMaster> filter = service.filter(recordFilter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}

