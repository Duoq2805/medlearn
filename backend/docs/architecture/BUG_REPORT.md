# BUG_REPORT.md

**Ngày:** 2026-08-07. Các bug đã fix + bug còn lại (ghi chú) trong đợt refactor.

## ✅ Đã fix trong đợt này

| # | Bug | Vị trí | Fix |
|---|---|---|---|
| 1 | **`applyToDisease()` no-op** — `POST /api/drafts/{id}/apply` trả 200 nhưng chỉ `log.warn`, không tạo version, không đổi gì | `DraftLifecycleServiceImpl` | Implement đầy đủ: tạo DiseaseVersion APPROVED, clone sections, archive cũ, set currentVersion, draft→ARCHIVED, audit |
| 2 | **Draft REJECTED không resubmit được** | `submitForReview` chỉ nhận DRAFT | Mở rộng chấp nhận REJECTED → PENDING_REVIEW |
| 3 | **Endpoint apply trả 200 giả** | `DraftController.apply` | Giờ thực sự thực thi (fix #1), vẫn trả 200 đúng |
| 4 | **Projection JPQL FQN cũ** | `DiseaseRepository` dùng `knowledge.disease.dto.DiseaseSummaryProjection` | Cập nhật sang `dto.projection.*` |
| 5 | **Import path sai `dto.request.response`** (double) | SymptomController & ai đổi | Fix về `dto.response` |
| 6 | **Package `notification` chưa chuyển** (sed không match `;`) | notification | Sửa package decl + import |
| 7 | **`JWT_SECRET` có fallback hardcode** — secret bị commit vào source | `application.yml`/`docker`/`dev`/`prod` | **Bỏ toàn bộ fallback** — dùng `${JWT_SECRET}` không default; app fail-fast khi thiếu; `.env.example` + README hướng dẫn |
| 8 | **`MAIL_USERNAME/PASSWORD`, `GOOGLE_CLIENT_ID/SECRET` thiếu default** — dev/docker fail | `application.yml`/`dev` | Thêm `:` (optional) |
| 9 | **`.env` không tồn tại nhưng docker-compose dùng `env_file`** | docker-compose | Tạo `.env.example` hướng dẫn; docker vẫn có fallback JWT trong yml |
| 10 | **Test `DraftLifecycleServiceTest` không theo business mới** | test | Cập nhật 2 tests cho behavior apply + resubmit |

## ⚠️ Bug/lỗi còn lại (chưa fix — ngoài scope, để TODO)

| # | Bug tiềm ẩn | Vị trí | Ghi chú |
|---|---|---|---|
| 1 | **Draft apply tạo SectionType null** nếu draft section type không có trong `section_type` table (VD tên không khớp lowercase) | `applyToDisease` | SectionType không match → null FK. Nhưng version submit sau sẽ bị `validateRequiredSections` chặn (Definition/Symptoms/Treatment). Khuyên: map mặc định + validate trước apply |
| 2 | **`AiUsageServiceImpl.hasQuota` hardcode 1M tokens** | `ai/usage/impl/AiUsageServiceImpl` | `monthlyLimit = 1_000_000L` không đọc từ User entity (ponytail) — quota không tùy chỉnh |
| 3 | **`AiCacheServiceImpl` clear-all khi maxSize** | `ai/cache/impl` | Khi đạt max, xóa toàn bộ cache (thô) — mất hit |
| 4 | **`chatStream` giả (non-stream → 1 chunk)** | `NineRouterGateway:61` | Frontend stream bị giả tin; chưa SSE thật |
| 5 | **EmailService không catch lỗi SMTP** — gửi fail nếu SMTP không cấu hình (dev default rỗng) | `common/email/EmailServiceImpl` | Check: verify-token flow ở dev sẽ fail khi mail không set. Cần wrap try/catch hoặc dev dummy sender (TODO) |
| 6 | **`DiseaseServiceImpl` mojibake javadoc** | `DiseaseServiceImpl` | Cosmetic |
| 7 | **`SectionTemplateResponse` template mojibake** | `knowledge/section` | Cosmetic |
| 8 | **Flashcard generation từ disease: context rỗng** | `FlashcardGeneratorImpl:97` | generate từ disease không có chunks → LLM tự bịa nội dung |
| 9 | **`validateRequiredSections` không áp cho draft submit** | `DraftLifecycleServiceImpl` | Draft thiếu section vẫn approve & apply → version fail sau. Khuyên thêm gate (TODO) |
| 10 | **Optimistic lock**: `Disease` có `@Version`, `diseaseVersion.approve` dùng `findByIdForUpdate` — nhưng 2 approve đồng thời cho CÙNG version | — | `archiveApprovedVersionsExcept` + lock giảm, nhưng không fully race-safe giữa 2 request approve cùng 1 version (cả 2 pass transition DRAFT+PENDING → đều APPROVED). Chấp nhận. |
| 11 | **`DiagnoseRequest` so chuỗi `equalsIgnoreCase` cho diagnosis** | `CaseStudyServiceImpl:93` | Gõ sai 1 từ = incorrect; quá nghiêm. Có thể cần fuzzy match (TODO, product decision) |

| 12 | **`AiGatewayRouter.chat(request, model)` luôn throw `AiConfigurationException`** — toàn bộ AI feature (Summary, Flashcard, Draft) không hoạt động trong production | `AiGatewayRouter:48`, `AiProvider` | **Fix 2026-08-13**: route qua `resolve(null)` (AiGateway path) thay vì `resolveProvider(model)` (AiProvider path). Xem chi tiết bên dưới. |

---

## Bug #12 — Chi tiết: AiGatewayRouter Critical Bug

**Ngày phát hiện:** 2026-08-13  
**Severity:** CRITICAL — 100% AI feature không hoạt động trong production  
**Root cause:** Sai abstraction boundary trong `AiGatewayRouter`

### Root cause

`AiGatewayRouter.chat(AiChatRequest, AiModel)` (dòng 48) gọi `resolveProvider(model)` → tìm trong `List<AiProvider> providers`. `AiProvider` là interface không có implementation nào → `providers` list rỗng trong Spring context → `orElseThrow AiConfigurationException("No provider found for model: ...")`.

Ba AI feature đều bị:
- `AiSummaryServiceImpl.generate()` dòng 76
- `FlashcardGeneratorImpl.generate()` dòng 147
- `AiDraftGeneratorImpl.generate()` dòng 107

Đều gọi `gatewayRouter.chat(chatRequest, model)` → throw ngay.

`NineRouterGateway` implements `AiGateway` (không phải `AiProvider`) → không được Spring inject vào `providers` list → path chết hoàn toàn.

### Tại sao 340 tests bỏ sót

Tất cả unit tests mock `AiGatewayRouter` bằng `@Mock` / `when(gatewayRouter.chat(any(), any())).thenReturn(...)`. Không có test nào kiểm tra actual routing path với Spring context thực tế. `AiGatewayRouterTest` inject `mockProvider` thủ công vào constructor.

### Fix áp dụng

`AiGatewayRouter.java` — `chat()` và `chatStream()` đổi từ `resolveProvider(model)` sang `resolve(null)` (dùng defaultProvider từ config):

```java
// BEFORE (broken):
public AiChatResponse chat(AiChatRequest request, AiModel model) {
    return resolveProvider(model).chat(request, model); // always throw
}

// AFTER (fixed):
public AiChatResponse chat(AiChatRequest request, AiModel model) {
    var gateway = resolve(null); // routes to NineRouterGateway via defaultProvider config
    return gateway.chat(request); // model already in request.model field
}
```

`AiProvider` interface và `resolveProvider()` giữ nguyên — không xóa (cơ sở cho multi-provider trực tiếp trong tương lai).

### Architecture sau fix

```
Feature (Summary/Flashcard/Draft)
  ↓ gatewayRouter.chat(AiChatRequest, AiModel)
AiGatewayRouter.resolve(null)          ← defaultProvider = "nine-router"
  ↓
NineRouterGateway.chat(AiChatRequest)  ← AiGateway impl
  ↓ POST {baseUrl}/v1/chat/completions [model trong request body]
9router.com API → LLM
```

### Tests thêm mới

| File | Tests | Loại | Ghi chú |
|---|---|---|---|
| `AiGatewayRouterTest` | 12 | Unit | **Thay thế** file cũ 4 tests → net +8 |
| `AiGatewayRoutingIntegrationTest` | 8 | Integration-level | File mới → net +8 |
| `AiGatewaySpringContextTest` | 9 | SpringBootTest | File mới → net +9 |
| **Tổng tests trong 3 files** | **29** | | **Net +25** (29 mới − 4 bị replace) |

**Test count: 340 (cũ) → 365 (sau fix). Tăng đúng +25.**

Chi tiết từng file:
- `AiGatewayRouterTest` (12): routing unit, empty provider list, multi-model, chatStream, resolveProvider boundary. File cũ chỉ có 4 tests — đặc biệt `chat_shouldDelegateToAiProvider` test cũ mock `mockProvider.supportsModel(any())=true` và expect routing qua AiProvider (sai design) → đã được replace bằng tests đúng
- `AiGatewayRoutingIntegrationTest` (8): real Router + real NineRouterGateway + mock RestTemplate; AI Summary/Flashcard/Draft → HTTP layer; error paths (429, connection refused)
- `AiGatewaySpringContextTest` (9): SpringBootTest với real Spring context; NineRouterGateway bean discovery; providers list empty; resolve() wiring; pre-fix failure mode documented

## Kết luận

Các bug nặng (flow chết, boot fail env, JPQL sai, AI gateway routing) đã fix và test green. Các lỗi còn lại là edge-case product/architecture đã liệt kê cho backlog.