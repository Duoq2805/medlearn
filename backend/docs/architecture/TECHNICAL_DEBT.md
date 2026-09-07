# TECHNICAL_DEBT.md

**Ngày:** 2026-08-07. Danh sách nợ kỹ thuật (không xóa, đánh dấu TODO).

## 0. Cập nhật đợt này (Phase 1-7 theo quyết định user)

- `knowledge/{version,section,category}`: **giữ nguyên** (quyết định Phase 1 — không merge)
- `JWT_SECRET`: **bắt buộc env** — xóa fallback khỏi mọi profile (quyết định Phase 7)
- `EmailServiceImpl`: move `common/email/impl/` (Phase 3 rule)
- `.env.example` + README env section: đã tạo/cập nhật

## 0b. Cập nhật 2026-08-13 — AI Gateway bug fix

**`AiGatewayRouter.chat()` FIXED** — không còn throw `AiConfigurationException`.

- **Root cause**: `chat(request, model)` gọi `resolveProvider(model)` → `List<AiProvider>` rỗng (không có implementation) → luôn throw
- **Fix**: `chat()` và `chatStream()` đổi sang `resolve(null)` → `NineRouterGateway.chat(request)` (AiGateway path)
- **AiProvider**: GIỮ NGUYÊN interface + `resolveProvider()` — không xóa. Khi add provider trực tiếp (Anthropic SDK, Google AI SDK), implement `AiProvider` và `AiGatewayRouter.chat()` có thể dùng `resolveProvider()` để route model-based
- **Tests**: 340 → 365 (+25 tests). Thêm `AiGatewayRouterTest` (12), `AiGatewayRoutingIntegrationTest` (8), `AiGatewaySpringContextTest` (9)
- **Chi tiết**: xem BUG_REPORT.md #12

## 1. Dead code / chưa hoàn thiện

| Mục | Vị trí | Mô tả | Ưu tiên |
|---|---|---|---|
| `ModerationService` | `common/ModerationService.java` | Interface rỗng, không impl, không ai dùng | Trung bình |
| `AiProvider` | `ai/gateway/provider/AiProvider.java` | Interface không có implementation; `AiGatewayRouter.resolveProvider()` luôn throw nếu gọi trực tiếp. **Bug routing đã fix 2026-08-13** — `chat()` không còn gọi `resolveProvider()`. Interface giữ nguyên cho tương lai multi-provider. | Thấp (không còn blocking) |
| `DraftSource` | `draft/entity/DraftSource.java` + repo | Entity + repo tồn tại, không code nào ghi (AiDraftGeneratorImpl không lưu source chunk) | Thấp |
| `AiFeedback` | `ai/feedback/entity/AiFeedback.java` + repo | Entity + repo, không service/controller | Thấp |
| `VersionComparisonResponse` | `ai/dto/response/VersionComparisonResponse.java` | Không ai dùng | Thấp |
| `ai_cache` bảng DB | `V2_10__create_ai_cache.sql` | Migration tạo bảng nhưng `AiCacheServiceImpl` dùng in-memory ConcurrentHashMap — không đọc/ghi bảng | Trung bình |
| `NineRouterGateway.chatStream` | `ai/gateway/NineRouterGateway.java:61` | Fake streaming: gọi non-streaming rồi trả 1 chunk (comment `ponytail`) | Trung bình |
| `ChatStream` frontend | `AiStreamController` + `sendAiChunkToUser` | SSE endpoint tồn tại, `sendAiChunkToUser()` không ai gọi | Thấp |
| `AdminController.getAnalytics` | `auth/controller/AdminController.java:71` | Trả placeholder Map, không thống kê thật | Thấp |
| `AdminController.getPendingReviews` | `auth/controller/AdminController.java:78` | Trả message chỉ dẫn, không dữ liệu | Thấp |
| `ModerationRequest`/`VersionComparisonResponse` dto | `knowledge/version/dto/request/ModerationRequest.java` | Chỉ dùng trong approve/reject (OK), VersionComparison unused | — |

## 2. Duplicate / trùng lặp

