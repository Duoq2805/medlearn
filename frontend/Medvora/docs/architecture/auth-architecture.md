# Authentication Architecture

Medvora uses JWT-based stateless authentication.

## Auth Flow

```
Login Request
-> Rate limit check (LoginAttemptService)
-> AuthenticationManager / AuthServiceImpl
-> JwtService generates access token + refresh token
-> UserSession saved (refresh token)
-> tokens returned to client
-> JwtAuthenticationFilter validates token on each request
-> SecurityContext stores authenticated user
```

## Security Stack

- Spring Security
- JWT (HS256, jjwt 0.12.6)
- Stateless sessions (`STATELESS`)
- Role-Based Authorization (`@PreAuthorize`)
- Method-Level Security (`@EnableMethodSecurity`)
- In-memory rate limiting (LoginAttemptServiceImpl)

## Main Components

| Component | Location |
|---|---|
| AuthController | `controller/AuthController.java` |
| AuthService/Impl | `service/`, `service/impl/` |
| JwtService | `config/JwtService.java` |
| JwtAuthenticationFilter | `config/JwtAuthenticationFilter.java` |
| SecurityConfig | `config/SecurityConfig.java` |
| LoginAttemptService/Impl | `service/`, `service/impl/` |
| EmailServiceImpl | `service/impl/` |

## Auth Endpoints

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| GET | `/api/auth/verify` | Public |
| POST | `/api/auth/resend-verification` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/refresh` | Public |
| POST | `/api/auth/logout` | Public |
| GET | `/api/auth/me` | Authenticated |
| POST | `/api/auth/forgot-password` | Public |
| POST | `/api/auth/reset-password` | Public |

## Authorization Roles

- USER
- REVIEWER
- ADMIN

## Security Rules

- Never bypass JWT validation
- Never hardcode secrets (use `@Value`)
- Timing-safe login (always run `passwordEncoder.matches()`)
- Refresh token reuse detection → revoke all sessions
- Rate limit login by email key (5 attempts / 15min block)
- Password reset tokens: 1-hour expiry, single-use
- JWT error types differentiated in filter (ExpiredJwtException vs JwtException)
- Sensitive exceptions: same response for not-found vs invalid token (prevent enumeration)

## Exception Map

| Exception | HTTP |
|---|---|
| InvalidCredentialsException | 400 |
| EmailNotVerifiedException | 400 |
| AccountDeactivatedException | 400 |
| TokenReusedException | 400 |
| InvalidTokenException | 401 |
| RateLimitExceededException | 429 |
| ResourceNotFoundException | 404 |
| AccessDeniedException | 403 |

## API Behavior

- All auth responses use `ApiResponse<T>` wrapper
- Validation errors joined with `"; "` separator
- Forgot-password / resend-verification: same response regardless of email existence (security)
- JWT filter: differentiated catch (Expired / Malformed / Signature / Generic) → `SecurityContextHolder.clearContext()`, pass to chain

## Philosophy

Authentication architecture should remain stable and reusable across future features.
