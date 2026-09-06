package com.spider.repository.tech;

import com.spider.common.repository.ParentRepository;
import com.spider.enity.tech.ModuleMaster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleMasterRepository  extends ParentRepository<ModuleMaster,Long> {

    List<ModuleMaster> getByParentId(Long moduleId);

    @Query("from ModuleMaster where parentId is null")
    List<ModuleMaster> getParentModuleList();

}
