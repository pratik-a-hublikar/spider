package com.spider.auth.model;


import com.spider.common.model.ParentEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_user_role_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_role_mapping_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_user_role_mapping_uuid", columnList = "uuid")
        })
public class UserRoleMappingMaster extends ParentEntity {


    @Column(name = "org_id")
    private Long orgId;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "role_id",referencedColumnName = "id")
    private RoleMaster roleMaster;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private UserMaster userMaster;

}
