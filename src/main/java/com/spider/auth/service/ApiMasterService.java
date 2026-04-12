package com.spider.auth.service;

import com.spider.auth.model.ApiMaster;
import com.spider.common.service.ParentService;

import java.util.Optional;

public interface ApiMasterService extends ParentService<ApiMaster,Long> {


    ApiMaster findOneByUriAndMethod(String uri, String method);
}
