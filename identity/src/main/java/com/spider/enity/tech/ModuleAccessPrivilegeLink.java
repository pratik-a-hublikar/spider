package com.spider.enity.tech;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "m_module_access_privilege_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleAccessPrivilegeLink  extends CommonEntity {

    @Column(name = "module_access_id")
    private Long moduleAccessId;

    @Column(name = "privilege_id")
    private Long privilegeId;

}
