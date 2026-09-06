package com.spider.enity.tech;

import com.spider.common.model.CommonEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = " m_module_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleMaster extends CommonEntity {

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "description")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "module_ui_name")
    private String moduleUiName;

    @Column(name = "url")
    private String url;

    @Column(name = "is_master_entry")
    private boolean isMasterEntry;

    @OneToMany(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "parent_id",referencedColumnName = "id",updatable = false,insertable = false)
    private List<ModuleMaster> childModules;

}
