package com.spider.common.repository;

import com.spider.common.model.ParentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ParentRepository<T extends ParentEntity,ID> extends JpaRepository<T,ID> {
    Page<T> findAll(Specification<T> specification, Pageable pageRequest);

    default T findOneActiveByUUID(String uuid,Long orgId){
        return this.findOneByUuidAndOrgIdAndIsActiveAndIsDeleted(uuid,orgId,true,false);
    }

    default Optional<T> findOneActiveByUUIDOptional(String uuid,Long orgId){
        return Optional.ofNullable(this.findOneByUuidAndOrgIdAndIsActiveAndIsDeleted(uuid,orgId,true,false));
    }


    default List<T> getAllActive(String uuid,Long orgId){
        return this.getAllByUuidAndOrgIdAndIsActiveAndIsDeleted(uuid,orgId,true,false);
    }

    List<T> getAllByUuidAndOrgIdAndIsActiveAndIsDeleted(String uuid,Long orgId, boolean active, boolean deleted);


    T findOneByUuidAndOrgIdAndIsActiveAndIsDeleted(String uuid,Long orgId, boolean active, boolean deleted);

}
