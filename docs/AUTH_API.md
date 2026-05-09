# IAM Service — Authentication API

Base URL: `/api/v1/iam`

All request/response bodies are JSON. All protected endpoints require an `Authorization: Bearer <accessToken>` header.

---

## TypeScript Types

```typescript
// ─── Enums ──────────────────────────────────────────────────────────────────

type Role = "ROLE_STUDENT" | "ROLE_TEACHER" | "ROLE_ADMIN";
type AuthProvider = "EMAIL" | "OAUTH";
type OAuthProvider = "google" | "github";

// ─── User ────────────────────────────────────────────────────────────────────

interface User {
  id: string;           // UUID
  email: string;
  nickname: string;
  picture: string;
  role: Role;
  authProvider: AuthProvider;
  createdAt: string;    // ISO 8601 datetime
}

interface PublicUser {
  id: string;
  nickname: string;
  picture: string;
  role: Role;
}

// ─── Auth responses ──────────────────────────────────────────────────────────

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  user: User;
}

interface TemporalTokenResponse {
  temporalToken: string;
}

// ─── Error response ──────────────────────────────────────────────────────────

interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;    // ISO 8601 datetime
}

// ─── JWT payloads ────────────────────────────────────────────────────────────

/** Decoded payload of an access token (temporal: false) */
interface AccessTokenPayload {
  sub: string;          // User UUID
  nickname: string;
  email: string;
  role: Role;
  temporal: false;
  iat: number;          // Unix seconds
  exp: number;          // Unix seconds — lifetime: 15 minutes
}

/** Decoded payload of a refresh token */
interface RefreshTokenPayload {
  sub: string;          // User UUID
  iat: number;
  exp: number;          // Unix seconds — lifetime: 7 days
}

/** Decoded payload of a temporal token (temporal: true) */
interface TemporalTokenPayload {
  sub: string;          // User UUID
  nickname: string;
  email: string;
  role: Role;
  temporal: true;
  iat: number;
  exp: number;          // Unix seconds — lifetime: 60 seconds
}
```

---

## 1. Sign Up

Register a new user with email and password.

**`POST /auth/sign-up`** — public

### Request body

```typescript
interface SignUpBody {
  email: string;      // valid email, must be unique
  nickname: string;   // 3–20 characters, must be unique
  password: string;   // minimum 8 characters
}
```

### Responses

| Status | Body | When |
|--------|------|------|
| `201 Created` | `AuthResponse` | Account created successfully |
| `400 Bad Request` | `ErrorResponse` | Validation failure, duplicate email or nickname |
| `500 Internal Server Error` | `ErrorResponse` | Unexpected error |

### Notes

- New users are assigned `ROLE_STUDENT` and `authProvider: "EMAIL"` by default.
- Passwords are stored as a BCrypt hash — the raw value is never persisted.
- Emits a `UserRegisteredEvent` to RabbitMQ (`iam.user.created`).

---

## 2. Sign In

Authenticate an existing email/password user.

**`POST /auth/sign-in`** — public

### Request body

```typescript
interface SignInBody {
  email: string;
  password: string;
}
```

### Responses

| Status | Body | When |
|--------|------|------|
| `200 OK` | `AuthResponse` | Credentials valid |
| `400 Bad Request` | `ErrorResponse` | Invalid credentials |
| `401 Unauthorized` | `ErrorResponse` | Account was created via OAuth — use the OAuth endpoint instead |
| `500 Internal Server Error` | `ErrorResponse` | Unexpected error |

---

## 3. Refresh Token

Exchange a valid refresh token for a new token pair.

**`POST /auth/refresh`** — public

### Request body

```typescript
interface RefreshTokenBody {
  refreshToken: string;
}
```

### Responses

| Status | Body | When |
|--------|------|------|
| `200 OK` | `AuthResponse` | New access + refresh tokens issued |
| `400 Bad Request` | `ErrorResponse` | Token is invalid, expired, or not recognised in the store |
| `500 Internal Server Error` | `ErrorResponse` | Unexpected error |

### Notes

- The old refresh token is revoked atomically when the new one is issued.
- Refresh tokens are stored server-side; a token that was not issued by this service will be rejected even if the signature is valid.

---

## 4. OAuth Sign In / Sign Up — Google & GitHub

Authenticate via an external OAuth provider. Creates an account on first use.

**`POST /auth/oauth/{provider}`** — public  
`{provider}` is `google` or `github` (case-insensitive).

### Request body

```typescript
interface OAuthBody {
  providerToken: string;  // Google: ID Token. GitHub: access token.
  nickname?: string;      // Optional — auto-derived from email if omitted
}
```

### How provider tokens are obtained

