package com.spider.common.response.identity;

import jakarta.persistence.Column;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDetailDTO {
    private Long id;
    private String name;
    private Long parentId;
    private String moduleUiName;
    private String url;
    private List<ModuleAccessDTO> moduleAccess;
    private List<String> moduleAccessStr;

    private List<ModuleDetailDTO> subModules;
}
