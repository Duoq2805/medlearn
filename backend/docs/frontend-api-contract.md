

## Disease Version Endpoints

### POST /versions/draft

Create a draft version for a disease.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Query Parameters:**
- `diseaseId` (required) - ID of the disease to associate the version with

**Request Body:**
```json
{
  "note": "Initial draft for review"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Draft version created",
  "data": {
    "id": 21,
    "diseaseId": 10,
    "versionNumber": 2,
    "status": "DRAFT",
    "createdAt": "2026-06-21T10:30:45.123456",
    "updatedAt": "2026-06-21T10:30:45.123456"
  }
}
```

---

### POST /versions/clone/{diseaseId}

Clone the current approved version as a new draft.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Path Parameters:**
- `diseaseId` (required) - ID of the disease whose version to clone

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version cloned",
  "data": { /* DiseaseVersionDTO */ }
}
```

---

### GET /versions/{versionId}

Get a specific version by its ID.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Path Parameters:**
- `versionId` (required) - ID of the version to retrieve

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* DiseaseVersionDTO */ }
}
```

---

### GET /versions/disease/{diseaseId}

Get all versions for a specific disease.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Path Parameters:**
- `diseaseId` (required) - ID of the disease

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    { /* DiseaseVersionDTO */ },
    { /* DiseaseVersionDTO */ }
  ]
}
```

---

### GET /versions/disease/{diseaseId}/current

Get the current approved version for a disease.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Path Parameters:**
- `diseaseId` (required) - ID of the disease

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* DiseaseVersionDTO */ }
}
```

---

### GET /versions/disease/{diseaseId}/latest-draft

Get the latest draft version for a disease.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_READ |
| **HTTP Method** | GET |

**Path Parameters:**
- `diseaseId` (required) - ID of the disease

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* DiseaseVersionDTO */ }
}
```

---

### GET /versions/pending-review

Get all versions pending review.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_REVIEW |
| **HTTP Method** | GET |

**Query Parameters:**
- `page`, `size`, `sort` (standard pagination)

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* Page<DiseaseVersionDTO> */ }
}
```

---

### PUT /versions/{versionId}

Update a draft version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | PUT |

**Path Parameters:**
- `versionId` (required) - ID of the version to update

**Request Body:**
```json
{
  "moderationNote": "Added more details",
  "title": "Updated Title",
  "content": "Updated content",
  "orderIndex": 1
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Draft version updated",
  "data": { /* Updated DiseaseVersionDTO */ }
}
```

---

### POST /versions/{versionId}/submit

Submit a version for review.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Path Parameters:**
- `versionId` (required) - ID of the version to submit

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version submitted for review",
  "data": { /* Submitted DiseaseVersionDTO */ }
}
```

**Error Responses:**
- **409**: Version already pending review, missing required sections

---

### POST /versions/{versionId}/approve

Approve a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_REVIEW |
| **HTTP Method** | POST |

**Path Parameters:**
- `versionId` (required) - ID of the version to approve

**Request Body:**
```json
{
  "note": "Approved as is."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version approved",
  "data": { /* Approved DiseaseVersionDTO */ }
}
```

**Error Responses:**
- **409**: Invalid workflow transition (e.g., trying to approve an archived version)
- **403**: Not a reviewer, or approving own version

---

### POST /versions/{versionId}/reject

Reject a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_REVIEW |
| **HTTP Method** | POST |

**Path Parameters:**
- `versionId` (required) - ID of the version to reject

**Request Body:**
```json
{
  "note": "Needs more detail on causes."
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version rejected",
  "data": { /* Rejected DiseaseVersionDTO */ }
}
```

**Error Responses:**
- **409**: Invalid workflow transition (e.g., trying to reject an archived version)
- **403**: Not a reviewer

---

### POST /versions/{versionId}/archive

Archive a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Path Parameters:**
- `versionId` (required) - ID of the version to archive

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version archived",
  "data": { /* Archived DiseaseVersionDTO */ }
}
```

**Error Responses:**
- **409**: Invalid workflow transition (e.g., trying to archive a DRAFT version)
- **403**: Insufficient permissions

---

### DELETE /versions/{versionId}

Soft delete a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_DELETE |
| **HTTP Method** | DELETE |

