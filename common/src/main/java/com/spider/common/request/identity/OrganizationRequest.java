package com.spider.common.request.identity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRequest {
    @NotBlank
    private String name;
    private Long parentOrganizationId;
}
