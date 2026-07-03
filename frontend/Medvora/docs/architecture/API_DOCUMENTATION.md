# MedLearn API Documentation

Base URL: `http://localhost:6060/api`

Response envelope:
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-07-02T12:00:00"
}
```

Pagination uses Spring Data `Pageable` — query params: `page` (0-based), `size`, `sort`.

---

## Authentication

### POST /api/auth/register

Register a new user.

**Authentication:** Public

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePass123!",
  "fullName": "John Doe"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Registration successful! Please check your email to verify your account.",
  "data": null
}
```

**Errors:** `409` email/username exists

---

### POST /api/auth/login

Authenticate and receive JWT tokens.

**Authentication:** Public

**Request:**
```json
{
  "usernameOrEmail": "john@example.com",
  "password": "securePass123!"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": { "id": 1, "username": "johndoe", "email": "john@example.com", "fullName": "John Doe", "roles": ["USER"], "isVerified": true, "status": "ACTIVE" },
    "accessToken": "eyJhbGci...",
    "refreshToken": "refresh_abc...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

**Errors:** `401` invalid credentials, `403` email not verified, `429` rate-limited

---

### GET /api/auth/verify-email/{token}

Verify email via path token (frontend-compatible path).

**Authentication:** Public

**Response (200):**
```json
{ "success": true, "message": "Email verified successfully! You can now login.", "data": null }
```

---

### GET /api/auth/verify?token={token}

Verify email via query param (legacy).

**Authentication:** Public

**Response:** Same as above.

---

### POST /api/auth/resend-verification

Resend verification email.

**Authentication:** Public

**Request:**
```json
{ "email": "john@example.com" }
```

**Response (200):**
```json
{ "success": true, "message": "If that email is registered and unverified, a new verification link has been sent.", "data": null }
```

---

### POST /api/auth/forgot-password

Request password reset email.

**Authentication:** Public

**Request:**
```json
{ "email": "john@example.com" }
```

**Response (200):** Always returns success (prevents email enumeration).

---

### POST /api/auth/reset-password

Reset password with token from email.

**Authentication:** Public

**Request:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "newSecurePass456!"
}
```

**Response (200):**
```json
{ "success": true, "message": "Password has been reset successfully. Please log in with your new password.", "data": null }
```

**Errors:** `400` invalid/expired token

---

### POST /api/auth/refresh

Refresh access token using refresh token.

**Authentication:** Public

**Request:**
```json
{ "refreshToken": "refresh_abc..." }
```

**Response (200):** Same structure as login.

**Errors:** `400` token reused (revokes all sessions), expired, invalid

---

### GET /api/auth/me

Get current authenticated user.

**Authentication:** Required

**Response (200):**
```json
{
  "success": true,
  "message": "Get profile success",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "roles": ["USER"],
    "isVerified": true,
    "status": "ACTIVE"
  }
}
```

---

### POST /api/auth/logout

Logout and revoke tokens.

**Authentication:** Required

**Request (optional):**
```json
{ "refreshToken": "refresh_abc..." }
```

**Response (200):**
```json
{ "success": true, "message": "Logged out successfully", "data": null }
```

---

## User Profile

### GET /api/users/me

Get current user profile (same as `/api/auth/me`).

**Authentication:** Required

**Response:** Same structure as `/api/auth/me`.

---

### PUT /api/users/me

Update profile.

**Authentication:** Required

**Permission:** None (own profile)

**Request:**
```json
{
  "username": "newusername",
  "email": "newemail@example.com",
  "fullName": "John Updated",
  "avatarUrl": "https://example.com/avatar.jpg",
  "phoneNumber": "+1234567890"
}
```

**Response (200):**
```json
{ "success": true, "message": "Profile updated", "data": { "id": 1, ... } }
```

**Errors:** `409` email/username conflict

---

## Diseases

### GET /api/diseases

Paginated list of approved diseases.

**Authentication:** Public

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| page | int | Page index (0-based, default 0) |
| size | int | Page size (default 20) |
| keyword | string | Search by disease name |
| categoryId | long | Filter by category |
| symptomIds | long[] | Filter by associated symptoms |

**Response (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "name": "Diabetes Mellitus", "slug": "diabetes-mellitus", "updatedAt": "..." }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

---

### GET /api/diseases/{id}

Get disease by ID.

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Diabetes Mellitus",
    "slug": "diabetes-mellitus",
    "categoryId": 2,
    "categoryName": "Endocrine",
    "currentVersionId": 5,
    "status": "APPROVED",
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

