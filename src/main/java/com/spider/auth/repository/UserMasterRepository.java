package com.spider.auth.repository;

import com.spider.auth.model.UserMaster;
import com.spider.common.repository.ParentRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMasterRepository extends ParentRepository<UserMaster,Long> {
    UserMaster findOneByEmailAndIsActiveAndIsDeleted(String username, boolean active, boolean deleted);


    @Query(value = """
        SELECT CASE WHEN COUNT(atm) > 0 THEN true ELSE false END
        FROM access_type_master atm
        WHERE atm.is_deleted = 0 AND atm.is_active = 1 AND (
            (atm.access_type = 'USER_ROLE' AND atm.user_id = :userId AND atm.role_id IS NOT NULL AND
             EXISTS (
                SELECT 1 FROM access_type_master atm2
                WHERE atm2.access_type = 'ROLE_MODULE' AND atm2.is_deleted = 0 AND atm2.is_active = 1 AND atm2.role_id = atm.role_id AND atm2.module_id IS NOT NULL AND
                      EXISTS (
                          SELECT 1 FROM access_type_master atm3
                          WHERE atm3.access_type = 'MODULE_API' AND atm3.is_deleted = 0 AND atm3.is_active = 1 AND atm3.module_id = atm2.module_id AND atm3.api_id = :apiId
                      )
             )
            )
            OR
            (atm.access_type = 'USER_ROLE_GROUP' AND atm.user_id = :userId AND atm.role_group_id IS NOT NULL AND
             EXISTS (
                SELECT 1 FROM access_type_master atm2
                WHERE atm2.access_type = 'ROLE_GROUP_ROLE' AND atm2.is_deleted = 0 AND atm2.is_active = 1 AND atm2.role_group_id = atm.role_group_id AND atm2.role_id IS NOT NULL AND
                      EXISTS (
                          SELECT 1 FROM access_type_master atm3
                          WHERE atm3.access_type = 'ROLE_MODULE' AND atm3.is_deleted = 0 AND atm3.is_active = 1 AND atm3.role_id = atm2.role_id AND atm3.module_id IS NOT NULL AND
                                EXISTS (
                                    SELECT 1 FROM access_type_master atm4
                                    WHERE atm4.access_type = 'MODULE_API' AND atm4.is_deleted = 0 AND atm4.is_active = 1 AND atm4.module_id = atm3.module_id AND atm4.api_id = :apiId
                                )
                      )
             )
            )
        )
        """, nativeQuery = true)
    boolean existsAccessToAPI(@Param("apiId") Long apiId, @Param("userId") Long userId);

}
