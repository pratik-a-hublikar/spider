package com.spider.auth.controller;

import com.spider.auth.exception.AuthenticationException;
import com.spider.auth.model.UserMaster;
import com.spider.auth.request.DepartmentMasterRequest;
import com.spider.auth.service.UserMasterService;
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
@RequestMapping(AppConstants.USER_MASTER)
public class UserMasterController {

    private final UserMasterService service;

    @Autowired
    public UserMasterController(UserMasterService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<CommonPayLoad<CommonResponse>> create(@Valid @RequestBody DepartmentMasterRequest request) {
        throw  new AuthenticationException("API is not ready yet!");
    }
    @PutMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> update(@PathVariable("uuid") String uuid,
                                                                @Valid @RequestBody DepartmentMasterRequest request) {
        throw  new AuthenticationException("API is not ready yet!");
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> get(@PathVariable("uuid") String uuid) {
        CommonPayLoad<CommonResponse> response = service.get(uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @DeleteMapping("/{uuid}")
    public ResponseEntity<CommonPayLoad<CommonResponse>> softDelete(@PathVariable("uuid") String uuid, @RequestAttribute("userId") String userId) {
        CommonPayLoad<CommonResponse> response = service.softDelete(uuid,userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/filter")
    public ResponseEntity<Page<UserMaster>> filter(@Valid @RequestBody RecordFilter recordFilter) {
        Page<UserMaster> filter = service.filter(recordFilter);
        return new ResponseEntity<>(filter, HttpStatus.OK);
    }

}

