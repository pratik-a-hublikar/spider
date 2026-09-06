package com.spider.sevice.core;

import com.spider.common.service.CommonService;
import com.spider.enity.core.UserLocationLink;

import java.util.List;

public interface UserLocationLinkService extends CommonService<UserLocationLink,Long> {

    void addLink(Long userId, Long locationId);

    void replaceLinks(Long userId, List<Long> locationIds);
}
