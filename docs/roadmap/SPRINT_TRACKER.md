# Phase 2 — AI Module Sprint Tracker

> Master progress tracker. Source of Truth: `docs/architecture/AI_MODULE_AUDIT.md`
> Do NOT mark anything completed until verified against Definition of Done in `docs/roadmap/AI_PHASE2_ROADMAP.md`.

---

## Sprint 0 — AI Platform Foundation

**Status**: ██████████ Not Started
**Target**: 2 weeks

### AiGateway — Provider Abstraction

- [ ] `AiGateway` interface (chat, chatStream, embed, isAvailable, getModelInfo)
- [ ] `NineRouterGateway` implementation (HTTP calls to 9router)
- [ ] `OpenAiGateway` (future — interface only)
- [ ] `ClaudeGateway` (future — interface only)
- [ ] `GeminiGateway` (future — interface only)
- [ ] `OllamaGateway` (future — interface only)
- [ ] `AiGatewayRouter` (provider selection, retry, circuit breaker, fallback)
- [ ] Gateway DTOs: `AiChatRequest`, `AiChatResponse`, `AiMessage`, `AiRole`, `AiChunk`, `ModelInfo`

### Prompt Engine

- [ ] `PromptTemplateService` interface
- [ ] `PromptTemplateServiceImpl`
- [ ] `PromptBuilder` (builder pattern, no Spring)
- [ ] `PromptRenderer` (cached rendering)
- [ ] `PromptValidator` (template + variable + output validation)
- [ ] `PromptVariables` (extraction + render + validation utilities)
- [ ] `PromptTesting` (A/B comparison framework)
- [ ] Prompt entities: `PromptTemplate`, `PromptVersion`, `PromptTestResult`

### AI Cache

- [ ] `AiCacheService` (response caching with TTL)
- [ ] `AiCacheEntry` entity + `AiCacheRepository`
- [ ] Caffeine context cache (disease sections, rendered prompts)
- [ ] Cache invalidation on template/disease version change

### AI Usage Logging

- [ ] `AiUsageService` (async, best-effort logging)
- [ ] `AiUsageLog` entity + `AiUsageLogRepository`
- [ ] Usage logging integration in `AiGatewayRouter`

### AI Security

- [ ] `PromptInjectionFilter` (input sanitization)
- [ ] `AiOutputValidator` (content safety + format + length checks)
- [ ] `AiQuotaService` (per-user monthly token budget)
- [ ] `AiRateLimiter` (per-feature per-user rate limiting)

### Shared Infrastructure

- [ ] AI DTOs (gateway + prompt + shared)
- [ ] AI exceptions (8 exception classes + GlobalExceptionHandler integration)
- [ ] AI configuration properties (ai.* yaml block)
- [ ] Enum extensions: `PermissionCode` (+10 AI values)
- [ ] Enum extensions: `AuditAction` (+8 AI values)
- [ ] `SecurityConfig` updates (AI endpoint patterns)

### Database — Sprint 0 Migrations

- [ ] `V2_0__add_user_ai_columns.sql`
- [ ] `V2_6__create_ai_usage_log.sql`
- [ ] `V2_7__create_prompt_template.sql`
- [ ] `V2_8__create_prompt_test_result.sql`
- [ ] `V2_10__create_ai_cache.sql`
- [ ] `V2_11__seed_prompt_templates.sql`

### Tests — Sprint 0

- [ ] `AiGatewayRouterTest`
- [ ] `NineRouterGatewayTest`
- [ ] `PromptTemplateServiceTest`
- [ ] `PromptBuilderTest`
- [ ] `PromptRendererTest`
- [ ] `PromptValidatorTest`
- [ ] `PromptVariablesTest`
- [ ] `PromptTestingTest`
- [ ] `AiQuotaServiceTest`
- [ ] `AiRateLimiterTest`
- [ ] `AiCacheServiceTest`
- [ ] `AiUsageServiceTest`
- [ ] `PromptInjectionFilterTest`
- [ ] `AiOutputValidatorTest`
- [ ] `AiAdminControllerTest` (template CRUD)

### Documentation — Sprint 0

- [ ] `docs/ai/SETUP.md`
- [ ] `docs/ai/GATEWAY.md`
- [ ] `docs/ai/PROMPT_SYSTEM.md`
- [ ] `docs/ai/SECURITY.md`
- [ ] Update `docs/API_CONTRACT.md` (admin endpoints)

