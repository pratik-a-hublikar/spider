package com.spider.auth.controller;

import com.spider.auth.model.ModuleMaster;
import com.spider.auth.request.ModuleMasterRequest;
import com.spider.auth.service.ModuleMasterService;
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
@RequestMapping(AppConstants.MODULE_MASTER)
public class ModuleMasterController {

    private final ModuleMasterService moduleMasterService;

    @Autowired
    public ModuleMasterController(ModuleMasterService moduleMasterService) {
        this.moduleMasterService = moduleMasterService;
    }

    @PostMapping()
    public ResponseEntity<CommonPayLoad<CommonResponse>> create(@Valid @RequestBody ModuleMasterRequest moduleMasterRequest,
                                                                @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = moduleMasterService.create(moduleMasterRequest,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> get(@PathVariable("uuid") String uuid,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = moduleMasterService.get(uuid,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> update(@PathVariable("uuid") String uuid,
                                                                @Valid @RequestBody ModuleMasterRequest moduleMasterRequest,@RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = moduleMasterService.update(uuid,moduleMasterRequest,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> softDelete(@PathVariable("uuid") String uuid, @RequestAttribute("userId") String userId,@RequestAttribute("orgId") Long orgId) {
        CommonPayLoad<CommonResponse> response = moduleMasterService.softDelete(uuid,userId,orgId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/filter")
    public ResponseEntity<Page<ModuleMaster>> filter(@Valid @RequestBody RecordFilter recordFilter) {
        Page<ModuleMaster> filter = moduleMasterService.filter(recordFilter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}

