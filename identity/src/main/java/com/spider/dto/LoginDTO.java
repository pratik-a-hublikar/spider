package com.spider.dto;

import com.spider.common.response.identity.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Authentication response")
public class LoginDTO {

        @Schema(example = "Jane")
        private String firstName;
        @Schema(example = "Doe")
        private String lastName;
        @Schema(example = "user@example.com")
        private String email;
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        private String accessToken;
        private UserDTO user;
}