---

## Sprint 1 — AI Summary

**Status**: ██████████ Not Started
**Target**: 2 weeks
**Depends on**: Sprint 0

### Pipeline

- [ ] `AiOrchestrator` (cache + quota + rate-limit + logging cross-cutting)
- [ ] `SummaryPipeline` (load → build → call → parse → persist → cache)
- [ ] Summary SSE streaming integration

### Database — Sprint 1

- [ ] `V2_1__create_ai_summary.sql`

### Entity

- [ ] `AiSummary` entity

### DTOs

- [ ] `SummaryRequest`
- [ ] `SummaryDTO`
- [ ] `SummaryType` enum (SHORT, DETAILED, CLINICAL, PATIENT)

### Repository

- [ ] `AiSummaryRepository`

### Controller

- [ ] `AiSummaryController` (4 endpoints)

### Prompt Templates

- [ ] `disease-summary-short` seed
- [ ] `disease-summary-detailed` seed
- [ ] `disease-summary-clinical` seed
- [ ] `disease-summary-patient` seed

### Tests — Sprint 1

- [ ] `SummaryPipelineTest`
- [ ] `SummaryPipelineIntegrationTest`
- [ ] `AiOrchestratorTest`
- [ ] `AiSummaryControllerTest`
- [ ] `PromptTemplateSeedTest` (summary templates)

### Documentation — Sprint 1

- [ ] Update `docs/API_CONTRACT.md` (summary endpoints)
- [ ] `docs/ai/SUMMARY.md`

---

## Sprint 2 — AI Flashcards + AI Quiz

**Status**: ██████████ Not Started
**Target**: 2 weeks
**Depends on**: Sprint 0

### Pipeline

- [ ] `FlashcardPipeline` (batch generation, JSON array parsing)
- [ ] `QuizPipeline` (batch generation, JSON parsing)
- [ ] Batch generation pattern (single call for N items)

### Database — Sprint 2

- [ ] `V2_2__create_flashcard.sql`
- [ ] `V2_3__create_quiz_tables.sql`

### Entities

- [ ] `Flashcard` entity
- [ ] `Quiz` entity + `QuizQuestion` entity

### DTOs

- [ ] `FlashcardGenerateRequest`
- [ ] `FlashcardDTO`
- [ ] `QuizGenerateRequest`
- [ ] `QuizDTO`
- [ ] `QuizQuestionDTO`

### Repositories

- [ ] `FlashcardRepository`
- [ ] `QuizRepository`
- [ ] `QuizQuestionRepository`

### Controllers

- [ ] `AiFlashcardController` (4 endpoints)
- [ ] `AiQuizController` (5 endpoints)

### Prompt Templates

- [ ] `flashcard-gen` seed
- [ ] `quiz-gen` seed

### Cache

- [ ] Flashcard cache (7-day TTL)
- [ ] Quiz cache (7-day TTL)

### Tests — Sprint 2

- [ ] `FlashcardPipelineTest`
- [ ] `QuizPipelineTest`
- [ ] `FlashcardControllerTest`
- [ ] `QuizControllerTest`
- [ ] `FlashcardBatchTest`
- [ ] `QuizSubmitTest`
- [ ] `FlashcardCacheTest`
- [ ] `QuizCacheTest`

### Documentation — Sprint 2

- [ ] Update `docs/API_CONTRACT.md` (flashcard + quiz endpoints)
- [ ] `docs/ai/FLASHCARD.md`
- [ ] `docs/ai/QUIZ.md`

---

## Sprint 3 — AI Case Study + AI Tutor Chat

**Status**: ██████████ Not Started
**Target**: 2 weeks
**Depends on**: Sprint 0

### Pipeline

- [ ] `CaseStudyPipeline` (generation + accuracy validation)
- [ ] `ChatPipeline` (message + history + stream)

### Database — Sprint 3

- [ ] `V2_4__create_chat_tables.sql`
- [ ] `V2_12__add_ai_columns_to_case_study.sql`

### Entities

- [ ] `ChatSession` entity
- [ ] `ChatMessage` entity

### DTOs

- [ ] `ChatSessionCreateRequest`
- [ ] `ChatSessionDTO`
- [ ] `ChatMessageRequest`
- [ ] `ChatMessageDTO`
- [ ] `ChatResponse`
- [ ] `CaseStudyGenerateRequest`

