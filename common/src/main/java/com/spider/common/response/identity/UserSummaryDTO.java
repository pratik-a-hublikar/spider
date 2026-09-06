package com.spider.common.response.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserSummaryDTO {
    private Long id;
    private String uuid;
    private String email;
    private String fullName;
    private String username;
    private List<Long> orgIdList;
    private Boolean canManage;
}
