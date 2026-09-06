package com.spider.repository.core;


import com.spider.common.repository.ParentRepository;
import com.spider.enity.core.Location;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository  extends ParentRepository<Location,Long> {

    Location findOneByLocationNameAndOrgIdAndIsActiveAndIsDeleted(
            String locationName, Long orgId, boolean active, boolean deleted);

    Location findOneByLocationNameAndOrgIdAndIdNotAndIsActiveAndIsDeleted(
            String locationName, Long orgId, Long id, boolean active, boolean deleted);
}
