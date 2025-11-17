package com.spider.auth.request;

import com.spider.common.AppConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = AppConstants.USERNAME_IS_MANDATORY)
    private String username;

    @NotBlank(message = AppConstants.PASSWORD_IS_MANDATORY)
    private String password;
}