---

### GET /api/diseases/slug/{slug}

Get disease detail by slug (SEO-friendly).

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": {
    "disease": { "id": 1, "name": "Diabetes Mellitus", ... },
    "currentVersion": { "id": 5, "versionNumber": 2, "status": "APPROVED", ... },
    "sections": [
      { "id": 10, "sectionTypeName": "Definition", "title": "What is Diabetes?", "content": "## Definition\n\nDiabetes is...", "orderIndex": 1 }
    ]
  }
}
```

---

### GET /api/diseases/{id}/current-version

Get disease with current version and sections.

**Authentication:** Public

**Response:** Same as slug detail.

---

### GET /api/diseases/search?q={keyword}

Search approved diseases by keyword.

**Authentication:** Public

**Response:** Paginated list of `DiseaseSummaryDTO`.

---

### POST /api/diseases/search

Search diseases with advanced filters.

**Authentication:** Public

**Request:**
```json
{
  "keyword": "diabetes",
  "categoryId": 2,
  "symptomIds": [1, 3, 5]
}
```

**Response:** Paginated list of `DiseaseSummaryDTO`.

---

### POST /api/diseases

Create a new disease.

**Authentication:** Required

**Permission:** DISEASE_WRITE

**Request:**
```json
{
  "name": "Hypertension",
  "slug": "hypertension",
  "categoryId": 1
}
```

**Response (200):**
```json
{ "success": true, "message": "Disease created", "data": { "id": 2, "name": "Hypertension", ... } }
```

---

### POST /api/diseases/draft

Create disease as draft.

**Authentication:** Required

**Permission:** DISEASE_WRITE

**Request:** Same as create.

**Response:** Same as create.

---

### PUT /api/diseases/{id}

Update disease metadata.

**Authentication:** Required

**Permission:** DISEASE_WRITE (or DISEASE_MANAGE to bypass ownership)

**Request:**
```json
{ "name": "Updated Name", "slug": "updated-name", "categoryId": 2 }
```

**Response (200):**
```json
{ "success": true, "message": "Disease updated", "data": { "id": 1, ... } }
```

---

### DELETE /api/diseases/{id}

Soft-delete a disease.

**Authentication:** Required

**Permission:** DISEASE_DELETE (or DISEASE_MANAGE to bypass ownership)

**Response (200):**
```json
{ "success": true, "message": "Disease soft deleted", "data": null }
```

---

### PATCH /api/diseases/{id}/restore

Restore soft-deleted disease.

**Authentication:** Required

**Permission:** DISEASE_RESTORE

---

### PATCH /api/diseases/{id}/category/{categoryId}

Assign category to disease.

**Authentication:** Required

**Permission:** DISEASE_MANAGE

---

### DELETE /api/diseases/{id}/category

Remove category from disease.

**Authentication:** Required

**Permission:** DISEASE_MANAGE

---

### POST /api/diseases/{id}/clone-current-version

Clone approved version into new draft.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

## Categories

### GET /api/categories

List all categories.

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Cardiology", "slug": "cardiology", "description": "Heart diseases", "createdAt": "..." }
  ]
}
```

---

### GET /api/categories/{id}

Get category by ID.

**Authentication:** Public

---

### GET /api/categories/slug/{slug}

Get category by slug.

**Authentication:** Public

---

### POST /api/categories

Create category.

**Authentication:** Required

**Permission:** DISEASE_WRITE

**Request:**
```json
{ "name": "Neurology", "description": "Nervous system disorders" }
```

---

### PUT /api/categories/{id}

Update category.

**Authentication:** Required

**Permission:** DISEASE_MANAGE

---

### DELETE /api/categories/{id}

Delete category.

**Authentication:** Required

**Permission:** DISEASE_MANAGE

---

## Symptoms

### GET /api/symptoms

