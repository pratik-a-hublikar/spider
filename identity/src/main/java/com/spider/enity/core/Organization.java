package com.spider.dao.enity.core;

import com.spider.common.model.CommonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.internal.util.stereotypes.Lazy;

@Entity
@Table(name = "m_organisation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends CommonEntity {


    @Column(name = "app_type", unique = true)
    private String appName;

    @Column(name = "is_super_organization", unique = true)
    private boolean isSuper;

    @Column(name = "parent_org", unique = true)
    private Long parentOrg;


    @OneToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "parent_org",referencedColumnName = "id",updatable = false,insertable = false)
    @Lazy
    private Organization parentOrganization;

    @OneToOne(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "id",referencedColumnName = "parent_org",updatable = false,insertable = false)
    @Lazy
    private Organization ChildOrganization;
}
