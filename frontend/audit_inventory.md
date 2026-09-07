# Backend API Inventory

## Disease Management

### DiseaseController (`/api/diseases`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/diseases` | POST | Create disease with initial draft version | CreateDiseaseRequest | ApiResponse<DiseaseResponse> | Yes | DISEASE_WRITE |
| `/api/diseases/draft` | POST | Create disease draft | CreateDiseaseDraftRequest | ApiResponse<DiseaseResponse> | Yes | DISEASE_WRITE |
| `/api/diseases` | GET | List approved diseases (with filters) | - | ApiResponse<PagedResponse<DiseaseSummaryProjection>> | No | - |
| `/api/diseases/search` | GET | Full-text search approved diseases by keyword | - | ApiResponse<PagedResponse<DiseaseSummaryProjection>> | No | - |
| `/api/diseases/search` | POST | Advanced disease search with filters in body | DiseaseSearchRequest | ApiResponse<PagedResponse<DiseaseSummaryProjection>> | No | - |
| `/api/diseases/{id}` | GET | Get disease by ID | - | ApiResponse<DiseaseResponse> | No | - |
| `/api/diseases/slug/{slug}` | GET | Get disease by slug | - | ApiResponse<DiseaseDetailResponse> | No | - |
| `/api/diseases/{id}/current-version` | GET | Get disease current approved version detail | - | ApiResponse<DiseaseDetailResponse> | No | - |
| `/api/diseases/{id}` | PUT | Update disease metadata | UpdateDiseaseRequest | ApiResponse<DiseaseResponse> | Yes | DISEASE_WRITE |
| `/api/diseases/{id}/clone-current-version` | POST | Clone current approved version into a new draft | - | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_WRITE |
| `/api/diseases/{id}` | DELETE | Soft delete disease | - | ApiResponse<Void> | Yes | DISEASE_DELETE |
| `/api/diseases/{id}/restore` | PATCH | Restore soft-deleted disease | - | ApiResponse<Void> | Yes | DISEASE_RESTORE |
| `/api/diseases/{id}/category/{categoryId}` | PATCH | Assign category to disease | - | ApiResponse<Void> | Yes | DISEASE_MANAGE |
| `/api/diseases/{id}/category` | DELETE | Remove category from disease | - | ApiResponse<Void> | Yes | DISEASE_MANAGE |

### DiseaseVersionController (`/api/versions`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/versions/draft` | POST | Create draft version for a disease | CreateDiseaseVersionRequest (with diseaseId param) | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_WRITE |
| `/api/versions/clone/{diseaseId}` | POST | Clone approved version into a new draft | - | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_WRITE |
| `/api/versions/{versionId}` | GET | Get version by ID | - | ApiResponse<DiseaseVersionResponse> | No | - |
| `/api/versions/disease/{diseaseId}` | GET | List all versions for a disease | - | ApiResponse<List<DiseaseVersionResponse>> | No | - |
| `/api/versions/disease/{diseaseId}/current` | GET | Get current approved version | - | ApiResponse<DiseaseVersionResponse> | No | - |
| `/api/versions/disease/{diseaseId}/latest-draft` | GET | Get latest draft version for a disease | - | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_READ |
| `/api/versions/pending-review` | GET | List versions pending review | - | ApiResponse<PagedResponse<DiseaseVersionResponse>> | Yes | VERSION_REVIEW |
| `/api/versions/{versionId}` | PUT | Update draft version content | UpdateDiseaseVersionRequest | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_WRITE |
| `/api/versions/{versionId}/submit` | POST | Submit version for review | - | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_WRITE |
| `/api/versions/{versionId}/approve` | POST | Approve a version | ModerationRequest | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_REVIEW |
| `/api/versions/{versionId}/reject` | POST | Reject a version | ModerationRequest | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_REVIEW |
| `/api/versions/{versionId}/archive` | POST | Archive a version | - | ApiResponse<DiseaseVersionResponse> | Yes | VERSION_WRITE |
| `/api/versions/{versionId}` | DELETE | Soft delete a version | - | ApiResponse<Void> | Yes | VERSION_DELETE |
| `/api/versions/{versionId}/restore` | PATCH | Restore soft-deleted version | - | ApiResponse<Void> | Yes | VERSION_RESTORE |