List all symptoms.

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Fever", "slug": "fever", "description": "Elevated body temperature", "createdAt": "...", "updatedAt": "..." }
  ]
}
```

---

### GET /api/symptoms/search?q={query}

Search symptoms by name/description.

**Authentication:** Public

---

### POST /api/symptoms

Create symptom.

**Authentication:** Required

**Permission:** DISEASE_WRITE

**Request:**
```json
{ "name": "Cough", "description": "Persistent cough" }
```

---

### PUT /api/symptoms/{id}

Update symptom.

**Authentication:** Required

**Permission:** DISEASE_WRITE

---

### DELETE /api/symptoms/{id}

Delete symptom.

**Authentication:** Required

**Permission:** DISEASE_DELETE

---

## Symptom Checker

### POST /api/symptom-checker/check

Check symptoms against diseases (legacy V0).

**Authentication:** Public

**Request body:** `[1, 3, 5]` (array of symptom IDs)

**Query Parameters:** `limit` (default 10)

**Response (200):**
```json
{
  "success": true,
  "message": "Symptom check completed",
  "data": [
    {
      "diseaseId": 1,
      "diseaseName": "Common Cold",
      "diseaseSlug": "common-cold",
      "matchCount": 3,
      "matchScore": 0.85,
      "matchedSymptoms": ["Fever", "Cough", "Runny Nose"],
      "shortDescription": "Common cold is..."
    }
  ]
}
```

---

### POST /api/symptom-checker/analyze

Deterministic symptom analysis (V1).

**Authentication:** Public

**Request:**
```json
{ "symptomIds": [1, 3, 5] }
```

**Response (200):**
```json
{
  "success": true,
  "message": "Symptom analysis completed",
  "data": [
    {
      "diseaseId": 1,
      "diseaseName": "Common Cold",
      "matchScore": 0.75,
      "matchedSymptoms": [{ "id": 1, "name": "Fever" }, { "id": 3, "name": "Cough" }],
      "missingSymptoms": [{ "id": 7, "name": "Sneezing" }],
      "explanation": "3 of 4 symptoms match (weighted score: 75%)"
    }
  ]
}
```

---

## Case Studies

### GET /api/cases

Paginated list of approved case studies.

**Authentication:** Public

**Query Parameters:** page, size, sort

**Response (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Chest Pain Assessment",
        "slug": "chest-pain-assessment",
        "description": "A 55-year-old male with acute chest pain",
        "difficulty": "MEDIUM",
        "isFeatured": true,
        "viewCount": 120,
        "createdAt": "..."
      }
    ],
    "totalElements": 30,
    "totalPages": 2
  }
}
```

---

### GET /api/cases/{id}

Get case study detail by ID.

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Chest Pain Assessment",
    "slug": "chest-pain-assessment",
    "description": "...",
    "difficulty": "MEDIUM",
    "diagnosis": "Acute Myocardial Infarction",
    "learningNotes": "Key learning points...",
    "patientAge": 55,
    "patientGender": "Male",
    "chiefComplaint": "Chest pain radiating to left arm",
    "caseQuestion": "What is the most likely diagnosis?",
    "explanation": "The patient presents with...",
    "viewCount": 120,
    "isFeatured": true,
    "createdBy": 1,
    "createdAt": "...",
    "updatedAt": "...",
    "symptomIds": [1, 3, 5]
  }
}
```

---

### GET /api/cases/slug/{slug}

Get case study by slug.

**Authentication:** Public

---

### POST /api/cases

Create case study.

**Authentication:** Required

**Permission:** DISEASE_WRITE

**Request:**
```json
{
  "title": "Chest Pain Assessment",
  "description": "A 55-year-old male with acute chest pain",
  "difficulty": "MEDIUM",
  "diagnosis": "Acute Myocardial Infarction",
  "learningNotes": "...",
  "patientAge": 55,
  "patientGender": "Male",
  "chiefComplaint": "Chest pain radiating to left arm",
  "caseQuestion": "What is the most likely diagnosis?",
  "explanation": "...",
  "symptomIds": [1, 3, 5]
}
```

---

### POST /api/cases/{id}/diagnose

Submit diagnosis answer for a case study.

**Authentication:** Public

**Request:**
```json
{ "diagnosis": "Acute Myocardial Infarction" }
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "correct": true,
    "feedback": "Correct diagnosis!"
  }
}
```

---

## Disease Versions

### POST /api/versions/draft?diseaseId={id}

Create draft version for a disease.

**Authentication:** Required

**Permission:** VERSION_WRITE

**Request (optional):**
```json
{ "note": "First draft" }
```

---

### POST /api/versions/clone/{diseaseId}

Clone approved version into new draft.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

### GET /api/versions/{versionId}

Get version by ID.

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "diseaseId": 1,
    "versionNumber": 2,
    "status": "DRAFT",
    "moderationNote": null,
    "createdById": 1,
    "reviewedById": null,
    "reviewedAt": null,
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

---

### GET /api/versions/disease/{diseaseId}

List all versions for a disease.

**Authentication:** Public

---

### GET /api/versions/disease/{diseaseId}/current

Get current approved version.

**Authentication:** Public

---

### GET /api/versions/disease/{diseaseId}/latest-draft

Get latest draft version.

**Authentication:** Required

**Permission:** VERSION_READ

---

### GET /api/versions/pending-review

List versions pending moderation review.

**Authentication:** Required

**Permission:** VERSION_REVIEW

---

### PUT /api/versions/{versionId}

Update draft version metadata.

**Authentication:** Required

**Permission:** VERSION_WRITE (or SECTION_EDIT_ANY to bypass ownership)

---

### POST /api/versions/{versionId}/submit

Submit draft for review.

**Authentication:** Required

**Permission:** VERSION_WRITE

**Errors:** `400` if required sections missing, duplicate submit, not in DRAFT status

---

### POST /api/versions/{versionId}/approve

Approve a pending version.

**Authentication:** Required

**Permission:** VERSION_REVIEW

**Request:**
```json
{ "note": "Approved after review" }
```

**Note:** Reviewer cannot approve own submission.

---

### POST /api/versions/{versionId}/reject

Reject a pending version.

**Authentication:** Required

**Permission:** VERSION_REVIEW

**Request:**
```json
{ "note": "Definition section needs more detail" }
```

---

### POST /api/versions/{versionId}/archive

Archive a version.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

### DELETE /api/versions/{versionId}

Soft-delete a version.

**Authentication:** Required

**Permission:** VERSION_DELETE

**Note:** Cannot delete APPROVED versions.

---

### PATCH /api/versions/{versionId}/restore

Restore soft-deleted version.

**Authentication:** Required

**Permission:** VERSION_RESTORE

---

## Disease Sections

### GET /api/sections/{sectionId}

Get section by ID.

**Authentication:** Public

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "diseaseVersionId": 5,
    "sectionTypeId": 1,
    "sectionTypeName": "Definition",
    "title": "What is Diabetes?",
    "content": "## Definition\n\nDiabetes is a chronic condition...",
    "orderIndex": 1
  }
}
```

