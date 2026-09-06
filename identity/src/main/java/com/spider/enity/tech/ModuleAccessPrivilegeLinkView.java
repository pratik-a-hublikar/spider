package com.spider.enity.tech;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Immutable
@Subselect("SELECT a.id as id,a.module_access_id as module_access_id,b.name as module_access_name,b.module_id as module_id,c.module_name as module_name,d.id as privilege_id,f.name as role_name  FROM m_module_access_privilege_link a left join m_module_access b on a.module_access_id  =b.id left join m_privilege_master d  on a.privilege_id =d.id left join m_role_module_access_link e on e.module_access_id =b.id left join m_role_master f on f.id =e.role_id left join m_module_master c on c.id= b.module_id")
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAccessPrivilegeLinkView extends CommonEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "module_access_id")
    private Long moduleAccessId;

    @Column(name = "module_access_name")
    private String moduleAccessName;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "privilege_id")
    private Long privilegeId;

    @Column(name = "role_name")
    private String roleName;

    @Transient
    private List<ModuleMaster> parentModuleList;

    @Transient
    private Map<Long, String> moduleAccess;
}
