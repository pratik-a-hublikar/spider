package com.spider.enity.tech;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "m_privilege_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeMaster extends CommonEntity {

    @Column(name = "privilege_name")
    private String name;

    @Column(name = "url_")
    private String uri;

    @Column(name = "method_")
    private String method;

}
