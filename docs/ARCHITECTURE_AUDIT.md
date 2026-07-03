# Backend Architecture Audit

## Critical

None found.

## High

- Controllers inject repositories directly: `SymptomController`, `CategoryController`, `CaseStudyController`.
- Controllers contain business logic, persistence access, entity creation, and manual DTO mapping in symptom/category/case-study flows.
- Manual Entity to DTO mapping exists where MapStruct convention already exists via `UserMapper` and `DiseaseMapper`.
- Multiple service read methods lack `@Transactional(readOnly = true)`: `AdminUserServiceImpl`, `DiseaseVersionServiceImpl`, `DiseaseSectionServiceImpl`, `SymptomCheckerServiceImpl`, `CustomUserDetailsServiceImpl`.
- `RoleServiceImpl` contains stub/dead behavior returning `null`, empty methods, or `false`.

## Medium

- Fully-qualified enum references appear inside many `@PreAuthorize` annotations.
- `DiseaseRepository` returns DTO projections, coupling repository layer to API DTOs.
- Disease version/workflow clone logic is duplicated across services.
- Exception handling uses raw `RuntimeException` in controllers and message-substring checks in `GlobalExceptionHandler`.
- `AuthController` parses authorization header via `HttpServletRequest` instead of request header binding.
- `NotificationServiceImpl` exposes Spring MVC `SseEmitter` from service layer.
- Permission rules exist both in controllers and services, risking policy drift.

## Low

- No field injection found in main source.
- No service directly accesses `HttpServletRequest`.
- Unused private helpers likely exist in `DiseaseVersionServiceImpl`.
- Package naming places DTOs under `domain.dto`, not a strict API/application boundary package.
- `SymptomCheckerService` and `SymptomCheckerServiceV1` coexist; versioning intent should be clarified later.

## Safe Phase 1 Fixes

- Move symptom/category/case-study controller logic into services.
- Add MapStruct mappers for symptom, category, and case-study DTOs.
- Add transaction annotations to new service methods and low-risk existing service methods.
- Replace fully-qualified permission enum references where edited.
- Keep repository query signatures and endpoint contracts unchanged.
- Avoid moving packages in Phase 1 unless needed for correctness.
