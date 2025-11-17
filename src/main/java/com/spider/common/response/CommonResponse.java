package com.spider.common.response;

import lombok.Data;

import java.util.Date;

@Data
public class CommonResponse {

    private String uuid;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;

}