---

### GET /api/sections/version/{versionId}

Get all sections for a version.

**Authentication:** Public

---

### GET /api/sections/version/{versionId}/type/{sectionType}

Get section by type name/key for a version.

**Authentication:** Public

---

### GET /api/sections/{sectionId}/markdown

Render section markdown content.

**Authentication:** Public

---

### GET /api/sections/types

List all available section types.

**Authentication:** Public

---

### GET /api/sections/templates

Get default section templates.

**Authentication:** Public

---

### POST /api/sections?versionId={id}

Create a section in a version.

**Authentication:** Required

**Permission:** VERSION_WRITE

**Request:**
```json
{
  "sectionTypeId": 1,
  "title": "Definition",
  "content": "## Definition\n\nContent...",
  "orderIndex": 1
}
```

---

### POST /api/sections/batch?versionId={id}

Create multiple sections at once.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

### PUT /api/sections/{sectionId}

Update section content.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

### POST /api/sections/version/{versionId}/reorder

Reorder sections in a version.

**Authentication:** Required

**Permission:** VERSION_WRITE

**Request:**
```json
[
  { "sectionId": 10, "orderIndex": 2 },
  { "sectionId": 11, "orderIndex": 1 }
]
```

---

### DELETE /api/sections/{sectionId}

Delete a section.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

### DELETE /api/sections/version/{versionId}

Soft-delete all sections for a version.

**Authentication:** Required

**Permission:** VERSION_DELETE

---

### POST /api/sections/version/{versionId}/validate

Validate all required sections exist for a version.

**Authentication:** Required

**Permission:** VERSION_WRITE

---

## Notifications

### GET /api/notifications/stream

SSE stream for real-time notifications.

**Authentication:** Public (controlled by auth within stream)

**Response:** Server-Sent Events stream.

---

## AI Stream

### GET /api/ai/stream

SSE stream for AI-generated content.

**Authentication:** Public (controlled by auth within stream)

**Response:** Server-Sent Events stream.

---

## Admin

### GET /api/admin/users

List all users (paginated).

**Authentication:** Required

**Permission:** USER_VIEW_ALL

---

### GET /api/admin/users/{id}

Get user by ID.

**Authentication:** Required

**Permission:** USER_VIEW_ALL

---

### PATCH /api/admin/users/{userId}/role

Assign a role to user.

**Authentication:** Required

**Permission:** ROLE_ASSIGN

