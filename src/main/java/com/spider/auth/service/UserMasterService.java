package com.spider.auth.service;

import com.spider.auth.model.UserMaster;
import com.spider.auth.request.AuthRequest;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.service.ParentService;
import jakarta.servlet.http.HttpServletResponse;

public interface UserMasterService extends ParentService<UserMaster,Long> {

    boolean hasAuthorization(String uri, String method, String uuid);


    CommonPayLoad<String> validateCredentials(AuthRequest authRequest, HttpServletResponse response);

}

