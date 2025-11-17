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
@Table(name = "m_api",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_api_master_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_api_master_uuid", columnList = "uuid")
        })
public class ApiMaster extends ParentEntity {

    @Column(name = "path")
    private String path;

    @Column(name = "method")
    private String method;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
