package com.spider.auth.repository.view;

import com.spider.auth.model.view.ModuleAPIMappingView;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleAPIMappingViewRepository extends ParentRepository<ModuleAPIMappingView,Long> {
}
