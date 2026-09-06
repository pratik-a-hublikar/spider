package com.spider.enity.tech;

import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "m_role_reporting_role_link")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleReportingRoleLink extends CommonEntity {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "reports_to_role_id", nullable = false)
    private Long reportsToRoleId;
}
