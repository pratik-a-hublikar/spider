package com.spider.auth.controller;

import com.spider.auth.model.ApiMaster;
import com.spider.auth.request.ApiMasterRequest;
import com.spider.auth.service.ApiMasterService;
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
@RequestMapping(AppConstants.API_MASTER)
public class ApiMasterController {


    private final ApiMasterService apiMasterService;

    @Autowired
    public ApiMasterController(ApiMasterService apiMasterService) {
        this.apiMasterService = apiMasterService;
    }

    @PostMapping()
    public ResponseEntity<CommonPayLoad<CommonResponse>> create(@Valid @RequestBody ApiMasterRequest apiMasterRequest,@RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {

        CommonPayLoad<CommonResponse> response = apiMasterService.create(apiMasterRequest,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> get(@PathVariable("uuid") String uuid,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = apiMasterService.get(uuid,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> update(@PathVariable("uuid") String uuid,
                                                                @Valid @RequestBody ApiMasterRequest apiMasterRequest,
                                                                @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = apiMasterService.update(uuid,apiMasterRequest,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> softDelete(@PathVariable("uuid") String uuid, @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = apiMasterService.softDelete(uuid,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/filter")
    public ResponseEntity<Page<ApiMaster>> filter(@Valid @RequestBody RecordFilter recordFilter) {
        Page<ApiMaster> filter = apiMasterService.filter(recordFilter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}

