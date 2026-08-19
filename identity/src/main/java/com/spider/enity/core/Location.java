package com.spider.dao.enity.core;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "m_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location extends CommonEntity {

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "description")
    private String description;

    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "org_id")
    private Long orgId;



}