### Repositories

- [ ] `ChatSessionRepository`
- [ ] `ChatMessageRepository`

### Controllers

- [ ] `AiCaseStudyController` (2 endpoints)
- [ ] `AiChatController` (6 endpoints)

### Streaming

- [ ] SSE streaming via `NotificationService.CHANNEL_AI_STREAM`
- [ ] `AiGateway.chatStream()` → `SseEmitter` wiring
- [ ] Chat context trimming (last 20 messages)

### Prompt Templates

- [ ] `casestudy-gen` seed
- [ ] `chat` seed

### Tests — Sprint 3

- [ ] `CaseStudyPipelineTest`
- [ ] `ChatPipelineTest`
- [ ] `ChatStreamingTest`
- [ ] `ChatSessionTest`
- [ ] `ChatMessageTest`
- [ ] `AiChatControllerTest`
- [ ] `AiCaseStudyControllerTest`
- [ ] `ChatPipelineHistoryTest`
- [ ] `CaseStudyAccuracyTest`

### Documentation — Sprint 3

- [ ] Update `docs/API_CONTRACT.md` (case study + chat endpoints)
- [ ] `docs/ai/CASE_STUDY.md`
- [ ] `docs/ai/CHAT.md`
- [ ] Update `docs/FRONTEND_GUIDE.md` (SSE chat)

---

## Sprint 4 — Study Report + Recommendation Engine

**Status**: ██████████ Not Started
**Target**: 2 weeks
**Depends on**: Sprint 0, Sprint 2, Sprint 3 (data)

### Pipeline

- [ ] `StudyReportPipeline` (data aggregation → AI → persist → return)
- [ ] Recommendation Engine (weak topic detection from quiz/flashcard data)
- [ ] Learning Analytics aggregation (quiz scores, flashcard accuracy, chat topics)

### Database — Sprint 4

- [ ] `V2_5__create_study_report.sql`
- [ ] `V2_9__create_ai_feedback.sql`

### Entities

- [ ] `StudyReport` entity
- [ ] `AiFeedback` entity

### DTOs

- [ ] `StudyReportRequest`
- [ ] `StudyReportDTO`
- [ ] `LearningAnalyticsDTO`
- [ ] `UsageStatsDTO`
- [ ] `AiFeedbackRequest`
- [ ] `AiFeedbackDTO`

### Repositories

- [ ] `StudyReportRepository`
- [ ] `AiFeedbackRepository`

### Controllers

- [ ] `AiStudyReportController` (3 endpoints)
- [ ] `AiFeedbackController` (2 endpoints)
- [ ] `AiAdminController` (usage dashboard — 3 endpoints)

### Prompt Templates

- [ ] `study-report` seed

### Tests — Sprint 4

- [ ] `StudyReportPipelineTest`
- [ ] `StudyReportControllerTest`
- [ ] `AiFeedbackServiceTest`
- [ ] `AiFeedbackControllerTest`
- [ ] `LearningAnalyticsTest`
- [ ] `AiAdminUsageControllerTest`
- [ ] `StudyReportRegenerationTest`

### Documentation — Sprint 4

- [ ] Update `docs/API_CONTRACT.md` (report + feedback + admin usage endpoints)
- [ ] `docs/ai/STUDY_REPORT.md`
- [ ] `docs/ai/FEEDBACK.md`

---

## Summary

| Sprint | Theme | Status | Target | Depends On |
|--------|-------|--------|--------|------------|
| Sprint 0 | AI Platform Foundation | ████ Not Started | 2 weeks | None |
| Sprint 1 | AI Summary | ████ Not Started | 2 weeks | Sprint 0 |
| Sprint 2 | AI Flashcards + Quiz | ████ Not Started | 2 weeks | Sprint 0 |
| Sprint 3 | AI Case Study + Chat | ████ Not Started | 2 weeks | Sprint 0 |
| Sprint 4 | Study Report + Feedback | ████ Not Started | 2 weeks | Sprint 0, 2, 3 |

**Total checkpoints**: 120
**Completed**: 0

---

*Tracker document. Do NOT mark items completed until verified against Definition of Done in `docs/roadmap/AI_PHASE2_ROADMAP.md`.*
