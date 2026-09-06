package com.spider.rest.tech;

import com.spider.common.dto.UserSessionDTO;
import com.spider.common.response.CommonResponse;
import com.spider.common.rest.BaseResource;
import com.spider.sevice.RedisService;
import com.spider.sevice.core.UserService;
import com.spider.common.response.identity.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;

@RestController
@Tag(name = "Sessions")
@SecurityRequirement(name = "bearerAuth")
public class AuthenticationRestController extends BaseResource {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RedisService redisService;
    private final UserService userService;

    public AuthenticationRestController(RedisService redisService, UserService userService) {
        this.redisService = redisService;
        this.userService = userService;
    }

    @GetMapping(path = "/auth/identity/session", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List the current user's sessions")
    public CommonResponse<Collection<UserSessionDTO>> getAllSessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return CommonResponse.of(redisService.getSessionsOfUser(sessionKey(authorization)), resolve("common.success"));
    }

    @PutMapping(path = "/api/auth/identity/session/logout/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Log out all sessions for the current user")
    public CommonResponse<Object> logoutAllSessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        redisService.removeAllSessionsOfUser(sessionKey(authorization));
        return CommonResponse.of(resolve("session.logout.success"));
    }

    @GetMapping(path = "/private/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the authenticated session")
    public CommonResponse<UserSessionDTO> authorize(HttpServletRequest request) {
        return CommonResponse.of((UserSessionDTO) request.getAttribute("userSession"), resolve("common.success"));
    }

    @GetMapping(path = "/auth/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the authenticated user")
    public CommonResponse<UserDTO> getCurrentUser(
            @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(userService.getById(userId), resolve("common.success"));
    }

    @GetMapping(path = "/api/identity/auth/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Log out the current session")
    public CommonResponse<Object> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        redisService.removeUserSessionDTO(sessionKey(authorization));
        return CommonResponse.of(resolve("session.logout.success"));
    }

    private String sessionKey(String authorization) {
        return authorization.startsWith(BEARER_PREFIX)
                ? authorization.substring(BEARER_PREFIX.length()).trim()
                : authorization.trim();
    }
}
