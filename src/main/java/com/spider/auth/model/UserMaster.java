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
@Table(name = "m_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_master_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_user_master_uuid", columnList = "uuid")
        })
public class UserMaster extends ParentEntity {


    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "is_super_admin", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSuperAdmin;

    @ManyToMany( fetch = FetchType.EAGER)
    @JoinTable(
            name = "m_user_dept_mapping",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private List<DepartmentMaster> departmentMasters;
}
