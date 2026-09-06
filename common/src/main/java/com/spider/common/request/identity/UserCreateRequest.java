package com.spider.common.request.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String fname;

    private String lname;

    @NotBlank
    private String username;

    private Integer statusId;

    private List<Long> orgIdList;
    private List<Long> locationIdList;
    private List<Long> roleIdList;
    private List<Long> reportsToUserIdList;

}
