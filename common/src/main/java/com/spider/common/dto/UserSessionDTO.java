package com.spider.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserSessionDTO {

    private Long id;

    private String uuid;

    private Long userId;

    private String email;

    private Integer timeout = -1;

    private boolean stayLoggedIn;

    private Date loginDate;

    private Date logOutDate;

    private Date lastAccessDate;

    private String ipAddress;

    private String userAgent;

    private boolean isSuperAdmin;

}
