package com.spider.enity.tech;


import com.spider.common.enums.EModuleAccessType;
import com.spider.common.model.CommonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "m_module_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAccessMaster extends CommonEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private EModuleAccessType name;

    @Column(name = "module_id")
    private Long moduleId;



}
