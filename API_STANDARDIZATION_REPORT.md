# API Standardization Report

## Files Changed

- `src/main/java/com/duoq/medlearn/domain/dto/common/PagedResponse.java`
- `src/main/java/com/duoq/medlearn/domain/dto/common/ErrorResponse.java`
- `src/main/java/com/duoq/medlearn/exception/GlobalExceptionHandler.java`
- `src/main/java/com/duoq/medlearn/domain/dto/ai/SymptomCheckerRequest.java`
- `src/main/java/com/duoq/medlearn/controller/AuthController.java`
- `src/main/java/com/duoq/medlearn/controller/AdminController.java`
- `src/main/java/com/duoq/medlearn/controller/DiseaseController.java`
- `src/main/java/com/duoq/medlearn/controller/DiseaseVersionController.java`
- `src/main/java/com/duoq/medlearn/controller/DiseaseSectionController.java`
- `src/main/java/com/duoq/medlearn/controller/CategoryController.java`
- `src/main/java/com/duoq/medlearn/controller/SymptomController.java`
- `src/main/java/com/duoq/medlearn/controller/CaseStudyController.java`
- `src/main/java/com/duoq/medlearn/controller/SymptomCheckerController.java`
- `src/main/java/com/duoq/medlearn/controller/UserController.java`
- `src/main/java/com/duoq/medlearn/controller/NotificationController.java`
- `src/main/java/com/duoq/medlearn/controller/AiStreamController.java`
- `src/main/java/com/duoq/medlearn/service/impl/SymptomServiceImpl.java`
- `docs/API_CONTRACT.md`
- `docs/FRONTEND_GUIDE.md`

## Endpoints Audited

All controllers were audited:

- `AdminController`
- `AiStreamController`
- `AuthController`
- `CaseStudyController`
- `CategoryController`
- `DiseaseController`
- `DiseaseSectionController`
- `DiseaseVersionController`
- `NotificationController`
- `SymptomCheckerController`
- `SymptomController`
- `UserController`

## Inconsistencies Fixed

- Added `PagedResponse<T>` wrapper and replaced raw `Page<T>` API responses in controllers.
- Added standard `ErrorResponse` with `timestamp`, `status`, `error`, `message`, and `path`.
- Updated `GlobalExceptionHandler` to return the unified error format.
- Standardized disease search query parameter to `keyword`.
- Kept symptom search backward compatibility by accepting both `keyword` and `q`.
- Wrapped symptom checker raw `List<Long>` request body into `SymptomCheckerRequest`.
- Added validation to symptom checker request.
- Changed `AuthController.logout` from `HttpServletRequest` parsing to `@RequestHeader` binding.
- Added class-level `/api/users` mapping to `UserController` while preserving endpoint paths.
- Added OpenAPI tags and operation summaries/descriptions to controllers.
- Documented SSE endpoints as explicit exceptions to `ApiResponse` wrapping.

## Response Standard

Successful JSON APIs return:

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-07-02T10:00:00"
}
```

Paginated APIs return `ApiResponse<PagedResponse<T>>`.

Errors return:

```json
{
  "timestamp": "2026-07-02T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation message",
  "path": "/api/example"
}
```

## Validation Improvements

- Ensured controller request bodies use `@Valid` where DTO validation exists.
- Added `@NotEmpty` validation to `SymptomCheckerRequest.symptomIds`.
- Preserved existing meaningful validation messages.

## OpenAPI Improvements

- Added `@Tag` to controllers.
- Added `@Operation` summaries/descriptions to endpoints.
- Existing bearer auth OpenAPI config remains in `OpenApiConfig`.

## Remaining Recommendations

- Add detailed `@ApiResponse` response code annotations and request examples for every endpoint.
- Add upload/document/flashcard/quiz endpoints only when product requirements exist; no such APIs exist currently.
- Consider replacing repository DTO projections with mapper-based service DTO conversion in a later architecture phase.
- Consider avoiding duplicate `/api/auth/me` and `/api/users/me` in a future non-breaking deprecation plan.
- Keep fully-qualified permission enum references in `@PreAuthorize`; short `T(PermissionCode)` can break Spring SpEL runtime type resolution.

## Verification

- Compile: `mvn -q -DskipTests compile` passed.
- Tests: `mvn -q test` passed.
- Lint: no dedicated lint command found in Maven configuration.
