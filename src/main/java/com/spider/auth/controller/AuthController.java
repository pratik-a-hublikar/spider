package com.spider.auth.controller;

import com.spider.auth.request.AuthRequest;
import com.spider.auth.service.UserMasterService;
import com.spider.common.AppConstants;
import com.spider.common.response.CommonPayLoad;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.AUTH_ENDPOINT)
public class AuthController {

    private final UserMasterService userMasterService;

    @Autowired
    public AuthController(UserMasterService userMasterService) {
        this.userMasterService = userMasterService;
    }

    @PostMapping(AppConstants.USER_SIGN_IN)
    public ResponseEntity<CommonPayLoad<String>> signIn(@Valid @RequestBody AuthRequest authRequest,
                                                        HttpServletResponse httpServletResponse) {
        CommonPayLoad<String> response = userMasterService.validateCredentials(authRequest,httpServletResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
