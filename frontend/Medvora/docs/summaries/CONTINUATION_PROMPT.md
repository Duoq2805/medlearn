# Continuation Prompt

## Context

*   **Project**: Medvora - AI-powered medical learning platform.
*   **Technology Stack**: Java 21+, Spring Boot 3.x, PostgreSQL, JWT, OAuth2.
*   **Architecture**: Layered (Controller, Service, Repository), DTO pattern, `ApiResponse` wrapper.
*   **Key Modules**: Authentication, Disease Management, Disease Versioning, Reviewer Moderation, AI Features.
*   **Documentation**: Markdown in `/docs` for reusable AI memory.

## Current Real State

*   **Application Startup**: Environment variable issues resolved with default values in `application-dev.yml`, `application-docker.yml`, `application-prod.yml` and test configuration in `src/test/resources/application-test.yml`.
*   **Tests**: `DiseaseVersionServiceImplTest` passes. `DiseaseWorkflowIntegrationTest` fails due to transaction rollback exception.
*   **Permissions**: Dynamic permission system is implemented, active, and used in controllers via `PermissionCode`.
*   **Current Blocker**: `UnexpectedRollbackException` in `DiseaseWorkflowIntegrationTest` caused by nested transaction behavior between the test's `@Transactional` and `AuditServiceImpl.log()` with `REQUIRES_NEW` propagation.

## Core Principles for Continuation

1.  **Prioritize Fixing Test Blockers**: `mvn clean test` passing is a prerequisite for further development.
2.  **Avoid Config Changes**: Environment configuration is stabilized.
3.  **Documentation Accuracy**: Document only the current state; update summaries when blockers are cleared.
4.  **Architectural Consistency**: Maintain layered architecture and permission system.

## Strategic Directives

1.  **Investigate `UnexpectedRollbackException`**: Trace the transaction conflict between `DiseaseWorkflowIntegrationTest` (outer transaction) and `AuditServiceImpl` (inner `REQUIRES_NEW` transaction). The audit logging exception is caught, but marks the outer transaction for rollback.
2.  **Refactor or Mock AuditService**: If needed for tests, consider mocking `AuditService` in the integration test or adjusting the transaction propagation strategy.
3.  **Verify Permissions**: Ensure all permission codes are covered by tests.

## Session Continuation Plan

-   **Immediate Action**: Debug transaction handling in `DiseaseWorkflowIntegrationTest` to allow `mvn clean test` to pass.
-   **Next Focus**: Once tests pass, continue with feature development and documentation updates.

This continuation prompt serves as a guide to maintain focus and consistency for future development efforts.