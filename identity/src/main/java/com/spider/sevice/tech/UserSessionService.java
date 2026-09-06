package com.spider.sevice.tech;

import com.spider.common.dto.UserSessionDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.tech.UserSession;

public interface UserSessionService extends CommonService<UserSession,Long> {
    UserSessionDTO findOneByTokenAndActive(String token);
}
