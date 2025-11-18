package com.spider.auth.controller;

import com.spider.auth.model.RoleMaster;
import com.spider.auth.request.RoleMasterRequest;
import com.spider.auth.service.RoleMasterService;
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
@RequestMapping(AppConstants.ROLE_DEPT_MASTER)
public class RoleDepartmentMappingMasterController {


    private final RoleMasterService roleMasterService;

    @Autowired
    public RoleDepartmentMappingMasterController(RoleMasterService roleMasterService) {
        this.roleMasterService = roleMasterService;
    }

    @PostMapping()
    public ResponseEntity<CommonPayLoad<CommonResponse>> create(@Valid @RequestBody RoleMasterRequest request,
                                                                @RequestAttribute("userId") String userId,
                                                                @RequestAttribute("orgId") Long orgId) {

        CommonPayLoad<CommonResponse> response = roleMasterService.create(request,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> get(@PathVariable("uuid") String uuid,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = roleMasterService.get(uuid,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> update(@PathVariable("uuid") String uuid,
                                                                @Valid @RequestBody RoleMasterRequest request,
                                                                @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = roleMasterService.update(uuid,request,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> softDelete(@PathVariable("uuid") String uuid,
                                                                    @RequestAttribute("userId") String userId,
                                                                    @RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = roleMasterService.softDelete(uuid,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/filter")
    public ResponseEntity<Page<RoleMaster>> filter(@Valid @RequestBody RecordFilter recordFilter) {
        Page<RoleMaster> filter = roleMasterService.filter(recordFilter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}

