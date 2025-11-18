package com.spider.auth.model;

import com.spider.common.model.ParentEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_department",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_department_master_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_department_master_uuid", columnList = "uuid")
        })
public class DepartmentMaster extends ParentEntity {

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "description")
    private String description;

    @ManyToMany( fetch = FetchType.EAGER)
    @JoinTable(
            name = "m_dept_module_mapping",
            joinColumns = @JoinColumn(name = "module_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private List<ModuleMaster> moduleMaster;

    @Column(name = "org_id")
    private Long orgId;

}
