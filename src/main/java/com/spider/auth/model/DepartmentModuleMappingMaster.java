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
@Table(name = "m_dept_module_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dept_module_mapping_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_dept_module_mapping_uuid", columnList = "uuid")
        })
public class DepartmentModuleMappingMaster extends ParentEntity {

//    @Column(name = "department_id")
//    private Long departmentId;
//
//    @Column(name = "module_id")
//    private Long moduleId;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "department_id",referencedColumnName = "id")
    private DepartmentMaster departmentMaster;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "module_id",referencedColumnName = "id")
    private ModuleMaster moduleMaster;
}
