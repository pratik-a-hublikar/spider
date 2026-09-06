package com.spider.dto;

import com.spider.enity.tech.ModuleAccessPrivilegeLinkView;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivilegeDTO {

    private Long id;

    private String name;

    private String uri;

    private String method;
    private List<ModuleAccessPrivilegeLinkView> lstModuleAccess;

    public PrivilegeDTO(Long id,String uri, String method) {
        this.id = id;
        this.uri = uri;
        this.method = method;
    }
}
