package com.spider.common.response.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrganizationDTO {
    private Long id;
    private String uuid;
    private String name;
    private Boolean superOrganization;
    private Long parentOrganizationId;
    private String parentOrganizationName;
}
