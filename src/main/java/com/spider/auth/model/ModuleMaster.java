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
@Table(name = "m_module_master",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_module_master_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_module_master_uuid", columnList = "uuid")
        })
public class ModuleMaster extends ParentEntity {

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "description")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "org_id")
    private Long orgId;

    @OneToMany(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "parent_id",referencedColumnName = "id",updatable = false,insertable = false)
    private List<ModuleMaster> childModules;

    @ManyToMany( fetch = FetchType.EAGER)
    @JoinTable(
            name = "m_api_module_mapping",
            joinColumns = @JoinColumn(name = "api_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<ApiMaster> apiMasterList;


}
