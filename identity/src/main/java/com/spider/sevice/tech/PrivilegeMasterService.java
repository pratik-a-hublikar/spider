package com.spider.sevice.tech;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.PrivilegeCreateRequest;
import com.spider.common.request.identity.PrivilegeUpdateRequest;
import com.spider.dto.PrivilegeDTO;
import com.spider.common.service.CommonService;
import com.spider.enity.tech.PrivilegeMaster;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;

public interface PrivilegeMasterService extends CommonService<PrivilegeMaster,Long> {
    Page<PrivilegeMaster> getAllPrivilege(RecordFilter filter);

    PrivilegeDTO getPrivilegeById(Long privilegeId);

    void updatePrivilege(PrivilegeUpdateRequest request);

    Page<PrivilegeMaster> getAllPrevilege(RecordFilter filter);

    void deletePrivilegeByPrivilegeId(Long id);

    List<PrivilegeDTO> getAllPermissions();

    void createPrivilege(PrivilegeCreateRequest request);

    PrivilegeMaster get(String uri, String method);

    Collection<Long> getPrivilegesByUserId(Long userId);
}