### DiseaseSectionController (`/api/sections`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/sections` | POST | Create section | CreateDiseaseSectionRequest (with versionId param) | ApiResponse<DiseaseSectionResponse> | Yes | VERSION_WRITE |
| `/api/sections/batch` | POST | Create sections in batch | List<CreateDiseaseSectionRequest> (with versionId param) | ApiResponse<List<DiseaseSectionResponse>> | Yes | VERSION_WRITE |
| `/api/sections/{sectionId}` | GET | Get section by ID | - | ApiResponse<DiseaseSectionResponse> | No | - |
| `/api/sections/version/{versionId}` | GET | List sections by version | - | ApiResponse<List<DiseaseSectionResponse>> | No | - |
| `/api/sections/version/{versionId}/type/{sectionType}` | GET | Get section by version and type | - | ApiResponse<DiseaseSectionResponse> | No | - |
| `/api/sections/{sectionId}` | PUT | Update section | UpdateDiseaseSectionRequest | ApiResponse<DiseaseSectionResponse> | Yes | VERSION_WRITE |
| `/api/sections/version/{versionId}/reorder` | POST | Reorder sections | List<SectionOrderRequest> | ApiResponse<Void> | Yes | VERSION_WRITE |
| `/api/sections/{sectionId}` | DELETE | Delete section | - | ApiResponse<Void> | Yes | VERSION_WRITE |
| `/api/sections/version/{versionId}` | DELETE | Soft delete sections by version | - | ApiResponse<Void> | Yes | VERSION_DELETE |
| `/api/sections/version/{versionId}/validate` | POST | Validate required sections | - | ApiResponse<Void> | Yes | VERSION_WRITE |
| `/api/sections/{sectionId}/markdown` | GET | Render section markdown | - | ApiResponse<String> | No | - |
| `/api/sections/types` | GET | List section types | - | ApiResponse<List<SectionTypeResponse>> | No | - |
| `/api/sections/templates` | GET | List default section templates | - | ApiResponse<List<SectionTemplateResponse>> | No | - |

### CaseStudyController (`/api/cases`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/cases` | GET | List approved case studies | - | ApiResponse<PagedResponse<CaseStudySummaryProjection>> | No | - |
| `/api/cases/{id}` | GET | Get case study by ID | - | ApiResponse<CaseStudyDetailResponse> | No | - |
| `/api/cases/slug/{slug}` | GET | Get case study by slug | - | ApiResponse<CaseStudyDetailResponse> | No | - |
| `/api/cases` | POST | Create case study | CreateCaseStudyRequest | ApiResponse<CaseStudyDetailResponse> | Yes | DISEASE_WRITE |
| `/api/cases/{id}/diagnose` | POST | Submit case study diagnosis | DiagnoseRequest | ApiResponse<DiagnoseResponse> | No | - |

### CategoryController (`/api/categories`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/categories` | GET | List categories (sorted by name) | - | ApiResponse<List<CategoryResponse>> | No | - |
| `/api/categories/{id}` | GET | Get category by ID | - | ApiResponse<CategoryResponse> | No | - |
| `/api/categories/slug/{slug}` | GET | Get category by slug | - | ApiResponse<CategoryResponse> | No | - |
| `/api/categories` | POST | Create category | CreateCategoryRequest | ApiResponse<CategoryResponse> | Yes | DISEASE_WRITE |
| `/api/categories/{id}` | PUT | Update category | UpdateCategoryRequest | ApiResponse<CategoryResponse> | Yes | DISEASE_MANAGE |
| `/api/categories/{id}` | DELETE | Delete category | - | ApiResponse<Void> | Yes | DISEASE_MANAGE |

### UserController (`/api/users`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/users/me` | GET | Get current user profile | - | ApiResponse<UserResponse> | Yes | - |
| `/api/users/me` | PUT | Update current user profile | UpdateProfileRequest | ApiResponse<UserResponse> | Yes | - |

### NotificationController (`/api/notifications`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/notifications/stream` | GET | Open notification SSE stream | - | SseEmitter | Yes | - |

