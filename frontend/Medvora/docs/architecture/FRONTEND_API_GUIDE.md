# MedLearn Frontend API Guide

This guide is for AI coding agents building the frontend. It explains exactly how to consume the backend.

---

## Base URL

```
VITE_API_URL=http://localhost:6060/api
```

Frontend must use environment variable `VITE_API_URL`. Never hardcode `localhost:6060` in components.

---

## Authentication

### JWT Bearer Token

All protected endpoints require an `Authorization: Bearer <accessToken>` header.

### Token Flow

```
POST /api/auth/login  →  { accessToken, refreshToken }
     ↓
Store accessToken + refreshToken (localStorage)
     ↓
Include Authorization header on every authenticated request
     ↓
On 401 → POST /api/auth/refresh  →  new tokens
     ↓
On refresh failure → clear tokens, redirect to /login
```

### ⚠️ CRITICAL RULE: Never call `/api/auth/me` without a token

```
App starts
     ↓
Check if accessToken exists in localStorage
     ↓
YES → GET /api/auth/me  →  load user profile
NO  → do NOT call /auth/me. Stay anonymous.
```

Anonymous users must never hit `/api/auth/me` — it will return 401.

### Logout

```
POST /api/auth/logout  (body optional, token from header)
     ↓
Clear tokens from localStorage
     ↓
Redirect to /login
```

---

## Public APIs (No JWT Required)

| Method | Endpoint | Purpose | Response DTO |
|--------|----------|---------|-------------|
| GET | /api/diseases | Paginated disease list (approved only) | `Page<DiseaseSummaryDTO>` |
| GET | /api/diseases/{id} | Disease by ID | `DiseaseDTO` |
| GET | /api/diseases/slug/{slug} | Disease detail with sections | `DiseaseDetailDTO` |
| GET | /api/diseases/search?q= | Search approved diseases | `Page<DiseaseSummaryDTO>` |
| POST | /api/diseases/search | Advanced disease search | `Page<DiseaseSummaryDTO>` |
| GET | /api/categories | All categories | `List<CategoryDTO>` |
| GET | /api/symptoms | All symptoms | `List<SymptomDTO>` |
| GET | /api/symptoms/search?q= | Search symptoms | `List<SymptomDTO>` |
| GET | /api/cases | Paginated approved case studies | `Page<CaseStudySummaryDTO>` |
| GET | /api/cases/{id} | Case study detail | `CaseStudyDetailDTO` |
| POST | /api/cases/{id}/diagnose | Submit diagnosis answer | `DiagnoseResponse` |
| POST | /api/symptom-checker/check | Legacy symptom check | `List<SymptomMatchResult>` |
| POST | /api/symptom-checker/analyze | Deterministic symptom analysis | `List<DiseaseMatchResultDTO>` |
| GET | /api/versions/{versionId} | Version by ID | `DiseaseVersionDTO` |
| GET | /api/versions/disease/{diseaseId} | All versions for a disease | `List<DiseaseVersionDTO>` |
| GET | /api/versions/disease/{diseaseId}/current | Current approved version | `DiseaseVersionDTO` |
| GET | /api/sections/{sectionId} | Section by ID | `DiseaseSectionDTO` |
| GET | /api/sections/version/{versionId} | Sections for a version | `List<DiseaseSectionDTO>` |

All auth endpoints (register, login, verify, refresh, forgot-password, reset-password, resend-verification) are also public.

---

## Protected APIs (JWT Required)

All create/update/delete/moderation/admin endpoints require authentication and appropriate permissions.

