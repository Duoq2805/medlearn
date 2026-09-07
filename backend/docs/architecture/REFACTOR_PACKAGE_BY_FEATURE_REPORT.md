# Architecture Report — Package by Feature Refactor

**Date:** 2026-08-07
**Scope:** Backend `com.duoq.medlearn` restructure only. No business logic, API, endpoint, database, entity-field, or behavior changes.
**Baseline:** 340 tests passing before; **340 tests passing after**; `mvn clean test` + `mvn package` → BUILD SUCCESS.

---

## 1. Cấu trúc cũ (hybrid layer + feature)

```
com.duoq.medlearn
├── config/          CorsConfig, OpenApiConfig, SecurityConfig
├── controller/      Auth, User, Admin, Disease*, CaseStudy, Category, Symptom, Notification
├── domain/
│   ├── dto/         auth, user, disease, version, section, symptom, category, casestudy, common, notification
│   ├── entity/      User, Role, Permission, Disease, DiseaseVersion, DiseaseSection, Symptom, CaseStudy, SectionType, AuditLog, tokens
│   └── enums/       PermissionCode, UserStatus, VersionStatus, ContentStatus, CaseDifficulty, DiseaseStatus, AuditAction
├── repository/      BaseRepository + per-feature repos
├── service/         interface + impl (Auth, User, AdminUser, Disease*, CaseStudy, Category, Symptom, Role, Permission, Audit, Notification, Email, UserSession)
├── mapper/          MapStructConfig + User/CaseStudy/Disease/Category/Symptom mappers
├── security/        JwtService, JwtAuthenticationFilter, CurrentUserResolver, CustomUserDetails, CustomOAuth2User, OAuth2LoginSuccessHandler
├── config/
├── exception/
├── util/            Sm2Algorithm
├── document/        (feature)
├── draft/           (feature)
├── flashcard/       (feature)
├── symptom/         (feature)
└── ai/              (feature)
```

## 2. Cấu trúc mới (strict package-by-feature)

```
com.duoq.medlearn
├── MedlearnApplication.java        (giữ nguyên root → component scan không cần cấu hình)
├── common/
│   ├── config/          CorsConfig, OpenApiConfig, SecurityConfig
│   ├── exception/       11 exception classes + GlobalExceptionHandler
│   ├── security/        JwtService, JwtAuthenticationFilter, CurrentUserResolver
│   ├── dto/             ApiResponse, ErrorResponse, PagedResponse
│   ├── mapper/          MapStructConfig
│   ├── repository/      BaseRepository
│   └── email/           EmailService, EmailServiceImpl  (+ ModerationService — rỗng, TODO)
├── auth/
│   ├── controller/      AuthController, UserController, AdminController
│   ├── service/         AuthService, UserService, RoleService, PermissionService, UserSessionService, AdminUserService (+ impl)
│   ├── repository/      UserRepository, RoleRepository, UserSessionRepository, VerificationTokenRepository, PasswordResetTokenRepository
│   ├── entity/          User, Role, Permission, UserSession, VerificationToken, PasswordResetToken
│   ├── dto/             auth + user dtos (merged)
│   ├── enums/           UserStatus, PermissionCode
│   ├── mapper/          UserMapper
│   └── security/        CustomUserDetails, CustomOAuth2User, OAuth2LoginSuccessHandler, Custom*ServiceImpl
├── knowledge/
│   ├── disease/         entity: Disease, DiseaseSection, CaseStudy · enums: DiseaseStatus, ContentStatus, CaseDifficulty · repository · service · controller · dto · mapper
│   ├── version/         entity: DiseaseVersion, DiseaseVersionSymptom · enums: VersionStatus · repository · service · controller · dto
│   ├── section/         entity: SectionType · repository · service · controller · dto
│   ├── category/        entity · repository · service · controller · dto · mapper
│   ├── symptom/         entity: Symptom · repository · service · controller · dto · mapper · ServiceV1/V2 checker
├── document/            controller · service (+impl flatten) · repository · entity · storage (LocalFileStorageService) · dto · exception
├── draft/               controller · service · repository · entity · dto · enums · mapper
├── flashcard/          controller · service · repository · entity · dto · enums · mapper · sm2 (Sm2Algorithm) · util
├── ai/                 controller · gateway · provider · cache · prompt · summary · draft · security · usage · dto · config · exception · enums · model · repository · generation · feedback
├── notification/       NotificationService(+Impl), NotificationController, dto/ (NotificationEventResponse)
└── audit/              entity, enums, repository, service
```

