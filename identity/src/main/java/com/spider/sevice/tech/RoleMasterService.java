package com.spider.sevice.tech;

import com.spider.common.response.identity.RoleDTO;
import com.spider.common.response.identity.RoleSummaryDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.tech.RoleMaster;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.spider.common.request.filter.RecordFilter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.spider.common.response.identity.UserDTO;

public interface RoleMasterService extends CommonService<RoleMaster,Long> {
    List<RoleMaster> getByModuleAccess(Long id);

    List<RoleSummaryDTO> getByModuleAccessDtos(Long id, Long requesterId);

    Page<RoleSummaryDTO> filterRoles(RecordFilter filter, Long requesterId);

    RoleDTO getByUuid(String roleUuid, Long requesterId);


    void assignRole(long userId, Collection<Long> roleIds);

    void saveRole(RoleDTO roleDTO, Long requesterId);

    void updateRole(String roleUuid, RoleDTO roleDTO, Long requesterId);

    List<RoleSummaryDTO> getAllRoles(Long requesterId);

    void deleteRole(String roleUuid, Long requesterId);

    Set<String> getManageableRoleUuids(Long requesterId);

    List<com.spider.common.response.identity.UserSummaryDTO> getRoleUsers(String roleUuid, Long requesterId);

    long countActiveRoleUsers(String roleUuid, Long requesterId);
}
