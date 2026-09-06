package com.spider.sevice.integration.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spider.exception.ShipmntsIntegrationException;
import com.spider.sevice.integration.AuthenticationScriptExecutor;
import com.spider.sevice.integration.ShipmntsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

@Service
public class ShipmntsServiceImpl implements ShipmntsService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final String USER_PROFILE_QUERY = """
            query user_profile {
              user_profile {
                id
                first_name
                last_name
                name
                email
                user_level
                tenant_id
                company_account {
                  id
                  registered_name
                  display_name
                  default_currency
                  mis_currency
                  business_type
                  subdomain
                  workos_org_id
                }
                branch_accounts {
                  id
                  name
                  entity_type
                  city { name code }
                  state { name code }
                  country { name code }
                }
              }
            }
            """;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI authenticationUri;
    private final URI graphQlUri;
    private final String clientId;
    private final String clientSecret;
    private final String organizationId;
    private final URI oauthAuthorizeUri;
    private final URI oauthTokenUri;
    private final String oauthRedirectUri;
    private final AuthenticationScriptExecutor authenticationScriptExecutor;
    private volatile String refreshToken;

    public ShipmntsServiceImpl(
            ObjectMapper objectMapper,
            @Value("${shipmnts.authentication-url}") URI authenticationUri,
            @Value("${shipmnts.graphql-url}") URI graphQlUri,
            @Value("${shipmnts.client-id}") String clientId,
            @Value("${shipmnts.client-secret:}") String clientSecret,
            @Value("${shipmnts.refresh-token:}") String refreshToken,
            @Value("${shipmnts.organization-id:}") String organizationId,
            @Value("${shipmnts.oauth-authorize-url}") URI oauthAuthorizeUri,
            @Value("${shipmnts.oauth-token-url}") URI oauthTokenUri,
            @Value("${shipmnts.oauth-redirect-uri}") String oauthRedirectUri,
            AuthenticationScriptExecutor authenticationScriptExecutor) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.authenticationUri = authenticationUri;
        this.graphQlUri = graphQlUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.organizationId = organizationId;
        this.oauthAuthorizeUri = oauthAuthorizeUri;
        this.oauthTokenUri = oauthTokenUri;
        this.oauthRedirectUri = oauthRedirectUri;
        this.authenticationScriptExecutor = authenticationScriptExecutor;
    }

    @Override
    public JsonNode fetchData() {
        validateConfiguration();
        String accessToken = authenticate();
        return fetchUserProfile(accessToken);
    }

    @Override
    public String generateRefreshToken(String email, String password, boolean interactive) {
        // Execute the authentication script which will write the refresh token to .env
        String token = authenticationScriptExecutor.executeAuthentication(email, password, interactive);
        if (token != null && !token.isBlank()) {
            // update in-memory value so restart isn't required
            this.refreshToken = token;
        } else {
            // If the script didn't emit a token, try to read current .env value
            String maybe = readEnvRefreshToken();
            if (maybe != null && !maybe.isBlank()) this.refreshToken = maybe;
            token = maybe == null ? "" : maybe;
        }
        return token == null ? "" : token;
    }

    @Override
    public String exchangeAuthorizationCode(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new ShipmntsIntegrationException(HttpStatus.BAD_REQUEST,
                    "Authorization code is required");
        }

        try {
            // Prepare token exchange request
            ObjectNode payload = objectMapper.createObjectNode()
                    .put("client_id", clientId)
                    .put("client_secret", clientSecret)
                    .put("grant_type", "authorization_code")
                    .put("code", authorizationCode)
                    .put("redirect_uri", oauthRedirectUri);

            HttpResponse<String> response = send(oauthTokenUri, payload, null);
            JsonNode body = readResponse(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw upstreamFailure("Shipmnts OAuth token exchange failed", response.statusCode(), body);
            }

            String accessToken = body.path("access_token").asText();
            if (accessToken.isBlank()) {
                throw new ShipmntsIntegrationException(HttpStatus.BAD_GATEWAY,
                        "Shipmnts OAuth did not return an access token");
            }

            String newRefreshToken = body.path("refresh_token").asText();
            if (newRefreshToken.isBlank()) {
                throw new ShipmntsIntegrationException(HttpStatus.BAD_GATEWAY,
                        "Shipmnts OAuth did not return a refresh token");
            }

            // Update in-memory token
            this.refreshToken = newRefreshToken;

            // Persist to .env
            persistRefreshTokenToEnv(newRefreshToken);

            return newRefreshToken;

        } catch (ShipmntsIntegrationException e) {
            throw e;
        } catch (Exception e) {
            throw new ShipmntsIntegrationException(HttpStatus.BAD_GATEWAY,
                    "Failed to exchange authorization code: " + e.getMessage());
        }
    }

    @Override
    public String getAuthorizationUrl() {
        try {
            // Build the authorization URL with proper parameters
            String state = UUID.randomUUID().toString();
            StringBuilder url = new StringBuilder(oauthAuthorizeUri.toString());
            if (!url.toString().endsWith("/")) {
                url.append("/");
            }
            url.append("?client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            url.append("&redirect_uri=").append(URLEncoder.encode(oauthRedirectUri, StandardCharsets.UTF_8));
            url.append("&response_type=code");
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));

            return url.toString();
        } catch (Exception e) {
            throw new ShipmntsIntegrationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate authorization URL: " + e.getMessage());
        }
    }

    private void persistRefreshTokenToEnv(String token) {
        try {
            Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
            String contents = Files.exists(envPath) ? Files.readString(envPath, StandardCharsets.UTF_8) : "";
            String updated = replaceEnvValue(contents, "SHIPMNTS_REFRESH_TOKEN", token);
            Files.writeString(envPath, updated, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Log but don't fail if .env update fails
            System.err.println("Warning: Failed to persist refresh token to .env: " + e.getMessage());
        }
    }

    private String replaceEnvValue(String contents, String key, String value) {
        String val = value == null ? "" : value;
        if (val.contains("\n") || val.contains("\r")) {
            throw new IllegalArgumentException("Refusing to store an invalid " + key + " value.");
        }
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
        for (String rawLine : contents.split("\r?\n", -1)) {
            if (rawLine.startsWith(key + "=")) {
                sb.append(key).append("=").append(val).append(System.lineSeparator());
                replaced = true;
            } else {
                sb.append(rawLine).append(System.lineSeparator());
            }
        }
        if (!replaced) {
            if (!contents.isEmpty() && !contents.endsWith("\n")) sb.append(System.lineSeparator());
            sb.append(key).append("=").append(val).append(System.lineSeparator());
        }
        return sb.toString();
    }

    private String readEnvRefreshToken() {
        try {
            Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
            if (!Files.exists(envPath)) return null;
            String contents = Files.readString(envPath, StandardCharsets.UTF_8);
            for (String rawLine : contents.split("\\r?\\n")) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int sep = line.indexOf('=');
                if (sep < 0) continue;
                String k = line.substring(0, sep).trim();
                String v = line.substring(sep + 1).trim();
                if (k.equals("SHIPMNTS_REFRESH_TOKEN")) {
                    if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                        v = v.substring(1, v.length() - 1);
                    }
                    return v;
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    private synchronized String authenticate() {
        ObjectNode payload = objectMapper.createObjectNode()
                .put("client_id", clientId)
                .put("grant_type", "refresh_token")
                .put("refresh_token", refreshToken);
        if (!organizationId.isBlank()) {
            payload.put("organization_id", organizationId);
        }

        HttpResponse<String> response = send(authenticationUri, payload, null);
        JsonNode body = readResponse(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw upstreamFailure("Shipmnts authentication failed", response.statusCode(), body);
        }

        String accessToken = body.path("access_token").asText();
        if (accessToken.isBlank()) {
            throw new ShipmntsIntegrationException(
                    HttpStatus.BAD_GATEWAY, "Shipmnts authentication did not return an access token.");
        }

        String rotatedRefreshToken = body.path("refresh_token").asText();
        if (!rotatedRefreshToken.isBlank()) {
            refreshToken = rotatedRefreshToken;
        }
        return accessToken;
    }

    private JsonNode fetchUserProfile(String accessToken) {
        ObjectNode operation = objectMapper.createObjectNode()
                .put("operationName", "user_profile")
                .put("query", USER_PROFILE_QUERY);
        operation.set("variables", objectMapper.createObjectNode());
        ArrayNode payload = objectMapper.createArrayNode().add(operation);

        HttpResponse<String> response = send(graphQlUri, payload, accessToken);
        JsonNode body = readResponse(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw upstreamFailure("Shipmnts data request failed", response.statusCode(), body);
        }
        JsonNode result = body.isArray() ? body.path(0) : body;
        if (result.has("errors") && !result.path("errors").isEmpty()) {
            String message = result.path("errors").path(0).path("message").asText("Unknown GraphQL error");
            throw new ShipmntsIntegrationException(
                    HttpStatus.BAD_GATEWAY, "Shipmnts GraphQL request failed: " + message);
        }
        if (!result.has("data")) {
            throw new ShipmntsIntegrationException(
                    HttpStatus.BAD_GATEWAY, "Shipmnts GraphQL response did not contain data.");
        }
        return result.path("data");
    }

    private HttpResponse<String> send(URI uri, JsonNode payload, String accessToken) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            if (accessToken != null) {
                request.header("Authorization", "Bearer " + accessToken);
            }
            return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ShipmntsIntegrationException(
                    HttpStatus.BAD_GATEWAY, "Shipmnts request was interrupted.");
        } catch (IOException | IllegalArgumentException exception) {
            throw new ShipmntsIntegrationException(
                    HttpStatus.BAD_GATEWAY, "Unable to connect to Shipmnts.");
        }
    }

    private JsonNode readResponse(String responseBody) {
        try {
            return objectMapper.readTree(responseBody == null || responseBody.isBlank() ? "{}" : responseBody);
        } catch (JsonProcessingException exception) {
            throw new ShipmntsIntegrationException(
                    HttpStatus.BAD_GATEWAY, "Shipmnts returned an invalid JSON response.");
        }
    }

    private ShipmntsIntegrationException upstreamFailure(String prefix, int statusCode, JsonNode body) {
        String description = body.path("error_description").asText();
        if (description.isBlank()) description = body.path("message").asText();
        String suffix = description.isBlank() ? "HTTP " + statusCode : description;
        return new ShipmntsIntegrationException(HttpStatus.BAD_GATEWAY, prefix + ": " + suffix);
    }

    private void validateConfiguration() {
        if (clientId.isBlank() || refreshToken.isBlank()) {
            throw new ShipmntsIntegrationException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Shipmnts integration requires SHIPMNTS_CLIENT_ID and a browser-issued "
                            + "SHIPMNTS_REFRESH_TOKEN.");
        }
    }
}
