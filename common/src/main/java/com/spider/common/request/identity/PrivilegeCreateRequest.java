package com.spider.common.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PrivilegeCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String url;

    @Size(min = 1)
    private Set<Long> moduleAccessIds;

    @NotNull
    @NotBlank
    private String method;
}
