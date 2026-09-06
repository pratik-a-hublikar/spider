package com.spider.common.request.identity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOrganizationLinkRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long orgId;
}
