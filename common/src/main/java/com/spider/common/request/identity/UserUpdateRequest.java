package com.spider.common.request.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank
    @Email
    private String email;

    @Size(min = 8)
    private String password;

    @NotBlank
    private String fname;

    private String lname;

    @NotBlank
    private String username;

    private Boolean emailVerified;
    private Boolean otpVerified;
    private Integer statusId;
    private Integer timeout;
    private List<Long> orgIdList;
    private List<Long> locationIdList;
    private List<Long> roleIdList;
    private List<Long> reportsToUserIdList;
    private Boolean superAdmin;
}
