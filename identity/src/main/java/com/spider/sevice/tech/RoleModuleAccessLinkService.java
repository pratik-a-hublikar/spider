package com.spider.sevice.tech;

import com.spider.common.service.CommonService;
import com.spider.enity.tech.RoleModuleAccessLink;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

public interface RoleModuleAccessLinkService extends CommonService<RoleModuleAccessLink,Long> {
    void addModuleAccessLink(Long id, Set<Long> roleIds);

    Set<Long> findRoleIdOfModuleAccess(Collection<Long> moduleAccessIds);

    @Transactional
    void deleteByModuleId(@Param("moduleId") Long moduleId);

    Set<Long> findRoleIdsByModuleId(@Param("moduleId") Long moduleId);
}