| Method | Endpoint | Permission | Purpose |
|--------|----------|------------|---------|
| POST | /api/diseases | DISEASE_WRITE | Create disease |
| PUT | /api/diseases/{id} | DISEASE_WRITE | Update disease metadata |
| DELETE | /api/diseases/{id} | DISEASE_DELETE | Soft-delete disease |
| PATCH | /api/diseases/{id}/restore | DISEASE_RESTORE | Restore disease |
| PATCH | /api/diseases/{id}/category/{catId} | DISEASE_MANAGE | Assign category |
| DELETE | /api/diseases/{id}/category | DISEASE_MANAGE | Remove category |
| POST | /api/diseases/draft | DISEASE_WRITE | Create draft |
| POST | /api/diseases/{id}/clone-current-version | VERSION_WRITE | Clone approved version |
| POST | /api/categories | DISEASE_WRITE | Create category |
| PUT | /api/categories/{id} | DISEASE_MANAGE | Update category |
| DELETE | /api/categories/{id} | DISEASE_MANAGE | Delete category |
| POST | /api/symptoms | DISEASE_WRITE | Create symptom |
| PUT | /api/symptoms/{id} | DISEASE_WRITE | Update symptom |
| DELETE | /api/symptoms/{id} | DISEASE_DELETE | Delete symptom |
| POST | /api/cases | DISEASE_WRITE | Create case study |
| GET | /api/auth/me | — | Current user profile |
| POST | /api/auth/logout | — | Logout |
| PUT | /api/users/me | — | Update own profile |
| POST | /api/versions/draft | VERSION_WRITE | Create draft version |
| POST | /api/versions/clone/{id} | VERSION_WRITE | Clone approved version |
| PUT | /api/versions/{id} | VERSION_WRITE | Update draft version |
| POST | /api/versions/{id}/submit | VERSION_WRITE | Submit for review |
| POST | /api/versions/{id}/approve | VERSION_REVIEW | Approve version |
| POST | /api/versions/{id}/reject | VERSION_REVIEW | Reject version |
| POST | /api/versions/{id}/archive | VERSION_WRITE | Archive version |
| DELETE | /api/versions/{id} | VERSION_DELETE | Delete version |
| POST | /api/sections | VERSION_WRITE | Create section |
| PUT | /api/sections/{id} | VERSION_WRITE | Update section |
| DELETE | /api/sections/{id} | VERSION_WRITE | Delete section |
| GET | /api/admin/users | USER_VIEW_ALL | List users |
| GET | /api/admin/users/{id} | USER_VIEW_ALL | Get user |
| PATCH | /api/admin/users/{id}/role | ROLE_ASSIGN | Assign role |
| PATCH | /api/admin/users/{id}/status | USER_MANAGE | Activate/deactivate |
| GET | /api/admin/analytics | USER_VIEW_ALL | Analytics |
| GET | /api/versions/pending-review | VERSION_REVIEW | Pending reviews |
| GET | /api/versions/disease/{id}/latest-draft | VERSION_READ | Latest draft |

---

## Response Format

Every response uses the standard wrapper:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-07-02T12:00:00"
}
```

- Always check `success` before reading `data`.
- Display `message` to the user when useful.

Error response:

```json
{
  "success": false,
  "message": "Disease not found",
  "data": null,
  "timestamp": "..."
}
```

---

## Pagination

Backend uses **Spring Data Pageable**. Query format:

```
?page=0&size=20&sort=name,asc
```

- `page` is 0‑based (page 0 = first page)
- `size` defaults to 20
- `sort` = field, direction (optional)

Response includes `Page` metadata:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20,
    "sort": { "sorted": true, "unsorted": false, "empty": false }
  }
}
```

Never invent custom pagination. Use these params directly.

---

## Error Handling

| Status | Meaning | Frontend Action |
|--------|---------|----------------|
| 401 | Token missing or expired | Try refresh. If refresh fails → logout. |
| 403 | Authenticated but no permission | Show "You do not have permission." |
| 404 | Resource not found | Show not-found state. |
| 409 | Validation conflict (duplicate) | Show backend error message. |
| 422 | Invalid input | Show validation errors. |
| 500 | Server error | Show generic error. |

---

## Route Mapping

```
Home
  └─ GET /api/diseases?page=0&size=20

Disease Detail
  └─ GET /api/diseases/slug/{slug}

Search
  └─ GET /api/diseases/search?q={keyword}

Categories
  └─ GET /api/categories

Symptoms
  └─ GET /api/symptoms

Symptom Checker
  └─ POST /api/symptom-checker/analyze
       Body: { "symptomIds": [1, 2, 3] }

Case Studies
  └─ GET /api/cases?page=0&size=10

Case Study Detail
  └─ GET /api/cases/{id}

Submit Diagnosis
  └─ POST /api/cases/{id}/diagnose
       Body: { "diagnosis": "..." }

Profile (logged in only)
  └─ GET /api/auth/me
```

---

## Integration Rules

1. All public GET endpoints require **no JWT**.
2. All POST/PUT/PATCH/DELETE endpoints require **authentication** unless explicitly documented as public.
3. The backend is the **single source of truth**. Never invent endpoints, DTOs, or enums.
4. Use the centralized API client (`src/api/client.ts`). Never call `fetch()` directly in components.
5. Refresh tokens in the interceptor, not in individual components.
6. DTOs consumed by frontend must mirror backend responses. Never create diverging frontend-only types.
7. When backend DTO changes, update frontend types immediately.
