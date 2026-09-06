package com.spider.sevice.tech;

import com.spider.common.service.CommonService;
import com.spider.enity.tech.ModuleAccessPrivilegeLinkView;

import java.util.List;

public interface ModuleAccessPrivilegeLinkViewService extends CommonService<ModuleAccessPrivilegeLinkView,Long> {
    List<ModuleAccessPrivilegeLinkView> getByPrivilegeId(Long privilegeId);
}