| Provider | Token type | How the client gets it |
|----------|-----------|------------------------|
| Google | ID Token (JWT) | `google.accounts.id.initialize` / `signIn()` popup — `credential` field in the response |
| GitHub | OAuth2 access token | GitHub OAuth App authorization code flow — exchange code at `https://github.com/login/oauth/access_token` |

### What this service fetches from the provider

**Google** — calls `GET https://oauth2.googleapis.com/tokeninfo?id_token={token}`  
Extracts: `email`, `name`, `picture`

**GitHub** — calls `GET https://api.github.com/user` with `Authorization: Bearer {token}`  
Extracts: `email`, `name`, `avatar_url`  
The GitHub account must have a **public** primary email set; requests with a blank email are rejected.

### Responses

| Status | Body | When |
|--------|------|------|
| `200 OK` | `AuthResponse` | Authenticated (new or existing OAuth user) |
| `400 Bad Request` | `ErrorResponse` | Invalid/expired provider token, or GitHub account has no public email |
| `409 Conflict` | `ErrorResponse` | Email already registered with an email/password account |
| `500 Internal Server Error` | `ErrorResponse` | Unexpected error |

### Notes

- OAuth users are created with `authProvider: "OAUTH"` and a `null` password.
- If the email already exists under `authProvider: "OAUTH"`, the user is logged in.
- If the email already exists under `authProvider: "EMAIL"`, the request is rejected with `409`.

---

## 5. Exchange Temporal Token

Convert a short-lived temporal token into a full access + refresh token pair. This is the second step of a cross-app auth handoff.

**`POST /auth/exchange-temporal-token`** — public

### Request body

```typescript
interface ExchangeTemporalTokenBody {
  temporalToken: string;
}
```

### Responses

| Status | Body | When |
|--------|------|------|
| `200 OK` | `AuthResponse` | Temporal token accepted; full tokens issued |
| `400 Bad Request` | `ErrorResponse` | Token is invalid, expired, or is not a temporal token |
| `500 Internal Server Error` | `ErrorResponse` | Unexpected error |

---

## 6. Logout

Revoke the current refresh token.

**`POST /auth/logout`** — protected (optional — works without a valid token too)

### Request headers

```
Authorization: Bearer <accessToken>
```

### Responses

| Status | Body | When |
|--------|------|------|
| `204 No Content` | — | Refresh token revoked |

### Notes

- Only the refresh token is revoked server-side. The access token continues to be valid until its natural expiry (15 min). For high-security contexts, add the access token to a server-side blacklist.

---

## 7. Issue Temporal Token

Issue a 60-second token for cross-app / cross-subdomain auth handoff.

**`POST /users/me/temporal-token`** — protected

### Request headers

```
Authorization: Bearer <accessToken>
```

### Responses

| Status | Body | When |
|--------|------|------|
| `200 OK` | `TemporalTokenResponse` | Temporal token issued |
| `401 Unauthorized` | — | Missing or invalid access token |

### Notes

- Temporal tokens carry `temporal: true` in their payload and expire after **60 seconds**.
- They are signed with the same secret as regular access tokens but are **rejected by the JWT filter** for any authenticated endpoint — they can only be used with `POST /auth/exchange-temporal-token`.
- Typical use-case: embed a one-time token in a redirect URL or iframe src to transfer auth state to another app/subdomain.

---

## Token Reference

### How to send a token

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Access token

| Property | Value |
|----------|-------|
| Algorithm | HS256 |
| Signing secret | `JWT_ACCESS_SECRET` (env var, Base64-encoded, min 32 bytes) |
| Lifetime | 15 minutes (`JWT_ACCESS_EXPIRATION_MS=900000`) |
| `temporal` claim | `false` |

### Refresh token

| Property | Value |
|----------|-------|
| Algorithm | HS256 |
| Signing secret | `JWT_REFRESH_SECRET` (separate env var) |
| Lifetime | 7 days (`JWT_REFRESH_EXPIRATION_MS=604800000`) |
| Storage | Server-side (`ConcurrentHashMap` keyed by user UUID) |

### Temporal token

| Property | Value |
|----------|-------|
| Algorithm | HS256 |
| Signing secret | `JWT_ACCESS_SECRET` (same as access token) |
| Lifetime | 60 seconds (hardcoded) |
| `temporal` claim | `true` |

---

## Error Response Shape

Every non-2xx response follows this structure:

```typescript
interface ErrorResponse {
  status: number;
  error: string;    // HTTP reason phrase, e.g. "Bad Request"
  message: string;  // Human-readable detail
  timestamp: string;
}
```

### Common status codes across all endpoints

| Code | Meaning |
|------|---------|
| `400` | Validation error, bad credentials, invalid/expired token |
| `401` | Wrong auth provider (e.g. email account used via OAuth endpoint) |
| `409` | Email already registered under a different provider |
| `500` | Unexpected server error |
