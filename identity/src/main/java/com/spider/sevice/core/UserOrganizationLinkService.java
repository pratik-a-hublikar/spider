package com.spider.sevice.core;

import com.spider.common.service.CommonService;
import com.spider.common.request.filter.RecordFilter;
import com.spider.enity.core.UserOrganizationLink;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserOrganizationLinkService extends CommonService<UserOrganizationLink,Long> {

    void createIfAbsent(Long userId, Long orgId);

    void replaceLinks(Long userId, List<Long> organizationIds);

    void deactivateByUserId(Long userId);

    List<String> getActiveUserIdsByOrganizationId(Long orgId);

    Page<UserOrganizationLink> getAllLinks(RecordFilter filter);

    UserOrganizationLink createLink(UserOrganizationLink link);

    UserOrganizationLink updateLink(Long id, UserOrganizationLink link);

    void deleteLink(Long id);
}