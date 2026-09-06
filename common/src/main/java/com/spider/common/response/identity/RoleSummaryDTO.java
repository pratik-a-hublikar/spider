package com.spider.common.response.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleSummaryDTO {
    private Long id;
    private String uuid;
    private String name;
    private Long orgId;
    private Long userCount;
    private Boolean canManage;
}
