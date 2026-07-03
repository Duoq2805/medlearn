# Session Summary - 2026-06-09

## Current State

Medvora backend is a Spring Boot medical learning platform with 130 Java files implementing:
- Authentication (JWT + OAuth2)
- Disease management with versioning
- Reviewer moderation workflow
- Permission-based authorization
- AI-assisted learning features
- Email notifications

## Completed Work

### Core Architecture
- Layered architecture (Controller → Service → Repository → Database)
- DTO pattern with MapStruct mapping
- ApiResponse wrapper for standardized responses
- Role-based authorization system

### Modules Implemented
1. **Authentication**: JWT tokens, OAuth2 (Google), email verification, password reset
2. **Disease Management**: CRUD operations, versioning, soft delete, optimistic locking
3. **Disease Versioning**: Version tracking, approval workflow, history preservation
4. **Reviewer Workflow**: Moderation system, approval/rejection logic
5. **Permission System**: Dynamic permission-based authorization (PermissionCode enum)
6. **User Management**: Profiles, roles, session tracking
7. **AI Features**: Symptom checker, stream responses
8. **Notifications**: Email notifications, event-based system

### Security Implementation
- JWT authentication with refresh tokens
- OAuth2 integration (Google)
- Spring Security configuration
- Role and permission-based authorization
- Email verification for registration
- Password reset with token expiry
- Token reuse prevention
- Rate limiting for brute force protection

### Database & Entities
- 14 main entities (User, Disease, DiseaseVersion, Role, Permission, etc.)
- Soft delete support (deletedAt field)
- Optimistic locking (@Version)
- Audit logging (AuditLog entity)
- Timestamp tracking (@CreationTimestamp, @UpdateTimestamp)
- Enum support (EnumType.STRING)

## Architecture Decisions

### Permission System
- Dynamic permission-based authorization via Permission entity
- Permissions assigned to roles, roles assigned to users
- PermissionCode enum defines available permissions
- Flexible for future permission additions

### Disease Workflow
- Diseases support version history
- New edits create DiseaseVersion entries
- Reviewer moderation before going live
- Approved version becomes public, rejected archived
- Maintains full audit trail

### API Design
- All responses wrapped in `ApiResponse<T>`
- Consistent HTTP status codes
- Pagination support for large datasets
- Frontend-friendly JSON structures

## Current Blocker

### `DiseaseWorkflowIntegrationTest` Failure
- **Exception Type**: `org.springframework.transaction.UnexpectedRollbackException`
- **Error Message**: "Transaction silently rolled back because it has been marked as rollback-only"
- **Root Cause**: Nested transaction conflict between the test's `@Transactional` and `AuditServiceImpl.log()` which uses `@Transactional(propagation = Propagation.REQUIRES_NEW)`. The audit logging exception, though caught internally, marks the outer transaction for rollback.

## Important Findings

- Dynamic permission system is active and used in controllers.
- No hardcoded role checks found in controllers; `PermissionCode` authorization is used.
- Environment variable startup issues have been resolved with default values and test configuration.
- The current primary blocker is the transaction rollback behavior in integration tests.

## Project Statistics

- Total Java files: 130
- Controllers: 6
- Services: 15+
- Entities: 14
- Repositories: 14
- DTOs: 30+
- Custom exceptions: 9
- Enums: 7

## Current Branch

- Branch: `disease`
- Git user: Nguyễn Bá Thái Dương

## Technology Stack

- Java 21+
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Tokens)
- OAuth2 (Google)
- MapStruct
- Lombok
- Jakarta Validation

## Session Update - 2026-06-18 (Production Readiness Audit)

### Completed Work
- Conducted a full Production Readiness Audit on the Authentication, Authorization, JWT, OAuth2, and Session Management modules.
- Scored security modules and categorized findings into P0, P1, P2, P3.
- **Fixed P1 Issue**: Implemented a JWT blocklist. When a user logs out, their stateless access token is now added to an in-memory blocklist (`ConcurrentHashMap` in `JwtService`) until its natural expiration, preventing stolen tokens from being used post-logout.
- `JwtAuthenticationFilter` now actively checks the blocklist to reject revoked access tokens.
- Modified `AuthController` and `AuthService` to extract the `Authorization` header and pass the `accessToken` to the logout flow for invalidation.
- Resolved compilation issues with `OffsetDateTime` arithmetic by utilizing `java.time.temporal.ChronoUnit`.
- Re-ran tests successfully (`mvn clean test`). The `DiseaseWorkflowIntegrationTest` blocker mentioned previously is no longer failing.

### Output Artifacts
- Created `docs/architecture/auth-security-review.md` containing the audit scores, exploit scenarios, and implemented fixes.