### SymptomController (`/api/symptoms`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/symptoms` | GET | Get all symptoms | - | ApiResponse<List<SymptomResponse>> | No | - |
| `/api/symptoms/search` | GET | Search symptoms by keyword | keyword param | ApiResponse<List<SymptomResponse>> | No | - |
| `/api/symptoms/{id}` | GET | Get symptom by ID | - | ApiResponse<SymptomResponse> | No | - |
| `/api/symptoms` | POST | Create a symptom | CreateSymptomRequest | ApiResponse<SymptomResponse> | Yes | SYMPTOM_WRITE |
| `/api/symptoms/{id}` | PUT | Update a symptom | UpdateSymptomRequest | ApiResponse<SymptomResponse> | Yes | SYMPTOM_WRITE |
| `/api/symptoms/{id}` | DELETE | Delete a symptom | - | ApiResponse<Void> | Yes | SYMPTOM_DELETE |
| `/api/symptoms/check` | POST | Check symptoms (V1) - Match symptoms against diseases using basic scoring | SymptomCheckerRequest | ApiResponse<List<SymptomMatchResult>> | No | - |

### NotificationController (`/api/notifications`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/notifications/stream` | GET | Open notification SSE stream | - | SseEmitter | Yes | - |

### Flashcard controllers

#### FlashcardController (`/api/flashcards`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/flashcards/generate` | POST | Generate flashcards from a disease or document | FlashcardGenerateRequest | ApiResponse<FlashcardGenerateResponse> | Yes | FLASHCARD_GENERATE |
| `/api/flashcards/{id}` | GET | Get flashcard by ID | - | ApiResponse<FlashcardResponse> | Yes | FLASHCARD_VIEW |
| `/api/flashcards` | GET | List flashcards by user | Pageable | ApiResponse<PagedResponse<FlashcardResponse>> | Yes | FLASHCARD_VIEW |
| `/api/flashcards/decks/{deckId}/cards` | GET | List flashcards in a deck | deckId path param, Pageable | ApiResponse<PagedResponse<FlashcardResponse>> | Yes | FLASHCARD_VIEW |
| `/api/flashcards/{id}` | PUT | Update flashcard content | FlashcardUpdateRequest | ApiResponse<FlashcardResponse> | Yes | FLASHCARD_EDIT |
| `/api/flashcards/{id}` | DELETE | Soft delete flashcard | - | ApiResponse<Void> | Yes | FLASHCARD_DELETE |
| `/api/flashcards/batch-update` | POST | Batch update flashcards | FlashcardBatchUpdateRequest | ApiResponse<Void> | Yes | FLASHCARD_EDIT |
| `/api/flashcards/export/{deckId}` | GET | Export deck as CSV or JSON | deckId path param, format query param | ApiResponse<FlashcardExportResponse> | Yes | FLASHCARD_EXPORT |
| `/api/flashcards/stats` | GET | Get flashcard review statistics | - | ApiResponse<FlashcardStatsResponse> | Yes | FLASHCARD_VIEW |

#### FlashcardDeckController (`/api/flashcards/decks`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/flashcards/decks` | GET | List flashcard decks | admin query param, Pageable | ApiResponse<PagedResponse<FlashcardDeckResponse>> | Yes | FLASHCARD_VIEW |

#### FlashcardReviewController (`/api/flashcards`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/flashcards/due` | GET | Get flashcards due for review | limit query param | ApiResponse<List<FlashcardProgressResponse>> | Yes | FLASHCARD_REVIEW |
| `/api/flashcards/{id}/review` | POST | Submit a flashcard review with SM-2 quality rating | FlashcardReviewRequest | ApiResponse<FlashcardReviewResponse> | Yes | FLASHCARD_REVIEW |

