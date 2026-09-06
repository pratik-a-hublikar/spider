# Shipmnts OAuth 2.0 Authorization Code Flow Implementation

## Overview

This document describes the OAuth 2.0 authorization code flow implemented for Shipmnts authentication. This is the recommended and most secure approach for handling third-party authentication.

## Architecture

### User Flow

1. User clicks "Fetch" button on the home page
2. A dialog appears with the option to "Open Shipmnts Login"
3. Clicking the button opens a popup window to the Shipmnts authorization URL
4. User enters Shipmnts username and password in the popup (handles CAPTCHA/MFA)
5. After successful authentication, Shipmnts redirects to `/auth/callback?code=<authorization_code>&state=<state>`
6. The callback page exchanges the code for tokens with the backend
7. The backend stores the refresh token in memory and `.env`
8. The callback page notifies the parent window (home page) via postMessage
9. The popup automatically closes
10. The parent window reloads to reflect the new authentication state
11. User can now use the `/fetch` API to get Shipmnts profile data

### Components

#### Backend

**Configuration** (`application.yml`)
- `shipmnts.oauth-authorize-url`: Shipmnts authorization endpoint (e.g., `https://auth.shipmnts.com/`)
- `shipmnts.oauth-token-url`: Token exchange endpoint (e.g., `https://auth.api.shipmnts.com/user_management/authenticate`)
- `shipmnts.oauth-redirect-uri`: Where Shipmnts redirects after login (e.g., `http://localhost:5173/auth/callback`)
- `shipmnts.client-id`: OAuth client ID from Shipmnts
- `shipmnts.client-secret`: OAuth client secret from Shipmnts

**Service** (`ShipmntsService`)
- `getAuthorizationUrl()`: Generates the OAuth authorization URL
- `exchangeAuthorizationCode(code)`: Exchanges authorization code for tokens

**Controller** (`ShipmntsController`)
- `GET /shipmnts/oauth/authorize-url`: Returns the authorization URL
- `POST /shipmnts/oauth/callback`: Handles the OAuth callback (exchanges code for tokens)

#### Frontend

**Pages**
- `AuthCallbackPage`: Handles the OAuth redirect and exchanges code for tokens

**Components**
- `ShipmntsAuthDialog`: Dialog with "Open Shipmnts Login" button

**Services**
- `shipmntsService.getAuthorizationUrl()`: Fetches the authorization URL
- `shipmntsService.exchangeAuthorizationCode(code)`: Exchanges code for token

**Routes**
- `/auth/callback`: Public route for OAuth callback (no auth required)

## Setup Instructions

### 1. Environment Configuration

Update `.env` with Shipmnts OAuth credentials:

```env
SHIPMNTS_CLIENT_ID=your_client_id_from_shipmnts
SHIPMNTS_CLIENT_SECRET=your_client_secret_from_shipmnts
SHIPMNTS_OAUTH_AUTHORIZE_URL=https://auth.shipmnts.com/
SHIPMNTS_OAUTH_TOKEN_URL=https://auth.api.shipmnts.com/user_management/authenticate
SHIPMNTS_OAUTH_REDIRECT_URI=http://localhost:5173/auth/callback
```

### 2. Shipmnts Configuration

Register your redirect URI with Shipmnts:
- Navigate to your Shipmnts application settings
- Add `http://localhost:5173/auth/callback` (or your production URL) as an authorized redirect URI
- Ensure `client_id` and `client_secret` are correctly set

### 3. Backend Configuration

The OAuth configuration is automatically loaded from `.env` via `application.yml`.

### 4. Frontend Routes

The route `/auth/callback` is already configured and public (no authentication required).

## Usage Flow

### Step 1: User Initiates Authentication

```javascript
// User clicks "Fetch" button in the app
// This opens ShipmntsAuthDialog
```

### Step 2: Dialog Opens Popup

```javascript
// User clicks "Open Shipmnts Login" button
const authUrl = await shipmntsService.getAuthorizationUrl();
window.open(authUrl, 'shipmnts_auth', 'width=500,height=600,...');
```

The authorization URL is formatted like:
```
https://auth.shipmnts.com/?client_id=CLIENT_ID&redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fauth%2Fcallback&response_type=code&state=UUID
```

### Step 3: Shipmnts Login

The popup window displays the Shipmnts login page where the user:
- Enters username and password
- Completes any CAPTCHA or MFA challenges
- Authorizes the application to access their profile

### Step 4: Shipmnts Redirects

After successful authentication, Shipmnts redirects the popup to:
```
http://localhost:5173/auth/callback?code=AUTHORIZATION_CODE&state=UUID
```

### Step 5: Callback Page Exchanges Code

The `AuthCallbackPage` component:
1. Extracts the `code` and `error` from the URL
2. Sends the code to the backend: `POST /api/v1/identity/shipmnts/oauth/callback`
3. The backend exchanges the code for tokens
4. Receives the refresh token in the response
5. Notifies the parent window via postMessage: `SHIPMNTS_AUTH_SUCCESS`
6. Closes the popup automatically

### Step 6: Parent Window Notified

