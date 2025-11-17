package com.spider.auth.repository;

import com.spider.auth.model.ModuleMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleMasterRepository extends ParentRepository<ModuleMaster,Long> {

    boolean existsByParentIdAndIsActiveAndIsDeleted(Long id, boolean active, boolean deleted);

}