### AdminController (`/api/admin`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/admin/users` | GET | List all users | Pageable | ApiResponse<PagedResponse<UserResponse>> | Yes | USER_VIEW_ALL |
| `/api/admin/users/{id}` | GET | Get user by ID | - | ApiResponse<UserResponse> | Yes | USER_VIEW_ALL |
| `/api/admin/users/{userId}/role` | PATCH | Assign role to user | RoleRequest | ApiResponse<Void> | Yes | ROLE_ASSIGN |
| `/api/admin/users/{userId}/status` | PATCH | Activate or deactivate a user | Map<String, Boolean> (isActive) | ApiResponse<Void> | Yes | USER_MANAGE |
| `/api/admin/analytics` | GET | Get system analytics | - | ApiResponse<Map<String, Object>> | Yes | USER_VIEW_ALL |
| `/api/admin/pending-reviews` | GET | Get pending reviews summary | - | ApiResponse<String> | Yes | VERSION_REVIEW |

### DraftController (`/api/drafts`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/drafts` | POST | Create draft manually | CreateDraftRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts` | GET | List drafts (by diseaseId or current user) | diseaseId param, Pageable | ApiResponse<PagedResponse<DiseaseDraftResponse>> | Yes | DRAFT_VIEW |
| `/api/drafts/{id}` | GET | Get draft by ID | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_VIEW |
| `/api/drafts/{id}` | PUT | Update draft content | UpdateDraftRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts/{id}` | DELETE | Soft delete draft | - | ApiResponse<Void> | Yes | DRAFT_DELETE |
| `/api/drafts/{id}/submit` | POST | Submit draft for review | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts/{id}/approve` | POST | Approve draft | DraftReviewRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_REVIEW |
| `/api/drafts/{id}/reject` | POST | Reject draft | DraftReviewRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_REVIEW |
| `/api/drafts/{id}/archive` | POST | Archive draft | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_DELETE |
| `/api/drafts/{id}/clone` | POST | Clone draft | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts/{id}/apply` | POST | Apply approved draft to disease version | - | ApiResponse<Void> | Yes | DRAFT_REVIEW |

### SymptomController (`/api/symptoms`)

### DraftController (`/api/drafts`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/drafts` | POST | Create draft manually | CreateDraftRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts` | GET | List drafts (by diseaseId or current user) | diseaseId param, Pageable | ApiResponse<PagedResponse<DiseaseDraftResponse>> | Yes | DRAFT_VIEW |
| `/api/drafts/{id}` | GET | Get draft by ID | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_VIEW |
| `/api/drafts/{id}` | PUT | Update draft content | UpdateDraftRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts/{id}` | DELETE | Soft delete draft | - | ApiResponse<Void> | Yes | DRAFT_DELETE |
| `/api/drafts/{id}/submit` | POST | Submit draft for review | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts/{id}/approve` | POST | Approve draft | DraftReviewRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_REVIEW |
| `/api/drafts/{id}/reject` | POST | Reject draft | DraftReviewRequest | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_REVIEW |
| `/api/drafts/{id}/archive` | POST | Archive draft | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_DELETE |
| `/api/drafts/{id}/clone` | POST | Clone draft | - | ApiResponse<DiseaseDraftResponse> | Yes | DRAFT_CREATE |
| `/api/drafts/{id}/apply` | POST | Apply approved draft to disease version | - | ApiResponse<Void> | Yes | DRAFT_REVIEW |

### SymptomController (`/api/symptoms`)

### AiDraftController (`/api/ai/drafts`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/ai/drafts` | POST | Generate draft using AI | AiDraftRequest | ApiResponse<DiseaseDraftResponse> | Yes | AI_DRAFT |

### DocumentController (`/api/documents`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/documents` | POST | Upload document | MultipartFile file, String title | ApiResponse<DocumentResponse> | Yes | DOCUMENT_UPLOAD |
| `/api/documents/import-url` | POST | Import document from URL | String url, String title | ApiResponse<DocumentResponse> | Yes | DOCUMENT_UPLOAD |
| `/api/documents` | GET | List documents | boolean admin, Pageable | ApiResponse<PagedResponse<DocumentResponse>> | Yes | DOCUMENT_VIEW |
| `/api/documents/{id}` | GET | Get document by ID | - | ApiResponse<DocumentResponse> | Yes | DOCUMENT_VIEW |
| `/api/documents/{id}/chunks` | GET | Get document chunks | - | ApiResponse<List<DocumentChunkResponse>> | Yes | DOCUMENT_VIEW |
| `/api/documents/{id}/download` | GET | Download document file | - | byte[] | Yes | DOCUMENT_VIEW |
| `/api/documents/{id}` | DELETE | Soft delete document | - | ApiResponse<Void> | Yes | DOCUMENT_DELETE |
| `/api/documents/{id}/admin` | DELETE | Admin: delete any document | - | ApiResponse<Void> | Yes | DOCUMENT_MANAGE |

