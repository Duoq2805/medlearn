# Project Summary

Medvora is an AI-powered medical learning platform for university students.

## Core Architecture & Completed Work
- **Layered Architecture**: Controller → Service → Repository → Database.
- **Authentication**: JWT, OAuth2 (Google), email verification, password reset.
- **Disease Management**: CRUD, versioning, soft delete, optimistic locking.
- **Reviewer Workflow**: Moderation, approval/rejection system.
- **Permission System**: Dynamic, role-based, using `PermissionCode` enum.
- **AI Features**: Symptom checker, streaming responses.
- **Security**: Spring Security, role/permission checks, rate limiting.
- **Data Modeling**: 14 entities with soft delete, audit logging, timestamps.
- **API Standards**: `ApiResponse<T>` wrapper, `ResponseEntity`, consistent status codes.
- **Code Quality**: DTO pattern, MapStruct, Lombok, Jakarta Validation.
- **Environment Configuration**: Resolved startup blockers with default values and test configuration.
- **Test Verification**: `mvn clean test` now passes for `DiseaseVersionServiceImplTest`.
- **Documentation**: Comprehensive markdown documentation in `/docs` for reusable AI memory.

## Current Blocker

- **`DiseaseWorkflowIntegrationTest` Failure**: An `UnexpectedRollbackException` occurs due to a nested transaction conflict between the test's `@Transactional` context and `AuditServiceImpl.log()` (which uses `Propagation.REQUIRES_NEW`). This causes the test transaction to be marked as rollback-only.

## Important Findings

- The dynamic permission system is fully implemented and active in controllers.
- Controllers utilize `PermissionCode` for authorization, avoiding hardcoded role checks.
- Environment variable startup issues are resolved; the primary blocker is now the transaction rollback in integration tests.

## Project Statistics

- Java Files: 130
- Controllers: 6
- Services: 15+
- Entities: 14
- Repositories: 14
- DTOs: 30+

## Technology Stack

- Java 21+
- Spring Boot 3.x
- Spring Security
- PostgreSQL
- JWT, OAuth2
- MapStruct, Lombok

## Repository Intelligence

- CLAUDE.md: Global behavior standards.
- `/docs`: Reusable AI memory (architecture, workflows, summaries, rules).
- `/diagrams`: Visual architecture representations.
