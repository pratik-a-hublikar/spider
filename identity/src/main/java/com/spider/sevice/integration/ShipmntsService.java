package com.spider.sevice.integration;

import com.fasterxml.jackson.databind.JsonNode;

public interface ShipmntsService {
    JsonNode fetchData();

    /**
     * Triggers the authentication flow to generate a new refresh token.
     * This will execute the npm script that opens a browser for Shipmnts login.
     * If email and password are provided they will be used for the authentication flow.
     * Returns the new refresh token (may be blank if not returned by the script).
     * If interactive is true the script will run with a visible browser so user can complete CAPTCHA/MFA.
     */
    String generateRefreshToken(String email, String password, boolean interactive);

    /**
     * Exchanges an OAuth authorization code for access and refresh tokens.
     * This implements the OAuth 2.0 authorization code flow.
     * Returns the refresh token obtained from the exchange.
     */
    String exchangeAuthorizationCode(String authorizationCode);

    /**
     * Gets the OAuth authorization URL for initiating the OAuth flow.
     * The user's browser should navigate to this URL.
     */
    String getAuthorizationUrl();
}
