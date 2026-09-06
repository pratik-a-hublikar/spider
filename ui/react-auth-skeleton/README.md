# React Auth Skeleton

A small React application skeleton with:

- React + Vite
- Material UI (free/open-source core package)
- Redux Toolkit + React Redux
- React Router
- Axios
- Backend login API integration
- Session restore through `/auth/me`
- Protected `/home` route
- Logout
- Authentication state stored in a Redux reducer

## Project structure

```text
src/
  app/
    store.js
  components/
    ProtectedRoute.jsx
  features/
    auth/
      authSlice.js
  layouts/
    AppLayout.jsx
  pages/
    LoginPage.jsx
    HomePage.jsx
  services/
    apiClient.js
    authService.js
  theme/
    theme.js
  App.jsx
  main.jsx
```

## Setup

1. Copy `.env.example` to `.env`.
2. Change the backend URL/endpoints if needed.
3. Install dependencies:

```bash
npm install
```

4. Start the app:

```bash
npm run dev
```

5. Production build:

```bash
npm run build
```

## Expected backend APIs

### Login

Default request:

```http
POST /api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "demo",
  "password": "password"
}
```

Supported response shapes include:

```json
{
  "accessToken": "jwt-token",
  "user": {
    "id": 1,
    "username": "demo",
    "name": "Demo User"
  }
}
```

or:

```json
{
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "username": "demo"
    }
  }
}
```

### Validate existing session

Default request:

```http
GET /api/auth/me
Authorization: Bearer <token>
```

Example response:

```json
{
  "user": {
    "id": 1,
    "username": "demo",
    "name": "Demo User"
  }
}
```

If `/auth/me` returns an error, the stored token is removed and the user is redirected to `/login`.

## Environment variables

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_LOGIN_ENDPOINT=/auth/login
VITE_ME_ENDPOINT=/auth/me
```

## Authentication behavior

- Login form dispatches the `loginUser` Redux async thunk.
- The thunk calls the backend through `authService`.
- On success, the token is stored in `localStorage` and Redux state is updated.
- Axios automatically adds the token as a Bearer token to subsequent requests.
- On browser refresh, the token is checked against `/auth/me` before protected content is shown.
- `/home` is wrapped by `ProtectedRoute`; an unauthenticated visitor is redirected to `/login`.
- Logout clears the token and Redux authentication state.

## Production security note

This skeleton stores a token in `localStorage` for simplicity. For a production application, consider an HttpOnly/Secure/SameSite cookie-based session if your backend architecture supports it, and enforce authorization on every backend endpoint. Client-side route protection improves UX but is not a substitute for backend authorization.
