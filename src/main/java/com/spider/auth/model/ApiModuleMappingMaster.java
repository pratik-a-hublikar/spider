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
@Table(name = "m_api_module_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_api_module_mapping_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_api_module_mapping_uuid", columnList = "uuid")
        })
public class ApiModuleMappingMaster extends ParentEntity {

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "api_id",referencedColumnName = "id")
    private ApiMaster apiMaster;

    @ManyToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "module_id",referencedColumnName = "id")
    private ModuleMaster moduleMaster;

    @Column(name = "org_id")
    private Long orgId;
}