### Flashcard controllers

#### FlashcardController (`/api/flashcards`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/flashcards/generate` | POST | Generate flashcards from a disease or document | FlashcardGenerateRequest | ApiResponse<FlashcardGenerateResponse> | Yes | FLASHCARD_GENERATE |
| `/api/flashcards/{id}` | GET | Get flashcard by ID | - | ApiResponse<FlashcardResponse> | Yes | FLASHCARD_VIEW |
| `/api/flashcards` | GET | List flashcards by user | Pageable | ApiResponse<PagedResponse<FlashcardResponse>> | Yes | FLASHCARD_VIEW |
| `/api/flashcards/decks/{deckId}/cards` | GET | List flashcards in a deck | deckId path param, Pageable | ApiResponse<PagedResponse<FlashcardResponse>> | Yes | FLASHCARD_VIEW |
| `/api/flashcards/{id}` | PUT | Update flashcard content | FlashcardUpdateRequest | ApiResponse<FlashcardResponse> | Yes | FLASHCARD_EDIT |
| `/api/flashcards/{id}` | DELETE | Soft delete flashcard | - | ApiResponse<Void> | Yes | FLASHCARD_DELETE |
| `/api/flashcards/batch-update` | POST | Batch update flashcards | FlashcardBatchUpdateRequest | ApiResponse<Void> | Yes | FLASHCARD_EDIT |
| `/api/flashcards/export/{deckId}` | GET | Export deck as CSV or JSON | deckId path param, format query param | ApiResponse<FlashcardExportResponse> | Yes | FLASHCARD_EXPORT |
| `/api/flashcards/stats` | GET | Get flashcard review statistics | - | ApiResponse<FlashcardStatsResponse> | Yes | FLASHCARD_VIEW |

#### FlashcardDeckController (`/api/flashcards/decks`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/flashcards/decks` | GET | List flashcard decks | admin query param, Pageable | ApiResponse<PagedResponse<FlashcardDeckResponse>> | Yes | FLASHCARD_VIEW |

#### FlashcardReviewController (`/api/flashcards`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/flashcards/due` | GET | Get flashcards due for review | limit query param | ApiResponse<List<FlashcardProgressResponse>> | Yes | FLASHCARD_REVIEW |
| `/api/flashcards/{id}/review` | POST | Submit a flashcard review with SM-2 quality rating | FlashcardReviewRequest | ApiResponse<FlashcardReviewResponse> | Yes | FLASHCARD_REVIEW |

### AdminController (`/api/admin`)

| Endpoint | Method | Description | Request DTO | Response DTO | Auth Required | Role/Permission |
|----------|--------|-------------|-------------|--------------|---------------|-----------------|
| `/api/admin/users` | GET | List all users | Pageable | ApiResponse<PagedResponse<UserResponse>> | Yes | USER_VIEW_ALL |
| `/api/admin/users/{id}` | GET | Get user by ID | - | ApiResponse<UserResponse> | Yes | USER_VIEW_ALL |
| `/api/admin/users/{userId}/role` | PATCH | Assign role to user | RoleRequest | ApiResponse<Void> | Yes | ROLE_ASSIGN |
| `/api/admin/users/{userId}/status` | PATCH | Activate or deactivate a user | Map<String, Boolean> (isActive) | ApiResponse<Void> | Yes | USER_MANAGE |
| `/api/admin/analytics` | GET | Get system analytics | - | ApiResponse<Map<String, Object>> | Yes | USER_VIEW_ALL |
| `/api/admin/pending-reviews` | GET | Get pending reviews summary | - | ApiResponse<String> | Yes | VERSION_REVIEW |

### AiDraftController (`/api/ai/drafts`)

## Next Steps

We will now examine each of the controllers listed above to complete the inventory.

We will then compare with the frontend API services and pages.

Let's start by examining the CategoryController.
