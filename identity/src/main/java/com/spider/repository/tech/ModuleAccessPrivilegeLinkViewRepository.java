package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.ModuleAccessPrivilegeLink;
import com.spider.enity.tech.ModuleAccessPrivilegeLinkView;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface ModuleAccessPrivilegeLinkViewRepository extends ParentRepository<ModuleAccessPrivilegeLinkView,Long> {

    List<ModuleAccessPrivilegeLinkView> getByPrivilegeId(Long privilegeId);

}
