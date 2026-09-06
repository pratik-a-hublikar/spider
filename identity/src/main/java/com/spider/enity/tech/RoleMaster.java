package com.spider.enity.tech;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spider.common.model.CommonEntity;
import com.spider.common.response.identity.RoleModuleAccessDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.JoinColumn;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_role_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleMaster extends CommonEntity {


    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_system_defined")
    private Boolean systemDefined;

    @Column(name = "org_id")
    private Long orgId;

    @Transient
    private List<Long> reportsToRoleIds;

    @Transient
    private List<RoleModuleAccessDTO> moduleAccess;

    @JsonIgnore
    @OneToMany
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<RoleReportingRoleLink> reportingRoleLinks;

}
