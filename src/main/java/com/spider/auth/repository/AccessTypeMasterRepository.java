package com.spider.auth.repository;

import com.spider.auth.enums.ACCESS_TYPE;
import com.spider.auth.model.AccessTypeMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTypeMasterRepository extends ParentRepository<AccessTypeMaster,Long> {

    default Optional<AccessTypeMaster> findOneByEntityIdAndAccessTypeAndRefIdAndOrgId(Long entityId, Long refId,ACCESS_TYPE accessType, Long orgId){
        return Optional.ofNullable(this.findOneByEntityIdAndAccessTypeAndRefIdAndOrgId(entityId,accessType,refId,orgId));
    }

    AccessTypeMaster findOneByEntityIdAndAccessTypeAndRefIdAndOrgId(Long entityId,  ACCESS_TYPE accessType, Long refId, Long orgId);

}
