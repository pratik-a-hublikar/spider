/*
 * Copyright (c) 2020 RECOBO
 */

package com.spider.common.response.identity;

import com.spider.common.enums.EModuleAccessType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleAccessDTO {
    private Long id;
    private EModuleAccessType name;
    private Long moduleId;

}
