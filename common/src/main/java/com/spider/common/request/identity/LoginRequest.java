package com.spider.common.request.identity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User login request")
public class LoginRequest {
    @Schema(description = "User email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @Schema(description = "User password", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String password;
    @Schema(description = "Keep the session signed in", example = "true")
    private boolean stayLoggedIn;
}