| Mục | Chi tiết |
|---|---|
| **Hai state machine trùng**: `DraftStatus` (DRAFT/PENDING_REVIEW/APPROVED/REJECTED/ARCHIVED) và `VersionStatus` (DRAFT/PENDING_REVIEW/APPROVED/REJECTED/ARCHIVED) — cùng semantics, khác entity | Trung bình — chấp nhận vì giữ 2 concept (draft vs version) |
| **`cloneSections`/`cloneSymptoms`** xuất hiện 2 lần: `DiseaseVersionServiceImpl` + `DiseaseWorkflowServiceImpl` | Trung bình — nên gom vào helper |
| `validateVersionEditable`/`canEditVersion`/`isApprovedVersion` helper trùng trong 2 service | Thấp |
| `generateNextVersionNumber` lặp ở DiseaseVersionService + DiseaseWorkflowService | Thấp |
| `findCurrentUser`/`findCurrentUserWithRoles` lặp khắp service | Thấp |

## 3. Circular dependencies (phát hiện Phase 6 — ghi, không tự sửa)

| Vòng | Chi tiết | Đánh giá |
|---|---|---|
| `knowledge/{version,section,symptom} ↔ kknowledge/disease` | JPA bi-directional entity (Disease.versions ↔ DiseaseVersion.disease, etc) | ✅ **Legit aggregate** — không phải bug; chỉ được phép entity-level |
| **draft ↔ ai** | `DraftController` (draft) → `AiDraftGenerator` (ai); `AiDraftGeneratorImpl` (ai) → `DiseaseDraftRepository` (draft) | ⚠️ **Circular thật** nhưng qua interface — Spring không bean-cycle. Thư mục định hướng gộp AI draft vào draft? |
| **notification ↔ ai** | `AiStreamController` (ai) → `NotificationService`; `NotificationServiceImpl` (notification) → `ai.dto.AiStreamChunkResponse` | ⚠️ Nhẹ: ai→notification dùng service, notification→ai chỉ dùng DTO. Có thể tách chunk DTO sang `common/dto` hoặc `notification/dto` |
| **audit ↔ auth** | `AuditLog.createdBy`→`auth.User`; `auth.*ServiceImpl` → `audit.AuditService` | ⚠️ **Service-level 2 chiều thật**. Nguyên: User là auth entity, Audit dùng nó. Nếu tách User làm module riêng sẽ hết |
| `draft ↔ document` | draft.entity → document | ✅ 1 chiều OK |

> Giải pháp tiềm năng (chưa làm): chuyển `AiStreamChunkResponse` → `common/dto` hoặc `notification/dto` để cắt vòng notification↔ai; tách `User` entity thành module `user/` để cắt vòng audit↔auth.

## 4. God service / fat entity

| Mục | Đánh giá |
|---|---|
| `DiseaseServiceImpl` (473 dòng) — CRUD + search + ownership + detail | Trung bình — tách search được |
| `DiseaseVersionServiceImpl` (385 dòng) — create/clone/approve/reject/archive/delete/restore | Chấp nhận (1 service cho 1 aggregate) |
| `FlashcardGeneratorImpl` (290 dòng) — AI call + parse + persist | Chấp nhận |
| `Disease` entity có helper `hasCurrentVersion` | Nhỏ, chấp nhận |

## 5. Spring best practices

| Kiểm tra | Kết quả |
|---|---|
| Constructor injection (`@RequiredArgsConstructor`) | ✅ nhất quán toàn project |
| `@Transactional` đặt đúng (readOnly cho read) | ✅ |
| `@Service`/`@Repository`/`@Component` | ✅ |
| MapStruct config `common/mapper/MapStructConfig` | ✅ |
| GlobalExceptionHandler `common/exception` | ✅ — handle mọi exception |
| Validation (`@Valid`, `@Validated`) | ✅ trên DTO |

## 6. Bug nhỏ đã thấy (chưa sửa — out of scope đợt này)

| Mục | Chi tiết |
|---|---|
| `DiseaseServiceImpl` javadoc tiếng Việt bị mojibake (UTF-8 vỡ) | Cosmetic |
| `SectionTemplateResponse` dto có template chứa mojibake | Cosmetic |
| `DiseaseController` dùng `@Transactional(readOnly=true)` trên GET | Không hại, có thể bỏ |

## 7. Đề xuất ưu tiên

1. ~~Implement hoặc xóa `AiProvider`~~ — **bug routing đã fix 2026-08-13**; `AiProvider` giữ nguyên không blocking
2. Xóa hoặc implement `ai_cache` DB table vs in-memory `AiCacheServiceImpl`
3. Implement `DraftSource` khi RAG/chunk-source đi vào
4. Gom `cloneSections/cloneSymptoms` + `generateNextVersionNumber` vào 1 helper class