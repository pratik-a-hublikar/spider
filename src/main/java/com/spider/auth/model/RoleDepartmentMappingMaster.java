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
@Table(name = "m_role_dept_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_dept_mapping_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_role_dept_mapping_uuid", columnList = "uuid")
        })
public class RoleDepartmentMappingMaster extends ParentEntity {


    @Column(name = "org_id")
    private Long orgId;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "role_id",referencedColumnName = "id")
    private RoleMaster roleMaster;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "department_id",referencedColumnName = "id")
    private DepartmentMaster departmentMaster;

}
