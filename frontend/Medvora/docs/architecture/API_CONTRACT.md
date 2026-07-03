# API Contract

## Standard Success Response

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-07-02T10:00:00"
}
```

## Standard Error Response

```json
{
  "timestamp": "2026-07-02T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation message",
  "path": "/api/example"
}
```

## Pagination Response

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "last": true,
    "first": true
  },
  "timestamp": "2026-07-02T10:00:00"
}
```

List endpoints accept Spring pagination parameters: `page`, `size`, `sort`.

## Authentication

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| POST | `/api/auth/register` | No | `RegisterRequest` | `ApiResponse<Void>` |
| POST | `/api/auth/login` | No | `LoginRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/refresh` | No | `{ refreshToken }` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/logout` | Yes | optional `{ refreshToken }`, optional `Authorization` header | `ApiResponse<Void>` |
| GET | `/api/auth/me` | Yes | none | `ApiResponse<UserDTO>` |
| GET | `/api/auth/verify?token=` | No | query token | `ApiResponse<Void>` |
| GET | `/api/auth/verify-email/{token}` | No | path token | `ApiResponse<Void>` |
| POST | `/api/auth/resend-verification` | No | `{ email }` | `ApiResponse<Void>` |
| POST | `/api/auth/forgot-password` | No | `{ email }` | `ApiResponse<Void>` |
| POST | `/api/auth/reset-password` | No | `{ token, newPassword }` | `ApiResponse<Void>` |

## Users

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| GET | `/api/users/me` | Yes | none | `ApiResponse<UserDTO>` |
| PUT | `/api/users/me` | Yes | `UpdateProfileRequest` | `ApiResponse<UserDTO>` |

## Admin

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| GET | `/api/admin/users?page=&size=&sort=` | Yes | pagination | `ApiResponse<PagedResponse<UserDTO>>` |
| GET | `/api/admin/users/{id}` | Yes | path id | `ApiResponse<UserDTO>` |
| PATCH | `/api/admin/users/{userId}/role` | Yes | `{ roleName }` | `ApiResponse<Void>` |
| PATCH | `/api/admin/users/{userId}/status` | Yes | `{ isActive }` | `ApiResponse<Void>` |
| GET | `/api/admin/analytics` | Yes | none | `ApiResponse<Map>` |
| GET | `/api/admin/pending-reviews` | Yes | none | `ApiResponse<String>` |

## Diseases

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| GET | `/api/diseases?keyword=&categoryId=&symptomIds=&page=&size=&sort=` | No | filters + pagination | `ApiResponse<PagedResponse<DiseaseSummaryDTO>>` |
| GET | `/api/diseases/search?keyword=&page=&size=&sort=` | No | keyword + pagination | `ApiResponse<PagedResponse<DiseaseSummaryDTO>>` |
| POST | `/api/diseases/search?page=&size=&sort=` | No | `DiseaseSearchRequest` | `ApiResponse<PagedResponse<DiseaseSummaryDTO>>` |
| GET | `/api/diseases/{id}` | No | path id | `ApiResponse<DiseaseDTO>` |
| GET | `/api/diseases/slug/{slug}` | No | path slug | `ApiResponse<DiseaseDetailDTO>` |
| GET | `/api/diseases/{id}/current-version` | No | path id | `ApiResponse<DiseaseDetailDTO>` |
| POST | `/api/diseases` | Yes | `CreateDiseaseRequest` | `ApiResponse<DiseaseDTO>` |
| POST | `/api/diseases/draft` | Yes | `CreateDiseaseDraftRequest` | `ApiResponse<DiseaseDTO>` |
| PUT | `/api/diseases/{id}` | Yes | `UpdateDiseaseRequest` | `ApiResponse<DiseaseDTO>` |
| POST | `/api/diseases/{id}/clone-current-version` | Yes | none | `ApiResponse<DiseaseVersionDTO>` |
| DELETE | `/api/diseases/{id}` | Yes | none | `ApiResponse<Void>` |
| PATCH | `/api/diseases/{id}/restore` | Yes | none | `ApiResponse<Void>` |
| PATCH | `/api/diseases/{id}/category/{categoryId}` | Yes | path IDs | `ApiResponse<Void>` |
| DELETE | `/api/diseases/{id}/category` | Yes | path id | `ApiResponse<Void>` |

## Disease Versions

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| POST | `/api/versions/draft?diseaseId=` | Yes | `CreateDiseaseVersionRequest` | `ApiResponse<DiseaseVersionDTO>` |
| POST | `/api/versions/clone/{diseaseId}` | Yes | none | `ApiResponse<DiseaseVersionDTO>` |
| GET | `/api/versions/{versionId}` | No | path versionId | `ApiResponse<DiseaseVersionDTO>` |
| GET | `/api/versions/disease/{diseaseId}` | No | path diseaseId | `ApiResponse<List<DiseaseVersionDTO>>` |
| GET | `/api/versions/disease/{diseaseId}/current` | No | path diseaseId | `ApiResponse<DiseaseVersionDTO>` |
| GET | `/api/versions/disease/{diseaseId}/latest-draft` | Yes | path diseaseId | `ApiResponse<DiseaseVersionDTO>` |
| GET | `/api/versions/pending-review?page=&size=&sort=` | Yes | pagination | `ApiResponse<PagedResponse<DiseaseVersionDTO>>` |
| PUT | `/api/versions/{versionId}` | Yes | `UpdateDiseaseVersionRequest` | `ApiResponse<DiseaseVersionDTO>` |
| POST | `/api/versions/{versionId}/submit` | Yes | none | `ApiResponse<DiseaseVersionDTO>` |
| POST | `/api/versions/{versionId}/approve` | Yes | `ModerationRequest` | `ApiResponse<DiseaseVersionDTO>` |
| POST | `/api/versions/{versionId}/reject` | Yes | `ModerationRequest` | `ApiResponse<DiseaseVersionDTO>` |
| POST | `/api/versions/{versionId}/archive` | Yes | none | `ApiResponse<DiseaseVersionDTO>` |
| DELETE | `/api/versions/{versionId}` | Yes | none | `ApiResponse<Void>` |
| PATCH | `/api/versions/{versionId}/restore` | Yes | none | `ApiResponse<Void>` |

## Disease Sections

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| POST | `/api/sections?versionId=` | Yes | `CreateDiseaseSectionRequest` | `ApiResponse<DiseaseSectionDTO>` |
| POST | `/api/sections/batch?versionId=` | Yes | `CreateDiseaseSectionRequest[]` | `ApiResponse<List<DiseaseSectionDTO>>` |
| GET | `/api/sections/{sectionId}` | No | path sectionId | `ApiResponse<DiseaseSectionDTO>` |
| GET | `/api/sections/version/{versionId}` | No | path versionId | `ApiResponse<List<DiseaseSectionDTO>>` |
| GET | `/api/sections/version/{versionId}/type/{sectionType}` | No | path params | `ApiResponse<DiseaseSectionDTO>` |
| PUT | `/api/sections/{sectionId}` | Yes | `UpdateDiseaseSectionRequest` | `ApiResponse<DiseaseSectionDTO>` |
| POST | `/api/sections/version/{versionId}/reorder` | Yes | `SectionOrderRequest[]` | `ApiResponse<Void>` |
| DELETE | `/api/sections/{sectionId}` | Yes | none | `ApiResponse<Void>` |
| DELETE | `/api/sections/version/{versionId}` | Yes | none | `ApiResponse<Void>` |
| POST | `/api/sections/version/{versionId}/validate` | Yes | none | `ApiResponse<Void>` |
| GET | `/api/sections/{sectionId}/markdown` | No | path sectionId | `ApiResponse<String>` |
| GET | `/api/sections/types` | No | none | `ApiResponse<List<SectionTypeDTO>>` |
| GET | `/api/sections/templates` | No | none | `ApiResponse<List<SectionTemplateDTO>>` |

## Categories, Symptoms, Cases, AI

| Method | Endpoint | Auth | Request | Response |
|---|---|---:|---|---|
| GET | `/api/categories` | No | none | `ApiResponse<List<CategoryDTO>>` |
| POST | `/api/categories` | Yes | `CreateCategoryRequest` | `ApiResponse<CategoryDTO>` |
| PUT | `/api/categories/{id}` | Yes | `UpdateCategoryRequest` | `ApiResponse<CategoryDTO>` |
| DELETE | `/api/categories/{id}` | Yes | none | `ApiResponse<Void>` |
| GET | `/api/symptoms` | No | none | `ApiResponse<List<SymptomDTO>>` |
| GET | `/api/symptoms/search?keyword=` | No | keyword (`q` also accepted) | `ApiResponse<List<SymptomDTO>>` |
| POST | `/api/symptoms` | Yes | `CreateSymptomRequest` | `ApiResponse<SymptomDTO>` |
| GET | `/api/cases?page=&size=&sort=` | No | pagination | `ApiResponse<PagedResponse<CaseStudySummaryDTO>>` |
| POST | `/api/cases/{id}/diagnose` | No | `DiagnoseRequest` | `ApiResponse<DiagnoseResponse>` |
| POST | `/api/symptom-checker/check` | No | `{ symptomIds, limit }` | `ApiResponse<List<SymptomMatchResult>>` |
| POST | `/api/symptom-checker/analyze` | No | `{ symptomIds }` | `ApiResponse<List<DiseaseMatchResultDTO>>` |
| GET | `/api/notifications/stream` | Yes | SSE | `text/event-stream` |
| GET | `/api/ai/stream` | Yes | SSE | `text/event-stream` |