**Request:**
```json
{ "roleName": "REVIEWER" }
```

---

### PATCH /api/admin/users/{userId}/status

Activate or deactivate user.

**Authentication:** Required

**Permission:** USER_MANAGE

**Request:**
```json
{ "isActive": true }
```

---

### GET /api/admin/analytics

System analytics dashboard data.

**Authentication:** Required

**Permission:** USER_VIEW_ALL

---

### GET /api/admin/pending-reviews

Alias — see `GET /api/versions/pending-review`.

**Authentication:** Required

**Permission:** VERSION_REVIEW

---

# Frontend Endpoint Mapping

| Frontend Feature | Backend Endpoint | Status |
|-----------------|------------------|--------|
| Disease List | GET /api/diseases | ✅ Added |
| Disease Detail (by ID) | GET /api/diseases/{id} | ✅ Existing |
| Disease Detail (by slug) | GET /api/diseases/slug/{slug} | ✅ Existing |
| Disease Search | GET /api/diseases/search?q= | ✅ Added |
| Disease Search (POST) | POST /api/diseases/search | ✅ Existing |
| Create Disease | POST /api/diseases | ✅ Existing (protected) |
| Update Disease | PUT /api/diseases/{id} | ✅ Existing (protected) |
| Delete Disease | DELETE /api/diseases/{id} | ✅ Existing (protected) |
| Categories List | GET /api/categories | ✅ Added |
| Create Category | POST /api/categories | ✅ Added (protected) |
| Symptoms List | GET /api/symptoms | ✅ Added |
| Symptoms Search | GET /api/symptoms/search?q= | ✅ Added |
| Create Symptom | POST /api/symptoms | ✅ Added (protected) |
| Update Symptom | PUT /api/symptoms/{id} | ✅ Added (protected) |
| Delete Symptom | DELETE /api/symptoms/{id} | ✅ Added (protected) |
| Symptom Checker | POST /api/symptom-checker/analyze | ✅ Existing |
| Symptom Checker V0 | POST /api/symptom-checker/check | ✅ Existing |
| Case Studies List | GET /api/cases | ✅ Added |
| Case Study Detail | GET /api/cases/{id} | ✅ Added |
| Create Case Study | POST /api/cases | ✅ Added (protected) |
| Submit Diagnosis | POST /api/cases/{id}/diagnose | ✅ Added |
| Register | POST /api/auth/register | ✅ Existing |
| Login | POST /api/auth/login | ✅ Existing |
| Forgot Password | POST /api/auth/forgot-password | ✅ Existing |
| Logout | POST /api/auth/logout | ✅ Existing (body optional) |
| Verify Email | GET /api/auth/verify-email/{token} | ✅ Added |
| My Profile | GET /api/auth/me | ✅ Existing |
| Update Profile | PUT /api/users/me | ✅ Existing (protected) |
| Admin: Users | GET /api/admin/users | ✅ Consolidated to AdminController |
| Admin: Role | PATCH /api/admin/users/{userId}/role | ✅ Consolidated |
| Admin: Status | PATCH /api/admin/users/{userId}/status | ✅ Consolidated |
| Admin: Analytics | GET /api/admin/analytics | ✅ Added (stub) |
| Admin: Pending Reviews | GET /api/versions/pending-review | ✅ Existing |
| Notifications Stream | GET /api/notifications/stream | ✅ Existing |
| AI Stream | GET /api/ai/stream | ✅ Existing |
| Disease Versions | GET /api/versions/disease/{diseaseId} | ✅ Existing |

## Notes on intentionally unavailable endpoints

- `GET /api/diseases/approved` — removed (the list endpoint `/api/diseases` returns only approved).
- `GET /api/diseases/categories` — removed. Use `GET /api/categories` instead.
- `GET /api/diseases/category/{slugOrId}` — removed. Filter diseases by passing `categoryId` as query param to `GET /api/diseases`.
- `GET /api/admin/pending-reviews` — redirect alias to `/api/versions/pending-review`.
- `PATCH /api/admin/users/{userId}/role` replaces old `PATCH /api/admin/users/{id}/roles`.
- `DELETE /api/symptoms/{id}` uses `DISEASE_DELETE` permission.
- Case study create assigns `DRAFT` status by default.
- Analytics endpoint returns placeholder — extend when metrics are needed.

## Legacy endpoints preserved

- `GET /api/auth/verify?token=` — original query-param verify; `GET /api/auth/verify-email/{token}` added for path-style.

