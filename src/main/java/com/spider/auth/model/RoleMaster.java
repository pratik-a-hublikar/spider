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
@Table(name = "m_role",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_master_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_role_master_uuid", columnList = "uuid")
        })
public class RoleMaster extends ParentEntity {


    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "org_id")
    private Long orgId;




}
