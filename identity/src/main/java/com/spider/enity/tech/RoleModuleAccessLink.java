package com.spider.enity.tech;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_role_module_access_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleModuleAccessLink extends CommonEntity {


    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "module_access_id")
    private Long moduleAccessId;

}