## 3. Mapping package chính

| Cũ | Mới |
|---|---|
| `domain/entity/User,Role,Permission,UserSession,VerificationToken,PasswordResetToken` | `auth/entity/` |
| `domain/enums/UserStatus,PermissionCode` | `auth/enums/` |
| `domain/dto/auth/*`, `domain/dto/user/*` | `auth/dto/` (merge) |
| `repository/UserRepository,RoleRepository,UserSessionRepository,VerificationTokenRepository,PasswordResetTokenRepository` | `auth/repository/` |
| `service/AuthService,UserService,... + impl` | `auth/service/` |
| `controller/AuthController,UserController,AdminController` | `auth/controller/` |
| `mapper/UserMapper` | `auth/mapper/` |
| `security/CustomUserDetails, CustomOAuth2User, OAuth2LoginSuccessHandler` | `auth/security/` |
| `domain/entity/Disease,DiseaseSection,CaseStudy` | `knowledge/disease/entity/` |
| `domain/enums/DiseaseStatus,ContentStatus,CaseDifficulty` | `knowledge/disease/enums/` |
| `repository/DiseaseRepository,CaseStudyRepository` + services/controllers/mappers/dtos | `knowledge/disease/` |
| `domain/entity/DiseaseVersion,DiseaseVersionSymptom` | `knowledge/version/entity/` |
| `domain/enums/VersionStatus` | `knowledge/version/enums/` |
| `domain/entity/DiseaseSection entity` + repo/service/controller | `knowledge/section/` |
| `domain/entity/SectionType` | `knowledge/section/entity/` |
| `domain/entity/Symptom` + SymptomRepository/Service/Controller | `knowledge/symptom/` |
| `domain/entity/Category` + CategoryRepository/Service/Controller | `knowledge/category/` |
| `symptom/*` (SymptomChecker V0/V1/V2) | `knowledge/symptom/` |
| `document/service/LocalFileStorageService` | `document/storage/` |
| `util/Sm2Algorithm` | `flashcard/sm2/` |
| `ai/gateway/AiCacheService(+Impl)` | `ai/cache/` |
| `service/NotificationService(+Impl)`, `controller/NotificationController`, `domain/dto/notification/*` | `notification/` |
| `domain/entity/AuditLog`, `domain/enums/AuditAction`, `repository/AuditLogRepository`, `service/AuditService(+Impl)` | `audit/` |
| `config/*`, `security/Jwt*`, `exception/*`, `domain/dto/common/*`, `mapper/MapStructConfig`, `repository/BaseRepository`, `service/EmailService(+Impl)`, `service/ModerationService` | `common/` |

## 4. Điểm cần cải thiện tương lai (không làm trong scope này)

- **Dead code giữ nguyên (đánh dấu TODO theo yêu cầu):** `ModerationService` (interface rỗng), `AiProvider` (interface không impl), `DraftSource`/`DraftProvenance` (entity+repo không được ghi trong AiDraftGeneratorImpl), `AiFeedback` (entity+repo, chưa service), `AiCacheServiceImpl` in-memory (khác bảng DB `ai_cache`), `NineRouterGateway.chatStream` (fake streaming), `privcate` `applyToDisease` (chỉ log.warn).
- **Tiếp tục clean:** `ai/repository/` vs `ai/{summary,usage,generation,feedback}/repository` — có dir `ai/repository` còn dư (AiSummaryRepository, AiUsageLogRepository, PromptTemplateRepository, PromptTestResultRepository) → nên chuyển sang `ai/<feature>/repository` cho nhất quán. (Không làm vì giữ ổn định, để TODO.)
- `ai/gateway/provider/AiProvider` dead code — cần hoặc triển khai impl, hoặc xoá.
- `document_chunk.embedding vector(1536)` có trong migration nhưng entity không map — RAG chưa implement (như audit trước đó).
- Javadoc tiếng Việt mojibake trong `DiseaseServiceImpl` (đã có).

## 5. Xác minh

- Rất nhiều `git mv` giữ lịch sử (183 renames tracked), không file bị xoá ngoài mục đích.
- `MedlearnApplication` giữ root → @SpringBootApplication scan từ `com.duoq.medlearn` bao phủ tất cả package mới → component scan không đổi.
- API/endpoint/DB/behavior không đổi — chỉ package + imports.
- ✅ `mvn clean test`: 340/340 pass
- ✅ `mvn package -DskipTests`: BUILD SUCCESS