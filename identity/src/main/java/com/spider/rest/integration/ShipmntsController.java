package com.spider.rest.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.spider.common.response.CommonResponse;
import com.spider.common.rest.BaseResource;
import com.spider.sevice.integration.ShipmntsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/shipmnts")
@Tag(name = "Shipmnts")
public class ShipmntsController extends BaseResource {
    private final ShipmntsService shipmntsService;

    public ShipmntsController(ShipmntsService shipmntsService) {
        this.shipmntsService = shipmntsService;
    }

    @GetMapping("/fetch")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Authenticate with Shipmnts and fetch the current user profile")
    public CommonResponse<JsonNode> fetch() {
        return CommonResponse.of(shipmntsService.fetchData(), resolve("common.success"));
    }

    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Generate a new Shipmnts refresh token by running the authentication flow")
    public CommonResponse<Map<String, String>> authenticate(@RequestBody(required = false) Map<String, Object> body) {
        String email = body != null ? (String) body.getOrDefault("email", null) : null;
        String password = body != null ? (String) body.getOrDefault("password", null) : null;
        boolean interactive = body != null && Boolean.TRUE.equals(body.get("interactive"));
        String token = shipmntsService.generateRefreshToken(email, password, interactive);
        return CommonResponse.of(Map.of("refreshToken", token), resolve("common.success"));
    }

    @GetMapping("/oauth/authorize-url")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the OAuth authorization URL to initiate the OAuth flow")
    public CommonResponse<Map<String, String>> getAuthorizationUrl() {
        String authUrl = shipmntsService.getAuthorizationUrl();
        return CommonResponse.of(Map.of("authorizeUrl", authUrl), resolve("common.success"));
    }

    @PostMapping(value = "/oauth/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OAuth callback and exchange authorization code for tokens")
    public CommonResponse<Map<String, String>> oauthCallback(@RequestBody Map<String, String> body) {
        String code = body != null ? body.get("code") : null;
        String token = shipmntsService.exchangeAuthorizationCode(code);
        return CommonResponse.of(Map.of("refreshToken", token), resolve("common.success"));
    }
}
