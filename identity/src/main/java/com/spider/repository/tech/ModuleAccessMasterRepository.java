package com.spider.repository.tech;

import com.spider.common.enums.EModuleAccessType;
import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.ModuleAccessMaster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Repository
public interface ModuleAccessMasterRepository  extends ParentRepository<ModuleAccessMaster,Long> {
    ModuleAccessMaster getByModuleIdAndName(Long id, EModuleAccessType name);

    Optional<ModuleAccessMaster> findByModuleIdAndNameAndIsActiveAndIsDeleted(
            Long moduleId, EModuleAccessType name, boolean active, boolean deleted);

    void deleteByModuleId(Long id);

    List<ModuleAccessMaster> getByModuleId(Long moduleId);


}
