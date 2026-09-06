package com.spider.common.response.identity;

import com.spider.common.enums.EModuleAccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleModuleAccessDTO {
    private Long moduleId;
    private List<EModuleAccessType> accesses;
}
