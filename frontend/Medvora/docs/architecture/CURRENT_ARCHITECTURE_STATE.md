# Current Architecture State

## Overview
Medvora backend follows a clean layered architecture with separation of concerns. The system implements a medical learning platform with disease management, user authentication, reviewer moderation, and AI-assisted features.

## Layered Architecture
```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```
- Controllers: Handle HTTP requests, delegate to services, remain thin
- Services: Contain business logic, use `@Transactional` for writes
- Repositories: Extend `JpaRepository`, handle data access only
- Database: PostgreSQL with JPA/Hibernate

## Core Modules

### 1. Authentication & Security
- JWT-based stateless authentication
- OAuth2 (Google) integration
- Spring Security configuration
- Role-based and permission-based authorization
- Password encryption, email verification, reset tokens
- Token refresh mechanism
- Rate limiting for brute force protection

### 2. Disease Management
- CRUD operations for diseases
- Soft delete (`deletedAt`) and optimistic locking (`@Version`)
- Versioning system for content moderation
- Search and filtering capabilities
- Disease sections for structured content

### 3. Disease Versioning Workflow
- Disease → DiseaseVersion (on edit) → Reviewer Moderation → Approve/Reject → Live
- Preserves history of all versions
- Approved versions become public; rejected remain archived
- Full audit trail maintained

### 4. Reviewer Workflow
- Reviewers verify medical accuracy and educational quality
- Moderate AI-generated content
- Approve/reject disease versions
- System prevents unsafe medical information from going live

### 5. AI Features
- Symptom checker for preliminary assessments
- AI-assisted learning support
- Streamed responses for real-time interaction
- Designed to assist, not replace medical professionals
- Avoids unsafe medical advice

### 6. User & Role Management
- User profiles, roles, and sessions
- Role-based access control (RBAC)
- Permission system via `Permission` entity and `PermissionCode` enum
- Dynamic permission assignment to roles

### 7. Notification System
- Email notifications (registration, password reset, updates)
- Event-driven notification service
- Template-based email content

## Key Technical Decisions

### Data Modeling
- All entities use:
  - `@CreationTimestamp` and `@UpdateTimestamp`
  - `OffsetDateTime` for time fields
  - `@Enumerated(EnumType.STRING)` for enums
  - Soft delete support (`deletedAt`)
  - Optimistic locking (`@Version`)
  - `@Builder.Default` for default values

### API Design
- Consistent response wrapper: `ApiResponse<T>`
- Standard envelope: `ResponseEntity<ApiResponse<T>>`
- Proper HTTP status codes
- Predictable JSON structures
- Jakarta Validation for request DTOs
- Builder pattern for response DTOs
- MapStruct for entity-DTO mapping

### Security Implementation
- `JwtService` handles token creation/validation
- Passwords encrypted with BCrypt
- OAuth2 login success handler for user creation
- Method-level security planned via `@PreAuthorize`
- Permission-based authorization checked in services
- Secrets externalized via environment variables

### Infrastructure
- Maven build system
- Spring Boot DevTools for development
- Environment-specific configuration (`application-{profile}.yml`)
- No hardcoded secrets; all via env vars or config files
- Test configuration missing (blocker)

## Current Dependencies
- Spring Boot Starter Web, Security, Data JPA
- PostgreSQL Driver
- Lombok (reduces boilerplate)
- MapStruct (object mapping)
- Jakarta Validation
- Springdoc OpenAPI (for API docs - inferred)
- Java Mail (email service)

## Architecture Rules Enforcement
- Controllers never contain business logic
- Services never return entities directly (use DTOs)
- Repositories only perform data access
- Constructor injection used throughout
- DTOs used for all API communication
- MapStruct mapper per domain entity
- Shared `MapStructConfig` for common mappings

## Known Gaps & TODOs
- Missing `application-test.yml` blocks test execution
- Environment variables required for startup (6 critical vars)
- Method-level security annotations not yet widely applied
- AI features could be expanded (explanations, study guides)
- Performance optimization (query optimization, caching)
- Additional audit trails for sensitive operations
- API documentation completeness (Swagger/OpenAPI)

## Directory Structure
```
src/main/java/com/duoq/medlearn/
├── config/                 # Security & Spring config
├── controller/             # REST endpoints (6 controllers)
├── domain/                 # Business logic core
│   ├── entity/             # JPA entities (14 entities)
│   └── enums/              # Java enums (7 enums)
├── dto/                    # Data Transfer Objects
│   ├── request/            # Input DTOs with validation
│   └── response/           # Output DTOs with builder
├── exception/              # Custom exceptions & handler
├── mapper/                 # MapStruct mappers
├── repository/             # Spring Data repositories
├── security/               # JWT, OAuth2, user details
├── service/                # Service interfaces
│   └── impl/               # Service implementations
├── util/                   # Utility classes
└── ai/                     # AI-related components
```

## Current Branch Information
- Branch: `disease` (feature branch for disease management enhancements)
- Base branch: `main`
- Recent commits show focus on:
  - Authentication improvements (permission-based auth)
  - Disease controller implementation
  - Externalizing secrets (security/api refactor)
  - Cleanup of temporary directories

## Relationship to Documentation
This file serves as the current architectural reference, complementing:
- `backend-summary.md` (general backend overview)
- `auth-architecture.md` (detailed security architecture)
- `disease-versioning.md` and `reviewer-workflow.md` (workflow specifics)
- `coding-rules.md` (development standards)
- Session summaries in `/docs/summaries/` for temporal context

## Recommended Actions
1. Resolve environment variable blocker to enable local development and testing
2. Create missing `application-test.yml` for test suite execution
3. Apply methodical security annotations (`@PreAuthorize`) to service methods
4. Update diagrams if significant architecture changes occur
5. Continue incremental feature development following established patterns