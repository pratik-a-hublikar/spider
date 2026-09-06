package com.spider.common.response.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserDTO {


    private Long id;
    private String uuid;
    private String email;

    private String fname;

    private String lname;

    private String fullName;

    private String username;

    private Integer statusId;


    private Boolean superAdmin;

    private List<Long> orgIdList;
    private List<Long> locationIdList;
    private List<Long> roleIdList;
    private List<Long> reportsToUserIdList;
    private Boolean canManage;
}