The parent window receives the `SHIPMNTS_AUTH_SUCCESS` message and:
1. Displays a success message
2. Reloads the page to reflect the new authentication state
3. The new refresh token is now available in the backend

## API Endpoints

### GET /api/v1/identity/shipmnts/oauth/authorize-url

**Authentication**: Required (Bearer token)

**Response**:
```json
{
  "success": true,
  "data": {
    "authorizeUrl": "https://auth.shipmnts.com/?client_id=...&redirect_uri=...&response_type=code&state=..."
  },
  "message": "success"
}
```

### POST /api/v1/identity/shipmnts/oauth/callback

**Authentication**: Not required (public endpoint for OAuth callback)

**Request**:
```json
{
  "code": "AUTHORIZATION_CODE_FROM_SHIPMNTS"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "refreshToken": "new_refresh_token_value"
  },
  "message": "success"
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "error description",
  "data": null
}
```

## Security Considerations

### HTTPS in Production

Ensure all OAuth flows use HTTPS in production:
- Authorization URL must use HTTPS
- Redirect URI must use HTTPS
- Backend API endpoints must use HTTPS
- Update `oauth-redirect-uri` configuration for production domain

### State Parameter Validation

The implementation generates a random UUID as the `state` parameter. In production, you should:
1. Store the state value on the backend (in session or cache)
2. Validate that the returned state matches the stored value
3. This prevents CSRF attacks where an attacker tricks a user into clicking a malicious link

Current implementation (basic):
```java
String state = UUID.randomUUID().toString();
```

Enhanced implementation (recommended for production):
```java
// Generate state
String state = UUID.randomUUID().toString();
// Store in Redis or session
redisTemplate.opsForValue().set("oauth_state_" + state, "pending", Duration.ofMinutes(10));

// On callback, validate
String storedState = redisTemplate.opsForValue().get("oauth_state_" + returnedState);
if (storedState == null) {
    throw new Exception("Invalid state parameter");
}
```

### Client Secret Protection

Never expose the `client_secret` in frontend code. It's configured on the backend only and used during the token exchange (`POST /oauth/callback`).

### Token Storage

- The refresh token is stored in `.env` for persistence
- Also stored in memory (`ShipmntsServiceImpl.refreshToken`) for immediate use
- Access tokens are obtained on-demand when needed (not persisted)

### CORS and Origin Validation

The callback page validates the window origin:
```javascript
if (event.origin !== window.location.origin) return; // Ignore messages from other origins
```

## Troubleshooting

### Popup Not Opening

**Cause**: Browser may block popup

**Solution**:
- Check browser popup blocker settings
- Ensure the button click directly triggers the popup (not async after a delay)
- Add a user interaction handler

### Popup Closes But No Token Received

**Cause**: Backend token exchange failed

**Solution**:
1. Check backend logs for OAuth exchange errors
2. Verify Shipmnts credentials are correct
3. Verify redirect URI matches Shipmnts configuration
4. Ensure `client_secret` is correct

### "Invalid state parameter" Error

**Cause**: State validation failed (if implemented)

**Solution**:
- Clear Redis/cache and try again
- Check backend logs for state mismatch details
- Ensure Redis is running (if using Redis for state storage)

### CORS Errors

**Cause**: Cross-origin request blocked

**Solution**:
1. Ensure backend has CORS configured
2. Add backend URL to browser console to see exact error
3. Verify `oauth-redirect-uri` matches frontend domain

### Token Not Persisted

**Cause**: `.env` file permission or path issue

**Solution**:
1. Verify `.env` exists and is writable
2. Check backend logs for file I/O errors
3. Ensure backend process has permission to write to `.env`
4. Check that the path to `.env` is correctly resolved

## Comparison: OAuth vs Script Flow

| Aspect | OAuth | Script (Playwright) |
|--------|-------|-------------------|
| Security | ✅ Excellent | ⚠️ Uses credentials in process |
| User Experience | ✅ Native browser login | ⚠️ Browser automation |
| CAPTCHA/MFA | ✅ Automatic (user solves in browser) | ⚠️ Requires manual completion |
| Scalability | ✅ Stateless, can scale easily | ⚠️ Process-intensive |
| Complexity | ⚠️ More setup | ✅ Simple |
| Browser Requirement | ✅ Uses user's browser | ⚠️ Requires Chrome/Chromium |
| Server Display | ✅ Not required | ⚠️ May require display server |
| Production Ready | ✅ Yes | ⚠️ Depends on requirements |

## Future Enhancements

1. **Refresh Token Rotation**: Implement automatic refresh token refresh
2. **State Validation**: Add Redis-backed state validation for CSRF protection
3. **Device Flow**: Support device authorization flow for CLI/server scenarios
4. **Token Expiration Handling**: Automatically refresh expired access tokens
5. **Multi-Account Support**: Allow users to manage multiple Shipmnts accounts

## References

- [OAuth 2.0 Authorization Code Flow](https://tools.ietf.org/html/rfc6749#section-1.3.1)
- [Shipmnts API Documentation](https://docs.shipmnts.com)
- [OWASP OAuth 2.0 Security Best Practices](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics)

---

**Version**: 1.0  
**Last Updated**: September 2026

