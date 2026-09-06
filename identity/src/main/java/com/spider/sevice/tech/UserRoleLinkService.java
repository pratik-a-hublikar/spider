package com.spider.sevice.tech;

import com.spider.common.response.identity.UserDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.tech.UserRoleLink;

import java.util.List;

public interface UserRoleLinkService extends CommonService<UserRoleLink,Long> {
    void replaceLinks(Long userId, List<Long> roleIds);

    List<Long> getActiveRoleIds(Long userId);

    UserDTO assignUserOrgRole(Long userId, Long roleId, Long orgId);

    UserDTO revokeUserOrgRole(Long userId, Long orgId);
}
