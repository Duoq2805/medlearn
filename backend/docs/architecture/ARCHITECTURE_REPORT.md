# ARCHITECTURE_REPORT.md

**Date:** 2026-08-07
**Scope:** Backend `com.duoq.medlearn` — chuẩn hóa kiến trúc Feature-based + N-Layer + hoàn thiện business workflow.
**Baseline:** 340 tests pass, BUILD SUCCESS, `mvn package` OK.

## Đã quyết định (Phase 1 + 7)

1. **knowledge/{version,section,category} — GIỮ NGUYÊN** (không merge vào disease).
   Lý do DDD: `Disease` là Aggregate Root đúng (references versions + currentVersion + category qua FK, các service tách package là application-service boundary hợp lý); `Category`, `SectionType` là shared-reference catalog (được CaseStudy/SymptomChecker dùng) — gộp vào disease sẽ phá bounded context. Quyết định: Phương án B.
2. **`JWT_SECRET` — BẮT BUỘC env, KHÔNG default** (theo yêu cầu "không commit default JWT secret"). Mọi profile (`application.yml/dev/docker/prod`) dùng `${JWT_SECRET}` không fallback → app fail-fast khi thiếu secret. Hướng dẫn sinh secret + `.env.example` + README section.
3. **`EmailServiceImpl` → `common/email/impl/`** (Phase 3 rule: impl không cùng package với interface).

---

## 1. Nguyên tắc đã áp dụng

| Rule | Trạng thái |
|---|---|
| Feature-based + N-Layer (controller/service/repository/entity/dto/enums/mapper) | ✅ sau refactor |
| `service/` = interface, `service/impl/` = implementation | ✅ tất cả features |
| `dto/request/`, `dto/response/`, `dto/projection/`, `dto/enums/`, `dto/internal/` | ✅ request/response/projection đã tách |
| Repository chỉ chứa Spring Data queries (không business) | ✅ (duy nhất `DiseaseRepository` có projection JPQL — đúng chuẩn repo query) |
| Entity chỉ data | ✅ (Disease/DiseaseVersion có helper `isApproved()` etc — giữ nhỏ, chấp nhận) |
| Controller — validate/gọi service/trả response | ✅ |
| AI module tách biệt, không biết business | ✅ (ai/gateway/prompt/config chỉ gọi gateway, không gọi disease service) |

## 2. Cấu trúc cuối (main)

```
com.duoq.medlearn
├── common/
│   ├── config/ entity exception? — config/ exception/ security/ dto/ mapper/ repository/ email/
│   ├── security/  JwtService, JwtAuthenticationFilter, CurrentUserResolver
│   ├── exception/ GlobalExceptionHandler + 10 exception
│   ├── dto/       ApiResponse, ErrorResponse, PagedResponse
│   ├── email/     EmailServiceImpl (interface+impl)
│   └── ModerationService (dead, TODO)
├── auth/           controller/ service(+impl) repository entity dto(request+response) enums mapper security
├── knowledge/
│   ├── disease/    controller service(+impl) repository entity dto(request+response+projection) mapper enums
│   ├── version/    controller service(+impl) repository entity dto(request+response) enums
│   ├── section/    controller service(+impl) repository entity dto(request+response)
│   ├── category/   controller service(+impl) repository entity dto(request+response) mapper
│   └── symptom/    controller service(+impl) repository entity dto(request+response) mapper
├── document/       controller service(+impl) storage repository entity dto(request+response) exception
├── draft/          controller service(+impl) repository entity dto(request+response) enums mapper
├── flashcard/      controller service(+impl) repository entity dto(request+response) enums mapper sm2 util
├── ai/             controller gateway provider cache(+impl) prompt(+impl) summary(+impl) usage(+impl) draft security dto enums model exception repository generation feedback
├── notification/   controller service(+impl) dto
└── audit/          entity enums repository service(+impl)
```

## 3. Thay đổi lớn trong đợt này (so với package-feature cũ)

1. **DTO**: mọi `dto/` flat → `dto/request/`, `dto/response/`, `dto/projection/` (projection cho Disease/CaseStudy summaries dùng trong JPQL).
2. **Service/impl**: tất cả `ServiceImpl` → `service/impl/` (auth 8, disease 3, version 1, section 1, category 1, symptom 4, document 1, draft 2, flashcard 3, ai cache/prompt/summary/usage, audit, notification).
3. **Enums AI**: `AiRole`, `SummaryType` từ `ai/dto/enums/` → `ai/enums/`.
4. **Notification/Audit**: hợp nhất vào subpackage chuẩn (`service/` + `service/impl/` + `controller/`).
5. **Business fix**: `applyToDisease()` hoàn thiện — draft → version.
6. **Env hardening**: JWT default dev, MAIL/GOOGLE optional, `.env.example` tạo.

## 3. API public

**Không thay đổi endpoint/API contract.** Chỉ thay đổi package + internal. Mọi `/api/**` giữ nguyên. Behavior: submit draft từ REJECTED giờ cho phép (bổ sung, không phá).

## 4. Component scan

`MedlearnApplication` giữ root `com.duoq.medlearn` → Spring scan toàn cây package mới, không cần config.

## 5. Xác minh

- ✅ `mvn clean test`: 365/365 (thêm 25 tests AI gateway 2026-08-13)
- ✅ `mvn package`: BUILD SUCCESS
- ✅ git mv giữ lịch sử (renames tracked)

## 6. Điểm tương lai

- `common/ModerationService` (dead) — decide implement hoặc xóa.
- `ai/gateway/provider/AiProvider` (interface không impl) — **GIỮ NGUYÊN** (cơ sở cho multi-provider trực tiếp tương lai). Bug routing đã fix 2026-08-13 — `chat()` dùng `AiGateway` path, không dùng `AiProvider` path. Xem BUG_REPORT.md #12.
- `V2_10__create_ai_cache.sql` (bảng ai_cache) vs `AiCacheServiceImpl` in-memory — chọn 1.
- `DraftSource` / `AiFeedback` / `VersionComparisonResponse` — unused; quyết định xóa.