**Path Parameters:**
- `versionId` (required) - ID of the version to delete

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version soft deleted",
  "data": null
}
```

---

### PATCH /versions/{versionId}/restore

Restore a soft-deleted version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_RESTORE |
| **HTTP Method** | PATCH |

**Path Parameters:**
- `versionId` (required) - ID of the version to restore

**Success Response (200):**
```json
{
  "success": true,
  "message": "Version restored",
  "data": null
}
```

---

## Disease Section Endpoints

### POST /sections

Create a new section for a disease version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Query Parameters:**
- `versionId` (required) - ID of the disease version

**Request Body:**
```json
{
  "sectionTypeId": 1,
  "title": "Definition",
  "content": "<p>A chronic condition...</p>",
  "orderIndex": 0
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Section created",
  "data": {
    "id": 1,
    "diseaseVersionId": 21,
    "sectionTypeId": 1,
    "sectionTypeName": "Definition",
    "title": "Definition",
    "content": "<p>A chronic condition...</p>",
    "orderIndex": 0
  }
}
```

---

### POST /sections/batch

Create multiple sections for a disease version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Query Parameters:**
- `versionId` (required) - ID of the disease version

**Request Body:**
```json
[
  { /* CreateDiseaseSectionRequest */ },
  { /* CreateDiseaseSectionRequest */ }
]
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Sections created",
  "data": [
    { /* DiseaseSectionDTO */ },
    { /* DiseaseSectionDTO */ }
  ]
}
```

---

### GET /sections/{sectionId}

Get a specific section by its ID.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Path Parameters:**
- `sectionId` (required) - Section ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* DiseaseSectionDTO */ }
}
```

---

### GET /sections/version/{versionId}

Get all sections for a disease version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Path Parameters:**
- `versionId` (required) - Version ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    { /* DiseaseSectionDTO */ },
    { /* DiseaseSectionDTO */ }
  ]
}
```

---

### GET /sections/version/{versionId}/type/{sectionType}

Get a specific section by type for a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Path Parameters:**
- `versionId` (required) - Version ID
- `sectionType` (required) - Name of the section type (e.g., "definition")

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* DiseaseSectionDTO */ }
}
```

---

### PUT /sections/{sectionId}

Update an existing section.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | PUT |

**Path Parameters:**
- `sectionId` (required) - Section ID

**Request Body:**
```json
{
  "sectionTypeId": 2,
  "title": "Updated Symptoms Title",
  "content": "<p>Updated symptoms...</p>",
  "orderIndex": 1
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Section updated",
  "data": { /* Updated DiseaseSectionDTO */ }
}
```

---

### POST /sections/version/{versionId}/reorder

Reorder sections for a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Path Parameters:**
- `versionId` (required) - Version ID

**Request Body:**
```json
[
  { "sectionId": 1, "orderIndex": 1 },
  { "sectionId": 2, "orderIndex": 0 }
]
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Sections reordered",
  "data": null
}
```

---

### DELETE /sections/{sectionId}

Soft delete a section.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | DELETE |

**Path Parameters:**
- `sectionId` (required) - Section ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Section deleted",
  "data": null
}
```

---

### DELETE /sections/version/{versionId}

Soft delete all sections for a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_DELETE |
| **HTTP Method** | DELETE |

**Path Parameters:**
- `versionId` (required) - Version ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Sections soft deleted",
  "data": null
}
```

---

### POST /sections/version/{versionId}/validate

Validate required sections for a version.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | VERSION_WRITE |
| **HTTP Method** | POST |

**Path Parameters:**
- `versionId` (required) - Version ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Sections validated",
  "data": null
}
```

**Error Responses:**
- **400**: Missing required sections (Definition, Symptoms, Treatment)

---

### GET /sections/types

Get list of available section types.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    { "id": 1, "name": "definition", "description": "Medical definition of the condition" },
    { "id": 2, "name": "symptoms", "description": "Common symptoms" }
  ]
}
```

---

### GET /sections/templates

Get list of default section templates.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    { "sectionType": "definition", "title": "Definition", "template": "<p>Definition...</p>" },
    { "sectionType": "symptoms", "title": "Symptoms", "template": "<p>Symptoms...</p>" }
  ]
}
```

---

## User Endpoints

### GET /api/users/me

Get current authenticated user profile.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Get profile success",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "roles": ["CONTRIBUTOR"],
    "isVerified": true,
    "status": "ACTIVE"
  }
}
```

---

### PUT /api/users/me

Update authenticated user profile.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | PUT |

**Request Body:**
```json
{
  "fullName": "Johnathan Doe",
  "avatarUrl": "http://example.com/avatar.jpg",
  "phoneNumber": "+1234567890"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Profile updated",
  "data": { /* Updated UserDTO */ }
}
```
