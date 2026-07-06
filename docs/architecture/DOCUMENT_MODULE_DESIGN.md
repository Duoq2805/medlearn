# Document + Draft Generation Module — Backend Architecture Design

> **Sprint 2** | Design-only | No implementation

---

## Table of Contents

1. [Problem & Scope](#1--problem--scope)
2. [Package Structure](#2--package-structure)
3. [Domain: Document](#3--domain-document)
4. [Domain: DiseaseDraft](#4--domain-diseasedraft)
5. [Database Schema (ERD)](#5--database-schema-erd)
6. [REST API](#6--rest-api)
7. [AI Integration Flow](#7--ai-integration-flow)
8. [Sequence Diagrams](#8--sequence-diagrams)
9. [Implementation Order](#9--implementation-order)
10. [New Permissions](#10--new-permissions)

---

## 1 — Problem & Scope

### What the frontend already ships (unwired)

| Component | Method | Expected Endpoint | Expected Response |
|-----------|--------|-------------------|-------------------|
| `UploadDocumentView` | Upload file | `POST /api/documents/upload` | `DocumentUploadResponse` |
| `UploadDocumentView` | Extract text | `POST /api/documents/{id}/extract` | `ExtractedTextResponse` |
| `ImportUrlView` | Import URL | `POST /api/documents/import-url` | `UrlImportResponse` |
| `AiGenerateView` | AI draft | `POST /api/ai/draft/generate` | `DraftGenerationResponse` |
| `DraftPreview` | Preview | (client-side only) | — |
| `SourceViewer` | Show sources | (client-side only) | — |
| `ProvenanceBadge` | Show meta | (client-side only) | — |

All six frontend components exist with stubs throwing `TODO: endpoint not implemented`.

### Two distinct domains

**Document domain** — file storage, text extraction, URL import. Plumbing. No business logic.

**DiseaseDraft domain** — structured content generated from source documents via AI. Business logic: accept/reject/lifecycle, versioning, provenance tracking.

### Design constraints

| Constraint | Source |
|------------|--------|
| Follow existing entity patterns (`@SQLRestriction`, `@Version`, `OffsetDateTime`) | `Disease.java`, `DiseaseVersion.java` |
| Follow existing DTO/Controller patterns (`ApiResponse<T>`, `@PreAuthorize`, `@RequiredArgsConstructor`) | `DiseaseController.java` |
| Reuse existing AI gateway, rate limiter, quota, usage logging | `AiGatewayRouter`, `AiQuotaService`, `AiUsageService` |
| Frontend types are frozen — backend must produce matching shapes | `diseaseDraft.ts` |
| Do NOT modify existing code or create migrations | Sprint 2 rule |
| Local file storage (not S3, no external dep) | Existing project pattern |

---

## 2 — Package Structure

### New packages (no existing files modified)

```
src/main/java/com/duoq/medlearn/
├── document/                          # NEW — Document domain
│   ├── controller/
│   │   └── DocumentController.java
│   ├── service/
│   │   ├── DocumentService.java            (interface)
│   │   └── impl/
│   │       └── DocumentServiceImpl.java
│   ├── dto/
│   │   ├── DocumentUploadResponse.java
│   │   ├── ExtractedTextResponse.java
│   │   └── UrlImportResponse.java
│   ├── entity/
│   │   ├── Document.java
│   │   └── DocumentChunk.java              (for RAG pipeline)
│   ├── repository/
│   │   ├── DocumentRepository.java
│   │   └── DocumentChunkRepository.java
│   ├── mapper/
│   │   └── DocumentMapper.java
│   ├── exception/
│   │   ├── DocumentNotFoundException.java
│   │   ├── DocumentStorageException.java
│   │   └── UnsupportedDocumentTypeException.java
│   └── storage/
│       ├── DocumentStorageService.java      (interface)
│       └── impl/
│           └── LocalFileStorageService.java
│
├── draft/                             # NEW — Draft domain
│   ├── controller/
│   │   ├── DraftController.java
│   │   └── AiDraftController.java
│   ├── service/
│   │   ├── DraftService.java               (interface)
│   │   ├── impl/
│   │   │   └── DraftServiceImpl.java
│   │   ├── DraftLifecycleService.java       (interface)
│   │   └── impl/
│   │       └── DraftLifecycleServiceImpl.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── GenerateDraftRequest.java
│   │   │   ├── AcceptDraftRequest.java
│   │   │   └── DraftSearchRequest.java
│   │   └── response/
│   │       ├── DraftResponse.java
│   │       ├── DraftDetailResponse.java
│   │       ├── DraftGenerationResponse.java
│   │       ├── DraftSectionResponse.java
│   │       └── DraftSourceResponse.java
│   ├── entity/
│   │   ├── DiseaseDraft.java
│   │   ├── DiseaseDraftSection.java
│   │   ├── DraftSource.java
│   │   └── DraftProvenance.java
│   ├── repository/
│   │   ├── DiseaseDraftRepository.java
│   │   ├── DiseaseDraftSectionRepository.java
│   │   ├── DraftSourceRepository.java
│   │   └── DraftProvenanceRepository.java
│   ├── mapper/
│   │   └── DraftMapper.java
│   ├── enums/
│   │   ├── DraftMethod.java
│   │   ├── DraftStatus.java
│   │   └── DraftSectionType.java
│   └── exception/
│       ├── DraftNotFoundException.java
│       ├── DraftNotEditableException.java
│       └── DraftLifecycleException.java
│
└── ai/draft/                          # NEW — AI generation adapter
    ├── AiDraftGenerator.java               (interface)
    └── impl/
        └── AiDraftGeneratorImpl.java
```

No existing files are touched. The AI draft generator lives under `ai/draft/` as an extension of the existing AI module — it reuses `AiGatewayRouter`, `PromptTemplateService`, `AiQuotaService`, `AiRateLimiter`, `AiOutputValidator`, `AiUsageService`.

---

## 3 — Domain: Document

### 3.1 Entities

#### `Document`

| Field | Type | Notes |
|-------|------|-------|
| id | Long (PK, auto) | |
| filename | String(255), NOT NULL | Original filename |
| storagePath | String(500), NOT NULL | Internal path in `uploads/` |
| contentType | String(100) | MIME type |
| size | Long | Bytes |
| pageCount | Integer | Nullable — extracted after processing |
| status | Enum: UPLOADED, PROCESSING, READY, FAILED | |
| errorMessage | TEXT | Nullable — failure reason |
| uploadedBy | @ManyToOne(User) | |
| createdAt | OffsetDateTime | |
| updatedAt | OffsetDateTime | |
| deletedAt | OffsetDateTime | Soft delete pattern |

Purpose: Tracks each uploaded file through its lifecycle. `LocalFileStorageService` writes files to `./uploads/{yyyy/MM}/` with UUID-based names to avoid collisions.

#### `DocumentChunk`

| Field | Type | Notes |
|-------|------|-------|
| id | Long (PK, auto) | |
| document | @ManyToOne(Document) | |
| chunkIndex | Integer | Order within document |
| content | TEXT, NOT NULL | Extracted text segment |
| pageNumber | Integer | Nullable |
| charStart | Integer | Offset in original text |
| charEnd | Integer | Offset in original text |
| tokenCount | Integer | Estimated for prompt budget |
| createdAt | OffsetDateTime | |

Purpose: Pre-chunked text segments for the RAG pipeline. Generated by `DocumentServiceImpl.extractText()` after upload. Each chunk is ~1000 tokens with 200-token overlap. Feeds into AI draft generation as retrievable context.

### 3.2 Storage Strategy

`LocalFileStorageService` (interface) → `LocalFileStorageServiceImpl`:

- Store files at `./uploads/{yyyy/MM}/{uuid}.{ext}`
- Validate MIME type against allowlist: `application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `text/plain`, `text/markdown`
- 50MB size cap (enforced in controller + service)
- On delete, `DocumentServiceImpl.softDelete()` marks DB row; storage cleanup runs as a scheduled job (out of scope for this sprint — files remain on disk)

### 3.3 Text Extraction Pipeline

```
Upload → validate → store → Document(status=UPLOADED)
                              ↓
                    DocumentServiceImpl.extractText(id)
                              ↓
                    Detect type by contentType
                    ┌──────┬──────┬──────┬──────┐
                    │ PDF  │ DOCX │ TXT  │  MD  │
                    └──┬───┴──┬───┴──┬───┴──┬───┘
                       ↓      ↓      ↓      ↓
                    Apache Tika (single library handles all four)
                              ↓
                    Raw text + optional page numbers
                              ↓
                    Split into DocumentChunk (1000-token sliding window)
                              ↓
                    Document(status=READY)
```

**Dependency**: `org.apache.tika:tika-core:3.x` (already compatible with Spring Boot 3.2, no version conflict).

### 3.4 URL Import

```
POST /api/documents/import-url { url }
         ↓
Validate URL format + domain allowlist
(who.int, cdc.gov, ncbi.nlm.nih.gov, mayoclinic.org, medscape.com)
         ↓
Jsoup.connect(url).get() → extract article content
         ↓
Create Document(status=READY)  filename = sanitized title + ".md"
         ↓
Create single DocumentChunk with full content
         ↓
Return UrlImportResponse { title, content, source }
```

**Dependency**: `Jsoup` already exists in `pom.xml` (used in `DiseaseSectionServiceImpl.sanitizeHtmlContent`). No new dependency.

---

## 4 — Domain: DiseaseDraft

### 4.1 Entities

#### `DiseaseDraft`

| Field | Type | Notes |
|-------|------|-------|
| id | Long (PK, auto) | |
| diseaseName | String(255), NOT NULL | User-entered name during creation |
| status | Enum: DRAFT, ACCEPTED, REJECTED, ARCHIVED | |
| method | Enum: UPLOAD, IMPORT_URL, AI_GENERATE, MANUAL | |
| sourceLabel | String(255) | Display label from frontend metadata |
| sourceUrl | String(500) | Nullable — for import-url method |
| originalFilename | String(255) | Nullable — for upload method |
| version | Integer, @Builder.Default=1 | Incremented on each accept |
| acceptedDiseaseId | Long | Nullable — links to Disease after accept |
| acceptedVersionId | Long | Nullable — links to DiseaseVersion after accept |
| createdBy | @ManyToOne(User) | |
| createdAt | OffsetDateTime | |
| updatedAt | OffsetDateTime | |
| deletedAt | OffsetDateTime | Soft delete |

Purpose: Container for a generated draft before it becomes an accepted disease version. Tracks origin method for audit.

#### `DiseaseDraftSection`

| Field | Type | Notes |
|-------|------|-------|
| id | Long (PK, auto) | |
| draft | @ManyToOne(DiseaseDraft) | |
| sectionType | Enum: DEFINITION, ETIOLOGY, SYMPTOMS, DIAGNOSIS, TREATMENT, COMPLICATIONS, PREVENTION, REFERENCES | Matches frontend `DRAFT_SECTIONS` |
| content | TEXT, NOT NULL | Generated markdown content |
| orderIndex | Integer | |
| createdAt | OffsetDateTime | |

Purpose: One row per generated section. 8 sections per draft.

#### `DraftSource`

| Field | Type | Notes |
|-------|------|-------|
| id | Long (PK, auto) | |
| section | @ManyToOne(DiseaseDraftSection) | |
| documentId | Long | FK to Document (nullable — source may be URL) |
| documentName | String(255) | Display name for frontend `SectionSource.document` |
| pageNumber | Integer | Nullable |
| paragraphIndex | Integer | Nullable — rough paragraph estimate |
| confidence | BigDecimal(5,4) | AI's confidence score 0-1 |
| createdAt | OffsetDateTime | |

Purpose: Ground-truth provenance. Maps each generated claim back to its source document.

#### `DraftProvenance`

| Field | Type | Notes |
|-------|------|-------|
| id | Long (PK, auto) | |
| draft | @OneToOne(DiseaseDraft) | |
| generatedBy | String(100) | "system" or user email |
| model | String(100) | AI model ID, e.g. "gpt-4o" |
| provider | String(50) | "nine-router" |
| promptVersion | String(20) | Prompt template version |
| promptTokens | int | |
| completionTokens | int | |
| totalTokens | int | |
| latencyMs | int | |
| generatedAt | OffsetDateTime | |
| createdAt | OffsetDateTime | |

Purpose: Full audit trail for AI-generated drafts. Maps to frontend `DraftProvenance` shape.

### 4.2 Lifecycle

```
     ┌─────────────────────────────────────────────────────┐
     │                  Draft Creation                      │
     │  Upload → Import URL → AI Generate → Manual         │
     └────────────────────────┬────────────────────────────┘
                              ↓
                     ┌────────────────┐
                     │  DRAFT         │ ◄── initial state
                     │  (editable)    │
                     └───────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ↓              ↓              ↓
         [Accept]      [Reject]       [Archive]
              │              │
              ↓              ↓
     ┌────────────────┐  ┌────────────────┐
     │ ACCEPTED       │  │ REJECTED       │
     │ (immutable)    │  │ (immutable)    │
     └────────────────┘  └────────────────┘
              │
              ↓
     Optional: create Disease + DiseaseVersion
     with draft content → sections
```

### 4.3 Accept Flow

```
AcceptDraft(draftId)
   │
   ├── Validate draft.status == DRAFT
   ├── Create Disease (if diseaseName doesn't exist by slug)
   │     └─ slug = SlugUtils.toSlug(diseaseName)
   ├── Create DiseaseVersion(status=DRAFT)
   ├── Create 8 DiseaseSections from draft sections
   ├── Link Disease.currentVersion → new version
   ├── Set draft.status = ACCEPTED
   ├── Set draft.acceptedDiseaseId, draft.acceptedVersionId
   ├── Log audit
   └── Return DraftDetailResponse
```

The ACCEPTED draft becomes a real `Disease` + `DiseaseVersion` with standard sections — it enters the existing disease workflow (submit → review → approve).

### 4.4 Reject Flow

```
RejectDraft(draftId)
   ├── Validate draft.status == DRAFT
   ├── Set draft.status = REJECTED
   └── Log audit
```

No Disease is created. The draft is preserved for analytics but not further editable.

### 4.5 Delete Flow

Soft delete via `@SQLRestriction("deleted_at IS NULL")`. Standard pattern identical to `Disease`, `DiseaseVersion`.

### 4.6 Versioning

Draft versioning is separate from DiseaseVersion versioning:

- `DiseaseDraft.version` increments only when an existing draft is regenerated (e.g., user uploads a new source document and re-generates)
- When a draft is accepted, it creates a fresh `DiseaseVersion(versionNumber=next)` starting at version 1
- Draft version history is preserved for audit — previous versions remain as separate `DiseaseDraft` rows with `status=ARCHIVED`

---

## 5 — Database Schema (ERD)

### 5.1 New Tables

```
┌─────────────────────────────────────────────────────────┐
│                     DOCUMENT                             │
├─────────────────────────────────────────────────────────┤
│ id              BIGINT PK AUTO                          │
│ filename        VARCHAR(255) NOT NULL                    │
│ storage_path    VARCHAR(500) NOT NULL                    │
│ content_type    VARCHAR(100)                             │
│ size            BIGINT                                   │
│ page_count      INT                                      │
│ status          VARCHAR(20) NOT NULL DEFAULT 'UPLOADED'  │
│ error_message   TEXT                                     │
│ uploaded_by     BIGINT FK → users(id)                    │
│ created_at      TIMESTAMPTZ                              │
│ updated_at      TIMESTAMPTZ                              │
│ deleted_at      TIMESTAMPTZ                              │
│ version         INT NOT NULL DEFAULT 0                   │
└───────────────────────┬─────────────────────────────────┘
                        │ 1
                        │
                        │ *
┌───────────────────────┴─────────────────────────────────┐
│                    DOCUMENT_CHUNK                         │
├──────────────────────────────────────────────────────────┤
│ id              BIGINT PK AUTO                           │
│ document_id     BIGINT FK → document(id) NOT NULL        │
│ chunk_index     INT NOT NULL                             │
│ content         TEXT NOT NULL                            │
│ page_number     INT                                      │
│ char_start      INT                                      │
│ char_end        INT                                      │
│ token_count     INT                                      │
│ created_at      TIMESTAMPTZ                              │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                    DISEASE_DRAFT                          │
├──────────────────────────────────────────────────────────┤
│ id                BIGINT PK AUTO                         │
│ disease_name      VARCHAR(255) NOT NULL                   │
│ status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT'    │
│ method            VARCHAR(20) NOT NULL                    │
│ source_label      VARCHAR(255)                            │
│ source_url        VARCHAR(500)                            │
│ original_filename VARCHAR(255)                            │
│ version           INT NOT NULL DEFAULT 1                  │
│ accepted_disease_id  BIGINT FK → disease(id)              │
│ accepted_version_id  BIGINT FK → disease_version(id)      │
│ created_by        BIGINT FK → users(id) NOT NULL          │
│ created_at        TIMESTAMPTZ                             │
│ updated_at        TIMESTAMPTZ                             │
│ deleted_at        TIMESTAMPTZ                             │
│ version           INT NOT NULL DEFAULT 0                  │
└──────────┬───────────────────────────────────────────────┘
           │ 1
           │
           │ *
┌──────────┴───────────────────────────────────────────────┐
│                 DISEASE_DRAFT_SECTION                      │
├──────────────────────────────────────────────────────────┤
│ id              BIGINT PK AUTO                            │
│ draft_id        BIGINT FK → disease_draft(id) NOT NULL    │
│ section_type    VARCHAR(30) NOT NULL                       │
│ content         TEXT NOT NULL                              │
│ order_index     INT NOT NULL                              │
│ created_at      TIMESTAMPTZ                               │
└──────────┬───────────────────────────────────────────────┘
           │ 1
           │
           │ *
┌──────────┴───────────────────────────────────────────────┐
│                    DRAFT_SOURCE                            │
├──────────────────────────────────────────────────────────┤
│ id              BIGINT PK AUTO                            │
│ section_id      BIGINT FK → disease_draft_section(id)     │
│ document_id     BIGINT FK → document(id)                  │
│ document_name   VARCHAR(255) NOT NULL                     │
│ page_number     INT                                       │
│ paragraph_index INT                                       │
│ confidence      DECIMAL(5,4)                              │
│ created_at      TIMESTAMPTZ                               │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                  DRAFT_PROVENANCE                          │
├──────────────────────────────────────────────────────────┤
│ id              BIGINT PK AUTO                            │
│ draft_id        BIGINT FK → disease_draft(id) UNIQUE      │
│ generated_by    VARCHAR(100)                              │
│ model           VARCHAR(100)                              │
│ provider        VARCHAR(50)                               │
│ prompt_version  VARCHAR(20)                               │
│ prompt_tokens   INT NOT NULL DEFAULT 0                    │
│ completion_tokens INT NOT NULL DEFAULT 0                  │
│ total_tokens    INT NOT NULL DEFAULT 0                    │
│ latency_ms      INT                                       │
│ generated_at    TIMESTAMPTZ                               │
│ created_at      TIMESTAMPTZ                               │
└──────────────────────────────────────────────────────────┘
```

### 5.2 Why each table exists

| Table | Rationale |
|-------|-----------|
| `document` | Every uploaded file needs a DB row for lifecycle tracking, audit, and future cleanup. FK to `users` for ownership. |
| `document_chunk` | Pre-computed text segments avoid re-chunking on every AI generation. Enables future RAG with embedding search. Separated from `document` because 1 doc → N chunks. |
| `disease_draft` | Central entity for the generation flow. Contains origin metadata (method, source label, URL or filename) to satisfy frontend `DraftCreationMetadata`. |
| `disease_draft_section` | 8 typed sections per draft. Typed (enum) rather than generic JSONB because sections are individually sourced, versioned, and linked to `draft_source`. |
| `draft_source` | Per-section source provenance. Enables frontend `SourceViewer` to show which document each claim came from, with confidence scores. |
| `draft_provenance` | Generation metadata separate from business data. 1:1 with `disease_draft`. Contains AI-specific fields (tokens, latency, model) that business logic doesn't need. |

### 5.3 Key relationships

- `document` 1:N `document_chunk` — delete chunks when document is deleted (cascade)
- `disease_draft` 1:N `disease_draft_section` — cascade all
- `disease_draft_section` 1:N `draft_source` — cascade all
- `disease_draft` 1:1 `draft_provenance` — cascade all
- `disease_draft.accepted_disease_id` → `disease.id` (nullable, set on accept)
- `disease_draft.accepted_version_id` → `disease_version.id` (nullable, set on accept)
- `draft_source.document_id` → `document.id` (nullable — URL imports have no uploaded document)

---

## 6 — REST API

### 6.1 Document Endpoints

#### `POST /api/documents/upload`

Upload a medical document file.

```
Request:  multipart/form-data
          file: File (required, max 50MB)

Response: 201 Created
{
  "success": true,
  "message": "Document uploaded",
  "data": {
    "id": "42",
    "filename": "pneumonia_guidelines.pdf",
    "url": "/api/documents/42/file",
    "size": 245800,
    "contentType": "application/pdf"
  }
}

Errors:
  400 — Invalid file type (must be PDF/DOCX/TXT/MD)
  400 — File too large (max 50MB)
  413 — Payload too large (Spring multipart limit)

Security: @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
```

#### `POST /api/documents/{id}/extract`

Extract text from an uploaded document.

```
Request:  (none — operates on stored document)

Response: 200 OK
{
  "success": true,
  "message": "Text extracted",
  "data": {
    "documentId": "42",
    "filename": "pneumonia_guidelines.pdf",
    "text": "Full extracted text content...",
    "pageCount": 15
  }
}

Errors:
  404 — Document not found
  409 — Document not in READY status
  422 — Text extraction failed

Security: @PreAuthorize("hasAuthority('DOCUMENT_READ')")
```

#### `POST /api/documents/import-url`

Import article content from a trusted medical URL.

```
Request:
{
  "url": "https://www.who.int/news-room/...",
  "diseaseName": "string (optional)"
}

Response: 201 Created
{
  "success": true,
  "message": "Article imported",
  "data": {
    "id": "43",
    "title": "Pneumonia - WHO Fact Sheet",
    "content": "Extracted article markdown...",
    "source": "https://www.who.int/news-room/..."
  }
}

Errors:
  400 — Invalid URL format
  400 — Untrusted source domain
  400 — Failed to fetch URL
  422 — No extractable content found

Security: @PreAuthorize("hasAuthority('DOCUMENT_IMPORT')")
```

#### `GET /api/documents/{id}`

Get document metadata.

```
Response: 200 OK (DocumentUploadResponse shape)
```

#### `GET /api/documents`

List user's documents (paginated).

```
Query:    ?page=0&size=20&status=READY
Response: 200 OK (PagedResponse<DocumentUploadResponse>)
```

#### `DELETE /api/documents/{id}`

Soft delete a document.

```
Response: 200 OK
Security: @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
```

### 6.2 Draft Endpoints

#### `POST /api/drafts/generate`

Generate a structured disease draft from source documents.

```
Request:
{
  "diseaseName": "Pneumonia",                        // required
  "documentIds": [42, 43],                           // optional — uploaded doc IDs
  "url": "https://...",                              // optional — single URL source
  "model": "gpt-4o",                                 // optional — defaults to prompt template model
  "temperature": 0.3                                 // optional — defaults to prompt template temp
}

Response: 201 Created
{
  "success": true,
  "message": "Draft generated",
  "data": {
    "draftId": 1,
    "diseaseName": "Pneumonia",
    "sections": [
      {
        "sectionType": "DEFINITION",
        "content": "Pneumonia is an infection...",
        "sources": [
          {
            "document": "pneumonia_guidelines.pdf",
            "page": 3,
            "paragraph": null,
            "confidence": 0.92
          }
        ]
      }
      // ... 7 more sections
    ],
    "provenance": {
      "generatedBy": "system",
      "model": "gpt-4o",
      "generatedAt": "2026-07-06T10:30:00Z",
      "promptVersion": "1.0"
    }
  }
}

Note: response/draftId is the DiseaseDraft.id. The nested `sections` array maps
      directly to frontend DraftGenerationResponse shape.

Errors:
  400 — diseaseName is required
  400 — No source provided (need documentIds or url)
  402 — AI quota exceeded
  429 — AI rate limited
  502 — AI provider error

Security: @PreAuthorize("hasAuthority('DRAFT_GENERATE')")
```

#### `GET /api/drafts/{id}`

Get a draft with all sections, sources, and provenance.

```
Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "diseaseName": "Pneumonia",
    "status": "DRAFT",
    "method": "UPLOAD",
    "sourceLabel": "pneumonia_guidelines.pdf",
    "version": 1,
    "sections": [ ... ],
    "provenance": { ... },
    "createdAt": "...",
    "acceptedDiseaseId": null,
    "acceptedVersionId": null
  }
}
```

#### `GET /api/drafts`

List current user's drafts (paginated).

```
Query:    ?page=0&size=20&status=DRAFT&method=UPLOAD
Response: 200 OK (PagedResponse<DraftResponse>)
```

Where `DraftResponse` is summary (no sections, no provenance).

#### `POST /api/drafts/{id}/accept`

Accept a draft — creates Disease + DiseaseVersion with draft content.

```
Request (optional body):
{
  "categoryId": 1     // optional — assign disease to category
}

Response: 200 OK
{
  "success": true,
  "message": "Draft accepted",
  "data": {
    "id": 1,
    "status": "ACCEPTED",
    "acceptedDiseaseId": 15,
    "acceptedVersionId": 23,
    // ... full DraftDetailResponse
  }
}

Errors:
  400 — Draft is not in DRAFT status
  409 — Disease with slug already exists
  422 — Draft has no generated sections

Security: @PreAuthorize("hasAuthority('DRAFT_ACCEPT')")
```

#### `POST /api/drafts/{id}/reject`

Reject a draft.

```
Request (optional body):
{
  "reason": "Insufficient source quality"
}

Response: 200 OK
{
  "success": true,
  "message": "Draft rejected",
  "data": { "id": 1, "status": "REJECTED" }
}

Security: @PreAuthorize("hasAuthority('DRAFT_REJECT')")
```

#### `DELETE /api/drafts/{id}`

Soft delete a draft.

```
Response: 200 OK
Security: @PreAuthorize("hasAuthority('DRAFT_DELETE')")
```

### 6.3 AI Draft Admin Endpoints

#### `GET /api/admin/drafts/stats`

Admin stats on drafts (count by status, method, token usage).

```
Response: 200 OK
{
  "totalDrafts": 150,
  "byStatus": { "DRAFT": 80, "ACCEPTED": 50, "REJECTED": 20 },
  "byMethod": { "UPLOAD": 40, "IMPORT_URL": 30, "AI_GENERATE": 80 },
  "totalTokensUsed": 2450000
}

Security: @PreAuthorize("hasAuthority('AI_VIEW_USAGE')")
```

### 6.4 Endpoint Summary

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/api/documents/upload` | DOCUMENT_UPLOAD | Upload file |
| POST | `/api/documents/{id}/extract` | DOCUMENT_READ | Extract text |
| POST | `/api/documents/import-url` | DOCUMENT_IMPORT | Import URL |
| GET | `/api/documents/{id}` | DOCUMENT_READ | Get doc metadata |
| GET | `/api/documents` | DOCUMENT_READ | List user docs |
| DELETE | `/api/documents/{id}` | DOCUMENT_DELETE | Soft delete doc |
| POST | `/api/drafts/generate` | DRAFT_GENERATE | AI generate draft |
| GET | `/api/drafts/{id}` | DRAFT_READ | Get draft detail |
| GET | `/api/drafts` | DRAFT_READ | List user drafts |
| POST | `/api/drafts/{id}/accept` | DRAFT_ACCEPT | Accept → Disease |
| POST | `/api/drafts/{id}/reject` | DRAFT_REJECT | Reject draft |
| DELETE | `/api/drafts/{id}` | DRAFT_DELETE | Soft delete draft |
| GET | `/api/admin/drafts/stats` | AI_VIEW_USAGE | Admin stats |

---

## 7 — AI Integration Flow

### 7.1 Generation Pipeline

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│   User   │   │ Frontend │   │  Draft   │   │   AI     │   │  Prompt  │   │  AI      │
│          │   │          │   │ Service  │   │  Draft   │   │ Template │   │ Gateway  │
│          │   │          │   │          │   │Generator │   │  Service │   │  Router  │
└────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘
     │  1. POST     │              │              │              │              │
     │  /drafts/    │              │              │              │              │
     │  generate    │              │              │              │              │
     ├─────────────►│              │              │              │              │
     │              │  2. Request  │              │              │              │
     │              ├─────────────►│              │              │              │
     │              │              │  3. Load     │              │              │
     │              │              │  documents + │              │              │
     │              │              │  chunks      │              │              │
     │              │              ├─────►Document│              │              │
     │              │              │◄────Service  │              │              │
     │              │              │              │              │              │
     │              │              │  4. Generate │              │              │
     │              │              ├─────────────►│              │              │
     │              │              │              │  5. Get      │              │
     │              │              │              │  active      │              │
     │              │              │              │  prompt      │              │
     │              │              │              │  template    │              │
     │              │              │              ├─────────────►│              │
     │              │              │              │◄─────────────┤              │
     │              │              │              │              │              │
     │              │              │              │  6. Build    │              │
     │              │              │              │  messages    │              │
     │              │              │              │  from        │              │
     │              │              │              │  template +  │              │
     │              │              │              │  chunks      │              │
     │              │              │              │              │              │
     │              │              │              │  7. Chat     │              │
     │              │              │              │  request     │              │
     │              │              │              ├─────────────►│              │
     │              │              │              │              │  8. Route    │
     │              │              │              │              │  to provider │
     │              │              │              │              ├─────►...     │
     │              │              │              │              │◄─────        │
     │              │              │              │◄─────────────┤              │
     │              │              │              │              │              │
     │              │              │              │  9. Validate │              │
     │              │              │              │  output      │              │
     │              │              │              │  (injection, │              │
     │              │              │              │   structure) │              │
     │              │              │              │              │              │
     │              │              │              │  10. Parse   │              │
     │              │              │              │  JSON →      │              │
     │              │              │              │  DraftSections│             │
     │              │              │              │              │              │
     │              │              │  11. Save    │              │              │
     │              │              │◄─────────────┤              │              │
     │              │              │              │              │              │
     │              │              │  12. Log     │              │              │
     │              │              │  usage +     │              │              │
     │              │              │  provenance  │              │              │
     │              │              │              │              │              │
     │              │  13. Return  │              │              │              │
     │              │◄─────────────┤              │              │              │
     │  14.         │              │              │              │              │
     │  Response    │              │              │              │              │
     │◄─────────────┤              │              │              │              │
```

### 7.2 Prompt Architecture

A new prompt template is seeded with code `DRAFT_GENERATE`:

```
Code:       DRAFT_GENERATE
Model:      gpt-4o (default)
Temperature: 0.3
MaxTokens:  4096

System prompt:
  You are a medical content generation assistant. Generate structured disease
  content EXCLUSIVELY from the provided source text. Never use your general
  knowledge. If information for a section is not found in the sources, output
  an empty string for that section. Respond with valid JSON only.

User prompt template:
  Disease: {{disease_name}}
  Source text:
  {{source_chunks}}

  Generate a structured disease entry with these sections as JSON:
  {
    "definition": { "content": "...", "sources": [...] },
    "etiology": { "content": "...", "sources": [...] },
    "symptoms": { "content": "...", "sources": [...] },
    "diagnosis": { "content": "...", "sources": [...] },
    "treatment": { "content": "...", "sources": [...] },
    "complications": { "content": "...", "sources": [...] },
    "prevention": { "content": "...", "sources": [...] },
    "references": { "content": "...", "sources": [...] }
  }

  Each source entry: { "document": "<filename>", "page": <N|null>,
                       "paragraph": <N|null>, "confidence": <0.0-1.0> }
```

### 7.3 Source Chunk Strategy

`AiDraftGeneratorImpl.buildSourceContext()`:

1. Load all `DocumentChunk` for the provided document IDs, ordered by `chunkIndex`
2. Tokenize each chunk, concatenate with delimiters
3. If total exceeds context budget (model max - prompt overhead - 4096 for output):
   - Priority: prefer earlier chunks (first pages usually contain definition/overview)
   - Budget each chunk proportionally, truncate tail
4. Pass as `{{source_chunks}}` variable

Chunk-level linking:
- Each returned source in the AI response cites a `document` name
- `AiDraftGeneratorImpl` post-processes: maps document name → document ID, chunks the paragraph index based on char offset heuristics
- Confidence is parsed from AI output as-is (the prompt instructs the model to assign confidence)

### 7.4 Output Validation

`AiOutputValidator` (existing class) runs:

1. Injection filter — reject if prompt injection patterns detected
2. Output filter — reject if output contains harmful content
3. JSON parse — `AiDraftGeneratorImpl` attempts `ObjectMapper.readValue()` on the response text
4. Schema validation — verify all 8 section keys present, each has `content` (String) and `sources` (array)
5. If validation fails → `AiOutputFilteredException` → frontend sees 400 with "Generation output failed validation"

### 7.5 Reused AI Infrastructure

| Component | Used for |
|-----------|----------|
| `AiGatewayRouter` | Route `AiChatRequest` to `NineRouterGateway` |
| `AiQuotaService` | Check user quota before generating |
| `AiRateLimiter` | Rate-limit generation requests |
| `AiOutputValidator` | Validate AI output safety + structure |
| `AiUsageService` | Log token usage per generation |
| `PromptTemplateService` | Look up active `DRAFT_GENERATE` prompt template |
| `PromptBuilder` | Build messages from template + variables |
| `AiCacheService` | Cache identical generation requests (hash of diseaseName + chunk IDs) |

No new AI provider or gateway code is needed.

---

## 8 — Sequence Diagrams

### 8.1 Upload → Extract → Generate flow

```
User                  Frontend                DocumentController   DocumentService     Storage
 │                        │                        │                   │                 │
 │ Upload PDF             │                        │                   │                 │
 ├───────────────────────►│                        │                   │                 │
 │                        │ POST /documents/upload │                   │                 │
 │                        ├───────────────────────►│                   │                 │
 │                        │                        │ validate + save   │                 │
 │                        │                        ├──────────────────►│                 │
 │                        │                        │                   │ write file      │
 │                        │                        │                   ├────────────────►│
 │                        │                        │                   │◄────────────────┤
 │                        │                        │◄──────────────────┤                 │
 │                        │◄───────────────────────┤                   │                 │
 │◄───────────────────────┤                        │                   │                 │
 │                        │                        │                   │                 │
 │ Extract text           │                        │                   │                 │
 ├───────────────────────►│                        │                   │                 │
 │                        │ POST /documents/42/    │                   │                 │
 │                        │     extract            │                   │                 │
 │                        ├───────────────────────►│                   │                 │
 │                        │                        │ read file → Tika  │                 │
 │                        │                        ├──────────────────►│                 │
 │                        │                        │                   │                 │
 │                        │                        │ chunk text        │                 │
 │                        │                        │ save chunks       │                 │
 │                        │◄───────────────────────┤                   │                 │
 │◄───────────────────────┤                        │                   │                 │
 │                        │                        │                   │                 │
 │ Generate draft         │                        │                   │                 │
 ├───────────────────────►│                        │                   │                 │
 │                        │ POST /drafts/generate  │                   │                 │
 │                        ├───────────────────────►│ (AiDraftController)                │
 │                        │                        │  │                                │
 │                        │                        │  ▼ AiDraftGenerator                │
 │                        │                        ├──────────────────────────────────►│
 │                        │                        │  (prompt + chunks → AI → parse)   │
 │                        │                        │◄──────────────────────────────────┤
 │                        │◄───────────────────────┤                                   │
 │◄───────────────────────┤                        │                                   │
```

### 8.2 Accept → Disease creation

```
DraftController        DraftService       DiseaseService    DiseaseVersionService
     │                      │                  │                   │
     │ POST /drafts/1/      │                  │                   │
     │   accept             │                  │                   │
     ├─────────────────────►│                  │                   │
     │                      │ validate DRAFT   │                   │
     │                      │ status           │                   │
     │                      │                  │                   │
     │                      │ create Disease   │                   │
     │                      ├─────────────────►│                   │
     │                      │◄─────────────────┤                   │
     │                      │                  │                   │
     │                      │ create           │                   │
     │                      │ DiseaseVersion   │                   │
     │                      ├────────────────────────────────────►│
     │                      │◄────────────────────────────────────┤
     │                      │                  │                   │
     │                      │ create 8         │                   │
     │                      │ DiseaseSections  │                   │
     │                      │ from draft       │                   │
     │                      │                  │                   │
     │                      │ set draft.status │                   │
     │                      │ = ACCEPTED       │                   │
     │                      │ link disease/    │                   │
     │                      │ version IDs      │                   │
     │                      │                  │                   │
     │◄─────────────────────┤                  │                   │
```

---

## 9 — Implementation Order

### Phase 1 — Document Foundation (independent)

| Step | File(s) | Depends On |
|------|---------|------------|
| 1.1 | `Document` entity + `DocumentRepository` | Nothing |
| 1.2 | `DocumentChunk` entity + `DocumentChunkRepository` | 1.1 |
| 1.3 | `DocumentStorageService` interface + `LocalFileStorageServiceImpl` | Nothing |
| 1.4 | `DocumentMapper` | 1.1 |
| 1.5 | `DocumentUploadResponse`, `ExtractedTextResponse`, `UrlImportResponse` DTOs | Nothing |
| 1.6 | `DocumentService` interface + `DocumentServiceImpl` | 1.1–1.5 |
| 1.7 | `DocumentController` (upload, extract, import-url, get, list, delete) | 1.6 |
| 1.8 | Document exceptions (3 classes) | Nothing |
| 1.9 | `PermissionCode` entries: `DOCUMENT_UPLOAD`, `DOCUMENT_READ`, `DOCUMENT_IMPORT`, `DOCUMENT_DELETE` | (Existing enum — add entries) |
| 1.10 | Tests: `DocumentServiceImplTest`, `DocumentControllerTest` | 1.7 |

### Phase 2 — Draft Foundation (depends on Phase 1)

| Step | File(s) | Depends On |
|------|---------|------------|
| 2.1 | `DraftStatus`, `DraftMethod`, `DraftSectionType` enums | Nothing |
| 2.2 | `DiseaseDraft` entity + `DiseaseDraftRepository` | 2.1 |
| 2.3 | `DiseaseDraftSection` entity + `DiseaseDraftSectionRepository` | 2.2 |
| 2.4 | `DraftSource` entity + `DraftSourceRepository` | 2.3 |
| 2.5 | `DraftProvenance` entity + `DraftProvenanceRepository` | 2.2 |
| 2.6 | `DraftMapper` | 2.2–2.5 |
| 2.7 | All draft DTOs (request + response) | 2.1 |
| 2.8 | Draft exceptions (3 classes) | Nothing |
| 2.9 | `PermissionCode` entries: `DRAFT_GENERATE`, `DRAFT_READ`, `DRAFT_ACCEPT`, `DRAFT_REJECT`, `DRAFT_DELETE` | (Existing enum) |

### Phase 3 — AI Generation (depends on Phase 1 + 2)

| Step | File(s) | Depends On |
|------|---------|------------|
| 3.1 | `AiDraftGenerator` interface | 2.2, 2.3 |
| 3.2 | `AiDraftGeneratorImpl` (prompt + chunk → AI → parse) | 3.1, 1.2, existing AI infra |
| 3.3 | `DraftService` interface + `DraftServiceImpl` (CRUD + generate orchestration) | 2.2–2.7, 3.2 |
| 3.4 | `DraftLifecycleService` interface + `DraftLifecycleServiceImpl` (accept/reject) | 2.2, 2.3, existing DiseaseService |
| 3.5 | `DraftController` (list, get, delete) | 3.3 |
| 3.6 | `AiDraftController` (generate) | 3.3 |
| 3.7 | `DraftAdminController` (stats) | 3.3 |
| 3.8 | Seed prompt template `DRAFT_GENERATE` (via migration) | (migration — out of scope for this design) |
| 3.9 | Tests: `AiDraftGeneratorImplTest`, `DraftServiceImplTest`, `DraftLifecycleServiceImplTest`, controllers | 3.7 |

### Parallelism

- Phases 1 and 2 are fully parallel (different entities, no cross-dependency)
- Phase 3 depends on both — start after Phases 1+2 are done
- Within each phase, steps can be parallelized per the usual pattern (all entities in parallel → all DTOs → all services → controllers)

---

## 10 — New Permissions

Add to existing `PermissionCode` enum:

```java
// Document domain
DOCUMENT_UPLOAD,
DOCUMENT_READ,
DOCUMENT_IMPORT,
DOCUMENT_DELETE,

// Draft domain
DRAFT_GENERATE,
DRAFT_READ,
DRAFT_ACCEPT,
DRAFT_REJECT,
DRAFT_DELETE,
```

No new roles — existing `USER` role gets `DOCUMENT_UPLOAD`, `DOCUMENT_READ`, `DOCUMENT_IMPORT`, `DRAFT_GENERATE`, `DRAFT_READ` via the same `role_permission` seeding pattern. `REVIEWER` additionally gets `DRAFT_ACCEPT`, `DRAFT_REJECT`. `ADMIN` gets all plus `DOCUMENT_DELETE`, `DRAFT_DELETE`.

---

## Appendix A — Frontend Type → Backend Shape Mapping

| Frontend Type | Backend Source | Notes |
|---------------|----------------|-------|
| `DraftCreationMethod` | `DraftMethod` enum | Direct enum mapping |
| `DraftCreationMetadata` | `DiseaseDraft` entity fields | Decomposed on accept |
| `DraftGenerationResponse` | `DraftGenerationResponse` DTO | Direct 1:1 mapping |
| `GeneratedSection` | `DraftSectionResponse` DTO | Plus `sectionType` |
| `SectionSource` | `DraftSourceResponse` DTO | Direct 1:1 |
| `DraftProvenance` | `DraftProvenanceResponse` DTO | Direct 1:1 |
| `DocumentUploadResponse` | `DocumentUploadResponse` DTO | Direct 1:1 |
| `ExtractedTextResponse` | `ExtractedTextResponse` DTO | Direct 1:1 |
| `UrlImportResponse` | `UrlImportResponse` DTO | Plus `id` field |
| `DRAFT_SECTIONS` array | `DraftSectionType` enum + ordering | Seed via `orderIndex` |

---

## Appendix B — Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Separate `DiseaseDraft` from `Disease`/`DiseaseVersion` | Drafts are ephemeral — before accept they don't belong in the production disease hierarchy |
| `Document` as separate aggregate | Documents can be reused across multiple drafts (upload once, generate multiple variations) |
| `DraftSource` as its own table | Normalized provenance enables queries like "which drafts cited this document" |
| Pre-chunking in `DocumentChunk` | Avoid re-extracting and re-chunking on every generation — critical for performance |
| Local file storage | No external dependencies, consistent with project's existing infrastructure |
| No embedding/RAG in initial sprint | Frontend explicitly shows "RAG Workflow (Future)" — keep scope minimal |
| `DraftSectionType` as Java enum not DB table | 8 fixed sections, no need for dynamic types (unlike existing `SectionType` which grows organically) |
| `DraftSource` per section not per draft | Frontend `SourceViewer` renders sources per-section |
