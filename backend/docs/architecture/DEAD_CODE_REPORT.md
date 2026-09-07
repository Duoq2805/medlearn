# DEAD_CODE_REPORT.md

**Ngày:** 2026-08-07. Danh sách mã chết full scan. **KHÔNG xóa — đánh dấu TODO** theo yêu cầu.

## 1. Interface không implementation

| File | Package | Lý do dead |
|---|---|---|
| `AiProvider` | `ai/gateway/provider` | Interface có, không `implements AiProvider` nào. `AiGatewayRouter.resolveProvider()` sẽ throw nếu chạm. Pass-through routing chưa dùng |
| `ModerationService` | `common` | Body rỗng, không impl, không call |

## 2. Entity + Repository không được dùng

| File | Ghi chú |
|---|---|
| `DraftSource` (+ `DraftSourceRepository`) | Migration V2_14 tạo bảng `draft_source`; entity có; **không code nào INSERT** (AiDraftGeneratorImpl không ghi) |
| `AiFeedback` + `AiFeedbackRepository` | Entity + repo, không service/controller dùng. Migration V2_10 tạo bảng `ai_feedback` |
| `AiGenerationRepository` | Chỉ `FlashcardGeneratorImpl` đọc (OK) — không dead |
| `CaseStudySummaryProjection` | Dùng trong `CaseStudyRepository` JPQL — active (không dead) |

## 3. DTO unused

| DTO | Package |
|---|---|
| `VersionComparisonResponse` | `ai/dto/response` — không class dùng |
| (kiểm tra thêm) `SectionTemplateResponse` | `knowledge/section/dto/response` — dùng trong getTemplates (active) |

## 4. Endpoint placeholder / fake

| Endpoint | Class:line | Trả về |
|---|---|---|
| `GET /api/admin/analytics` | `AdminController.java:71` | `Map.of("message", "Analytics endpoint. Extend...")` — placeholder, không dữ liệu |
| `GET /api/admin/pending-reviews` | `AdminController.java:78` | String hướng dẫn dùng `/api/versions/pending-review` — không trả dữ liệu |
| `GET /api/ai/stream` (SSE) | `AiStreamController` | mở emitter nhưng `sendAiChunkToUser` chưa ai bắn data |

## 5. Fake / incomplete implementation

| Class | Chi tiết |
|---|---|
| `NineRouterGateway.chatStream` | `// ponytail: streaming via SSE not yet implemented; falls back to non-streaming then simulates chunks` — trả 1 chunk từ full response |
| `DraftLifecycleService.applyToDisease` | **ĐÃ IMPLEMENT trong đợt này** (trước là `log.warn` no-op) — không còn dead |

## 5. Migration không khớp code

| Migration | Bảng | Code dùng? |
|---|---|---|
| `V2_10__create_ai_cache.sql` | `ai_cache` | Không — `AiCacheServiceImpl` in-memory `ConcurrentHashMap`, không đọc/ghi bảng |
| `V2_11__seed_ai_permissions.sql` | `ai` permissions | Có (permission seeds dùng) |
| `V2_14` `document_chunk.embedding vector(1536)` | cột embedding | Entity `DocumentChunk` không map field — chỉ migration |
| `V2_8__create_prompt_test_result.sql` | `prompt_test_result` | Có (PromptTester dùng) |
| `V2_12__create_ai_summary.sql` | `ai_summary` | Có (AiSummaryServiceImpl dùng) |

## 6. TODO trong code (chưa hoàn thiện)

| Vị trí | TODOs |
|---|---|
| `AiSummaryServiceImpl.hasQuota` (AiUsageServiceImpl) | `// ponytail: quota check uses user-level token tracking; add global pool check when needed` |
| `FlashcardGeneratorImpl.java:97` | `// ponytail: disease sections as context when available` (disease source generate bỏ trống context, chỉ document có chunks) |
| `AiCacheServiceImpl` | in-memory max-size clear-all (thô) |
| `AiQuotaService.hasQuota` `monthlyLimit = 1_000_000L` | `// ponytail: read from User entity's monthlyTokenQuota` — hardcode, chưa đọc entity |

## 7. Kết luận

Dead code không ảnh hưởng build/test. Đợt này không xóa gì theo yêu cầu. Khuyến nghị tương lai:
- Xóa `AiProvider` (hoặc thêm impl)
- Xóa `ModerationService` (hoặc implement)
- Quyết định `ai_cache`: DB table hay memory (dùng 1)
- `document_chunk.embedding` hoặc implement RAG hoặc bỏ cột