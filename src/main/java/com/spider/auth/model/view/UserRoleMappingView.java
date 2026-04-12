package com.spider.auth.model.view;


import com.spider.common.model.ParentEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.View;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@View(query = "select a.org_id and org_id,a.entity_id as user_id,a.ref_id as role_id  from m_access_type_master a where a.access_type='USER_ROLE'")
public class UserRoleMappingView extends ParentEntity {


    @Column(name = "org_id")
    private Long orgId;

//    @ManyToOne(cascade = { CascadeType.MERGE })
//    @JoinColumn(name = "role_id",referencedColumnName = "id")
//    private RoleMaster roleMaster;
//
//    @ManyToOne(cascade = { CascadeType.MERGE })
//    @JoinColumn(name = "user_id",referencedColumnName = "id")
//    private UserMaster userMaster;

}
