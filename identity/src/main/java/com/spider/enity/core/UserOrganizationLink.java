package com.spider.enity.core;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_organization_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationLink extends CommonEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "org_id")
    private Long orgId;

}
