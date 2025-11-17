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

    default T findOneActiveByUUID(String uuid){
        return this.findOneByUuidAndIsActiveAndIsDeleted(uuid,true,false);
    }

    default Optional<T> findOneActiveByUUIDOptional(String uuid){
        return Optional.ofNullable(this.findOneByUuidAndIsActiveAndIsDeleted(uuid,true,false));
    }


    default List<T> getAllActive(String uuid){
        return this.getAllByUuidAndIsActiveAndIsDeleted(uuid,true,false);
    }

    List<T> getAllByUuidAndIsActiveAndIsDeleted(String uuid, boolean active, boolean deleted);


    T findOneByUuidAndIsActiveAndIsDeleted(String uuid, boolean active, boolean deleted);

}
