# Phase 2 — AI Module Implementation Roadmap

> Produced: 2026-07-04
> Source of Truth: `docs/architecture/AI_MODULE_AUDIT.md`
> This document is the OFFICIAL implementation guide. No deviations from the approved architecture.

---

## Overview

Phase 2 converts the approved AI architecture into 5 implementation sprints. Each sprint produces production-ready, tested, documented deliverables that build on the previous sprint.

**Total estimated effort**: 8–10 weeks (2 weeks per sprint)
**Architecture authority**: `AI_MODULE_AUDIT.md` sections 1–7. Every file, class, table, and endpoint in this roadmap is defined there.

---

## Sprint 0 — AI Platform Foundation

**Duration**: 2 weeks
**Complexity**: High (infrastructure, many new abstractions)
**Dependencies**: None (greenfield AI module)

### Goal

Build the reusable AI infrastructure that every subsequent sprint depends on. No feature delivers user-visible value yet — this is the platform layer.

### Objectives

1. Implement the AI Gateway abstraction (`AiGateway` interface + `NineRouterGateway`)
2. Implement `AiGatewayRouter` with retry, circuit breaker, fallback chain
3. Implement the Prompt Engine (`PromptTemplateService`, `PromptBuilder`, `PromptRenderer`, `PromptValidator`, `PromptVariables`)
4. Implement AI Cache (`AiCacheService`, `ai_cache` table, Caffeine context cache)
5. Implement AI Usage Logging (`AiUsageService`, `ai_usage_log` table, async logging)
6. Implement shared AI DTOs, exceptions, configuration
7. Implement shared AI test infrastructure (test factories, mock providers)

### Deliverables

#### Database Changes

**Flyway migrations** (from `AI_MODULE_AUDIT.md §3.3`):

| Migration | Table | Purpose |
|-----------|-------|---------|
| `V2_0__add_user_ai_columns.sql` | `users` | Add `monthly_token_quota`, `tokens_used_this_month`, `quota_reset_at`, `ai_enabled` |
| `V2_6__create_ai_usage_log.sql` | `ai_usage_log` | Per-request usage tracking |
| `V2_7__create_prompt_template.sql` | `prompt_template` | Versioned prompt templates |
| `V2_8__create_prompt_test_result.sql` | `prompt_test_result` | A/B test results |
| `V2_10__create_ai_cache.sql` | `ai_cache` | Response caching |
| `V2_11__seed_prompt_templates.sql` | — | Seed initial prompt templates |

#### Entities (from `AI_MODULE_AUDIT.md §2.2`, `§3.2`)

| Entity | Table | Key Fields |
|--------|-------|------------|
| `PromptTemplate` | `prompt_template` | `name`, `content`, `system_prompt`, `variables` (JSONB), `version`, `active`, `feature`, `temperature`, `max_tokens` |
| `PromptVersion` | `prompt_template` (versioned) | `template_id`, `version`, `content`, `variables`, `changelog`, `created_by` |
| `PromptTestResult` | `prompt_test_result` | `template_id`, `version`, `test_input`, `test_output`, `tokens_input`, `tokens_output`, `latency_ms`, `human_rating` |
| `AiUsageLog` | `ai_usage_log` | `user_id`, `feature`, `provider`, `model`, `tokens_input`, `tokens_output`, `cost`, `latency_ms`, `success` |
| `AiCacheEntry` | `ai_cache` | `cache_key`, `feature`, `response_text`, `model`, `expires_at` |

**User entity changes** (from `AI_MODULE_AUDIT.md §6.4`):
- Add `monthly_token_quota BIGINT DEFAULT 1000000`
- Add `tokens_used_this_month BIGINT DEFAULT 0`
- Add `quota_reset_at TIMESTAMPTZ`
- Add `ai_enabled BOOLEAN DEFAULT true`

#### Package Structure (from `AI_MODULE_AUDIT.md §2.3`)

```
com.duoq.medlearn.ai
├── gateway/
│   ├── AiGateway.java                    (interface)
│   ├── NineRouterGateway.java            (impl — primary target)
│   ├── AiGatewayRouter.java              (routes to active gateway)
│   └── dto/
│       ├── AiChatRequest.java
│       ├── AiChatResponse.java
│       ├── AiMessage.java
│       ├── AiRole.java
│       ├── AiChunk.java
│       └── ModelInfo.java
├── prompt/
│   ├── PromptTemplateService.java        (interface)
│   ├── PromptTemplateServiceImpl.java    (impl)
│   ├── PromptBuilder.java                (builder, no Spring)
│   ├── PromptRenderer.java               (Spring @Service)
│   ├── PromptValidator.java              (utility)
│   ├── PromptVariables.java              (utility)
│   ├── PromptTesting.java                (Spring @Service)
│   └── entity/
│       ├── PromptTemplate.java
│       ├── PromptVersion.java
│       └── PromptTestResult.java
├── security/
│   ├── PromptInjectionFilter.java        (input sanitization)
│   ├── AiOutputValidator.java            (output safety check)
│   ├── AiQuotaService.java               (per-user quota enforcement)
│   └── AiRateLimiter.java                (per-user per-feature rate limit)
├── usage/
│   ├── AiUsageService.java
│   ├── AiCacheService.java
│   └── entity/
│       ├── AiUsageLog.java
│       └── AiCacheEntry.java
└── controller/
    └── AiAdminController.java            (template management, usage stats)
```

#### Services (from `AI_MODULE_AUDIT.md`)

| Service Interface | Impl | Responsibility | Source § |
|-----------------|------|---------------|----------|
| `PromptTemplateService` | `PromptTemplateServiceImpl` | Template CRUD, versioning, active version resolution | §2.1 |
| — | `PromptBuilder` | Build `AiChatRequest` from template + variables + context | §2.2 |
| — | `PromptRenderer` | Render template with variable substitution (cached) | §2.2 |
| — | `PromptValidator` | Validate templates, variable sets, rendered output | §2.2 |
| — | `PromptVariables` | Extract/validate `{{variable}}` placeholders | §2.2 |
| — | `PromptTesting` | A/B test template versions | §2.2 |
| — | `AiGatewayRouter` | Route requests, retry, circuit-break, fallback | §1.4 |
| — | `NineRouterGateway` | HTTP calls to 9router API | §1.2 |
| — | `AiQuotaService` | Per-user monthly token budget enforcement | §6.4 |
| — | `AiRateLimiter` | Per-feature per-user rate limiting | §6.5 |
| — | `AiUsageService` | Async usage logging to `ai_usage_log` | §6.6 |
| — | `AiCacheService` | Response caching with TTL | §5.1 |

#### Repositories (from `AI_MODULE_AUDIT.md §2.3`)

| Repository | Entity |
|-----------|--------|
| `PromptTemplateRepository` | `PromptTemplate` |
| `PromptVersionRepository` | `PromptVersion` |
| `PromptTestResultRepository` | `PromptTestResult` |
| `AiUsageLogRepository` | `AiUsageLog` |
| `AiCacheRepository` | `AiCacheEntry` |

#### DTOs (from `AI_MODULE_AUDIT.md §5.2`)

| DTO | Fields | Used By |
|-----|--------|---------|
| `AiChatRequest` | `model`, `messages` (List\<AiMessage\>), `temperature`, `maxTokens`, `stream`, `userId` | `AiGateway` |
| `AiChatResponse` | `content`, `model`, `tokensInput`, `tokensOutput`, `finishReason` | `AiGateway` |
| `AiMessage` | `role` (AiRole), `content` | `AiChatRequest` |
| `AiRole` | `SYSTEM`, `USER`, `ASSISTANT` (enum) | `AiMessage` |
| `AiChunk` | `content`, `done` | Streaming |
| `ModelInfo` | `name`, `contextWindow`, `supportsStreaming`, `supportsEmbedding`, `costPer1kInputTokens`, `costPer1kOutputTokens` | Admin/status |
| `PromptTemplateDTO` | Template fields (no content in list views) | Admin |
| `PromptTestResultDTO` | Test result fields | Admin |
| `VersionComparisonDTO` | Side-by-side version comparison | Admin |

#### Shared Exceptions (from `AI_MODULE_AUDIT.md §5.5`)

| Exception | Trigger | HTTP Status |
|-----------|---------|-------------|
| `AiAuthenticationException` | Provider 401 | 502 |
| `AiRateLimitException` | Provider 429 | 429 |
| `AiProviderException` | Provider 500 | 502 |
| `AiTimeoutException` | Request timeout | 504 |
| `AiContextLengthException` | Context window exceeded | 400 |
| `AiModelException` | Deprecated/missing model | 502 |
| `AiSecurityException` | Injection detected | 400 |
| `AiQuotaExceededException` | Monthly quota exceeded | 429 |

#### Shared Configuration (from `AI_MODULE_AUDIT.md §7.7`)

```yaml
ai:
  enabled: true
  default-provider: nine-router
  providers:
    nine-router:
      base-url: ${NINEROUTER_BASE_URL:https://api.9router.com}
      api-key: ${NINEROUTER_API_KEY}
      default-model: ${NINEROUTER_DEFAULT_MODEL:openai/gpt-4o}
      timeout: ${AI_TIMEOUT:30000}
      max-retries: 3
      retry-backoff-ms: 1000
  cache:
    enabled: true
    storage: caffeine
    response-ttl:
      summary: 86400
      flashcard: 604800
      quiz: 604800
      casestudy: 604800
  quota:
    default-monthly-tokens: 1000000
    enabled: true
  rate-limit:
    enabled: true
    backend: memory
  features:
    summary:   enabled=true
    flashcard: enabled=true
    quiz:      enabled=true
    casestudy: enabled=true
    chat:      enabled=true
    report:    enabled=true
```

#### Environment Variables (from `AI_MODULE_AUDIT.md §7.7`)

```properties
NINEROUTER_BASE_URL=https://api.9router.com
NINEROUTER_API_KEY=sk-your-key-here
NINEROUTER_DEFAULT_MODEL=openai/gpt-4o
AI_TIMEOUT=30000
AI_CACHE_ENABLED=true
AI_QUOTA_DEFAULT=1000000
```

#### Enum Extensions (from `AI_MODULE_AUDIT.md §6.6, §6.8`)

**`PermissionCode` — new values:**
- `AI_USE` — Access AI features at all
- `AI_SUMMARY` — Use summary generation
- `AI_FLASHCARD` — Use flashcard generation
- `AI_QUIZ` — Use quiz generation
- `AI_CASE` — Use case study generation
- `AI_CHAT` — Use chat
- `AI_REPORT` — Use study report
- `AI_MANAGE` — Manage prompt templates
- `AI_VIEW_USAGE` — View usage stats
- `AI_ADMIN` — Administer quotas, view all logs

**`AuditAction` — new values:**
- `AI_REQUEST`
- `AI_RESPONSE`
- `AI_QUOTA_EXCEEDED`
- `AI_INJECTION_DETECTED`
- `AI_RATE_LIMITED`
- `AI_CACHE_HIT`
- `AI_PROVIDER_FAILOVER`
- `AI_OUTPUT_REJECTED`

#### Tests

| Test Suite | What It Covers |
|-----------|---------------|
| `AiGatewayRouterTest` | Provider selection, fallback chain, circuit breaker open/close/half-open |
| `NineRouterGatewayTest` | HTTP call construction, response parsing, error mapping, streaming |
| `PromptTemplateServiceTest` | CRUD, version activation, active template resolution |
| `PromptBuilderTest` | Template rendering, variable substitution, context injection, history injection |
| `PromptRendererTest` | Cached vs fresh rendering, TTL invalidation |
| `PromptValidatorTest` | Required variables, extra variables, output format validation |
| `PromptVariablesTest` | Extraction regex, render edge cases (nested braces, empty vars) |
| `PromptTestingTest` | A/B comparison, test result persistence |
| `AiQuotaServiceTest` | Quota check accept/reject, monthly reset, token estimation |
| `AiRateLimiterTest` | Token bucket fill/consume, burst handling, 429 response |
| `AiCacheServiceTest` | Cache hit/miss, TTL expiry, invalidation on template change |
| `AiUsageServiceTest` | Async logging, best-effort failure handling |
| `PromptInjectionFilterTest` | Known injection patterns, false positive rate |
| `AiOutputValidatorTest` | Content safety, format compliance, length bounds |
| `AiAdminControllerTest` | Template CRUD endpoints, usage stats endpoints |

#### Documentation

| Document | Content |
|----------|---------|
| `docs/ai/SETUP.md` | Environment setup, 9router key configuration, provider config |
| `docs/ai/GATEWAY.md` | Gateway architecture, provider addition guide, retry/fallback config |
| `docs/ai/PROMPT_SYSTEM.md` | Template format, variable conventions, A/B testing workflow |
| `docs/ai/SECURITY.md` | Injection protection, output validation, quota/rate-limit config |
| `docs/architecture/AI_MODULE_AUDIT.md` | (existing) Updated with Sprint 0 implementation details |
| Update `docs/API_CONTRACT.md` | Add admin endpoints for template management, usage stats |

### Risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| 9router API incompatible with OpenAI spec | Gateway fails for all features | Test against 9router sandbox first. Keep `AiGateway` interface — swap to direct OpenAI if 9router fails |
| Prompt injection bypasses filter | Malicious content reaches LLM | Defense in depth: filter + system prompt reinforcement + output validation. Start with conservative filter rules, relax later |
| Quota system incorrectly estimates tokens | Users blocked prematurely or quota overrun | Estimate conservatively (4 chars = 1 token). Actual tokens from response correct the counter. Over-estimation is safer |
| Async usage logging drops records | Lost cost data | Best-effort pattern (same as `AuditServiceImpl`). Log failure is non-blocking. Acceptable for Phase 2 — add reliable queue in Phase 4 |

### Dependencies

- **External**: 9router API key, active 9router account
- **Internal**: Existing Spring Boot infrastructure, PostgreSQL, Flyway
- **None**: Sprint 0 has zero dependency on any other sprint

### Acceptance Criteria

1. `AiGatewayRouter` routes requests to `NineRouterGateway` and returns valid `AiResponse`
2. `NineRouterGateway` makes HTTP POST to 9router and parses response correctly
3. Streaming returns `Flux<AiChunk>` with correct SSE format
4. `PromptBuilder` builds `AiChatRequest` from template + variables + context
5. `PromptRenderer` caches rendered prompts and invalidates on template change
6. `PromptValidator` detects missing/extra variables
7. `AiQuotaService` rejects requests when monthly budget exceeded
8. `AiRateLimiter` returns 429 when per-feature rate limit exceeded
9. `AiCacheService` returns cached response for identical request within TTL
10. `AiUsageService` writes to `ai_usage_log` asynchronously without blocking caller
11. All enum extensions compile and deploy cleanly
12. Admin controller allows template CRUD via API
13. All Flyway migrations apply cleanly (up and down)
14. Test coverage > 80% for all new code

### Definition of Done

- [ ] All Flyway migrations applied and reversible
- [ ] All entities created with correct JPA mappings
- [ ] All repositories defined with required queries
- [ ] All service interfaces and implementations compile
- [ ] All DTOs defined with validation annotations
- [ ] All exceptions defined with HTTP status mapping
- [ ] `GlobalExceptionHandler` updated with AI exceptions
- [ ] `SecurityConfig` updated with AI endpoint patterns
- [ ] Configuration properties loaded and injectable
- [ ] Environment variables documented in `.env.example`
- [ ] Test coverage ≥ 80%
- [ ] Integration test proves end-to-end: request → router → NineRouterGateway → response
- [ ] LSP diagnostics clean on all new files
- [ ] Existing tests still pass
- [ ] Documentation files written

---

## Sprint 1 — AI Summary

**Duration**: 2 weeks
**Complexity**: Medium
**Dependencies**: Sprint 0 (AiGateway, Prompt Engine, Cache, Usage Logging)

### Goal

Generate structured disease summaries from approved disease content, cache and persist them, and expose via REST API.

### Objectives

1. Implement the Summary Pipeline (`SummaryPipeline`)
2. Implement `AiOrchestrator` (pipeline coordinator with cache + quota + rate-limit + logging cross-cutting)
3. Implement Summary persistence (`ai_summary` table, `AiSummary` entity)
4. Implement Summary REST API endpoints
5. Seed summary prompt templates

### Deliverables

#### Database Changes

**Flyway migrations** (from `AI_MODULE_AUDIT.md §3.3`):

| Migration | Table | Purpose |
|-----------|-------|---------|
| `V2_1__create_ai_summary.sql` | `ai_summary` | Store generated summaries |

**`ai_summary` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
disease_id        BIGINT NOT NULL FK → disease.id
disease_version_id BIGINT FK → disease_version.id (nullable)
summary_type      VARCHAR(50) NOT NULL   -- SHORT, DETAILED, CLINICAL, PATIENT
content           TEXT NOT NULL
model_used        VARCHAR(100)
prompt_version    VARCHAR(50)
tokens_input      INT DEFAULT 0
tokens_output     INT DEFAULT 0
is_cached         BOOLEAN DEFAULT false
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

#### Entity

| Entity | Table | Key Fields |
|--------|-------|------------|
| `AiSummary` | `ai_summary` | `disease_id`, `disease_version_id`, `summary_type`, `content`, `model_used`, `prompt_version`, `tokens_input`, `tokens_output` |

#### DTOs

| DTO | Fields | Notes |
|-----|--------|-------|
| `SummaryRequest` | `diseaseId: Long`, `summaryType: SummaryType`, `stream: boolean` | Input |
| `SummaryDTO` | `id`, `diseaseId`, `summaryType`, `content`, `modelUsed`, `createdAt` | Response |
| `SummaryType` | `SHORT`, `DETAILED`, `CLINICAL`, `PATIENT` (enum) | From §4.2 |

#### Repository

| Repository | Key Queries |
|-----------|-------------|
| `AiSummaryRepository` | `findByDiseaseIdAndSummaryType(diseaseId, type)`, `findLatestByDiseaseId(diseaseId)` |

#### Service

| Component | Responsibility | Source § |
|-----------|---------------|----------|
| `SummaryPipeline` | Full pipeline: load disease + sections → build prompt → call AiGateway → parse → persist → cache | §4.2 |
| `AiOrchestrator` | Cross-cutting: quota check → rate limit → cache check → execute pipeline → log → cache → return | §4.1 |

#### Controller

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `POST /api/ai/summary` | `AiSummaryController` | Generate summary (non-streaming) |
| `POST /api/ai/summary?stream=true` | `AiSummaryController` | Generate summary (SSE streaming) |
| `GET /api/ai/summary/{diseaseId}?type=SHORT` | `AiSummaryController` | Get cached summary |
| `POST /api/ai/summary/{diseaseId}/regenerate` | `AiSummaryController` | Force regeneration (bypass cache) |

#### Prompt Templates (seed data)

| Template Name | Feature | Variables | Source § |
|---------------|---------|-----------|----------|
| `disease-summary-short` | `summary` | `diseaseName`, `maxWords` | §4.2 |
| `disease-summary-detailed` | `summary` | `diseaseName`, `maxWords` | §4.2 |
| `disease-summary-clinical` | `summary` | `diseaseName`, `sections`, `maxWords` | §4.2 |
| `disease-summary-patient` | `summary` | `diseaseName`, `sections`, `maxWords` | §4.2 |

Each seeded with `system_prompt` (medical education context), `temperature`, `max_tokens`, and `content` with `{{variable}}` placeholders as defined in §4.2.

#### Tests

| Test Suite | What It Covers |
|-----------|---------------|
| `SummaryPipelineTest` | Full pipeline: disease load → prompt build → AiGateway call → response parse → persist (mocked gateway) |
| `SummaryPipelineIntegrationTest` | End-to-end with `TestNineRouterGateway` (stubbed) |
| `AiOrchestratorTest` | Cache hit short-circuits pipeline, quota reject throws, rate limit reject throws |
| `AiSummaryControllerTest` | All endpoints: generate, get cached, regenerate, streaming, validation, auth |
| `PromptTemplateSeedTest` | All 4 summary templates valid (variables match expected) |

#### Documentation

| Document | Content |
|----------|---------|
| Update `docs/API_CONTRACT.md` | Add summary endpoints |
| `docs/ai/SUMMARY.md` | Summary pipeline architecture, prompt design, regeneration rules |

### Risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| AI hallucinates incorrect medical content | Misinformation in summaries | `AiOutputValidator` checks disease name match. Use `CLINICAL` mode for higher-quality model |
| Long disease sections exceed context window | Pipeline fails | Context trimming: truncate least-important sections (lower `sort_order`). Log truncation events |
| Summary cache returns stale content | User sees outdated info | Invalidate cache on disease version approval. Cache key includes `disease_version_id` |

### Dependencies

- **Sprint 0**: `AiGateway`, `AiGatewayRouter`, `NineRouterGateway`, `PromptBuilder`, `PromptRenderer`, `PromptValidator`, `AiCacheService`, `AiUsageService`, `AiQuotaService`, `AiRateLimiter`, `PromptInjectionFilter`, `AiOutputValidator`

### Acceptance Criteria

1. `POST /api/ai/summary` returns `SummaryDTO` with generated content
2. `POST /api/ai/summary?stream=true` returns SSE stream of `AiChunk`
3. Same request twice returns cached response (second call skips AiGateway)
4. `POST /api/ai/summary/{diseaseId}/regenerate` bypasses cache
5. Summary content passes `AiOutputValidator` checks
6. Summary is persisted in `ai_summary` table
7. Usage logged in `ai_usage_log` for every generate call
8. Quota-exceeded user receives 429
9. Rate-limited user receives 429
10. Unauthorized user (no `AI_SUMMARY` permission) receives 403
11. Invalid `diseaseId` returns 404
12. All 4 summary types produce different content lengths

### Definition of Done

- [ ] Flyway migration `V2_1` applied and reversible
- [ ] `AiSummary` entity with correct JPA mappings
- [ ] `AiSummaryRepository` with required queries
- [ ] `SummaryPipeline` wired and testable
- [ ] `AiOrchestrator` with cache/quota/rate-limit/logging cross-cutting
- [ ] `AiSummaryController` with all 4 endpoints
- [ ] 4 summary prompt templates seeded
- [ ] Tests cover all acceptance criteria
- [ ] Integration test proves end-to-end flow
- [ ] `SecurityConfig` has summary endpoint patterns
- [ ] LSP diagnostics clean
- [ ] Existing tests still pass
- [ ] Documentation written

---

## Sprint 2 — AI Flashcards + AI Quiz

**Duration**: 2 weeks
**Complexity**: Medium
**Dependencies**: Sprint 0 (AiGateway, Prompt Engine, Cache, Usage Logging)

### Goal

Generate structured learning material (flashcards and quizzes) from disease content with difficulty levels, batch generation, and regeneration support.

### Objectives

1. Implement Flashcard Pipeline (`FlashcardPipeline`)
2. Implement Quiz Pipeline (`QuizPipeline`)
3. Implement Flashcard persistence + API
4. Implement Quiz persistence + API
5. Implement batch generation (single call for N items)
6. Seed flashcard and quiz prompt templates

### Deliverables

#### Database Changes

**Flyway migrations** (from `AI_MODULE_AUDIT.md §3.3`):

| Migration | Table | Purpose |
|-----------|-------|---------|
| `V2_2__create_flashcard.sql` | `flashcard` | Generated flashcards |
| `V2_3__create_quiz_tables.sql` | `quiz`, `quiz_question` | Generated quizzes and questions |

**`flashcard` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
disease_id        BIGINT NOT NULL FK → disease.id
disease_version_id BIGINT FK → disease_version.id
question          TEXT NOT NULL
answer            TEXT NOT NULL
difficulty        VARCHAR(20) NOT NULL   -- EASY, MEDIUM, HARD
tags              JSONB DEFAULT '[]'
ai_generated      BOOLEAN DEFAULT false
prompt_version    VARCHAR(50)
times_reviewed    INT DEFAULT 0
times_correct     INT DEFAULT 0
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**`quiz` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
title             VARCHAR(255) NOT NULL
description       TEXT
disease_id        BIGINT NOT NULL FK → disease.id
difficulty        VARCHAR(20) NOT NULL
ai_generated      BOOLEAN DEFAULT false
prompt_version    VARCHAR(50)
question_count    INT NOT NULL
pass_score        INT DEFAULT 70
time_limit_minutes INT (nullable)
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**`quiz_question` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
quiz_id           BIGINT NOT NULL FK → quiz.id ON DELETE CASCADE
question          TEXT NOT NULL
options           JSONB NOT NULL
correct_answer    VARCHAR(10) NOT NULL
explanation       TEXT
order_index       INT NOT NULL
question_type     VARCHAR(20) DEFAULT 'MULTIPLE_CHOICE'
created_at        TIMESTAMPTZ DEFAULT NOW()
```

#### Entities

| Entity | Table | Notes |
|--------|-------|-------|
| `Flashcard` | `flashcard` | `Disease disease` (ManyToOne), `promptVersion`, `difficulty` enum |
| `Quiz` | `quiz` | `Disease disease` (ManyToOne), `List<QuizQuestion> questions` (OneToMany cascade) |
| `QuizQuestion` | `quiz_question` | `Quiz quiz` (ManyToOne), `options` JSONB mapped to `List<QuizOption>` |

#### DTOs

| DTO | Fields | Notes |
|-----|--------|-------|
| `FlashcardGenerateRequest` | `diseaseId`, `count` (default 10), `difficulty` | Input |
| `FlashcardDTO` | `id`, `question`, `answer`, `difficulty`, `tags`, `diseaseId` | Response |
| `QuizGenerateRequest` | `diseaseId`, `questionCount` (default 5, max 20), `difficulty` | Input |
| `QuizDTO` | `id`, `title`, `diseaseId`, `difficulty`, `questions` (List\<QuizQuestionDTO\>) | Response |
| `QuizQuestionDTO` | `question`, `options`, `correctAnswer`, `explanation`, `orderIndex` | Nested |

#### Repositories

| Repository | Key Queries |
|-----------|-------------|
| `FlashcardRepository` | `findByDiseaseId(diseaseId)`, `findByDiseaseIdAndDifficulty(diseaseId, difficulty)` |
| `QuizRepository` | `findByDiseaseId(diseaseId)`, `findByDiseaseIdAndDifficulty(diseaseId, difficulty)` |
| `QuizQuestionRepository` | `findByQuizIdOrderByOrderIndex(quizId)` |

#### Services

| Component | Responsibility | Source § |
|-----------|---------------|----------|
| `FlashcardPipeline` | Load disease → build prompt (batch N flashcards) → call AiGateway → parse JSON array → persist all | §4.2 |
| `QuizPipeline` | Load disease → build prompt (batch N questions) → call AiGateway → parse JSON → persist quiz + questions | §4.2 |

**Batch generation pattern** (from `AI_MODULE_AUDIT.md §5.5):

Single prompt requests N items in one call:
> "Generate 10 flashcards about {{diseaseName}} at {{difficulty}} difficulty.
> Return as a JSON array with each flashcard having 'question', 'answer', and 'difficulty' fields."

#### Controllers

| Endpoint | Controller | Purpose |
|----------|-----------|---------|
| `POST /api/ai/flashcards/generate` | `AiFlashcardController` | Generate flashcards |
| `GET /api/ai/flashcards?diseaseId=&difficulty=` | `AiFlashcardController` | List flashcards |
| `GET /api/ai/flashcards/{id}` | `AiFlashcardController` | Get single flashcard |
| `POST /api/ai/flashcards/{diseaseId}/regenerate` | `AiFlashcardController` | Regenerate all for disease |
| `POST /api/ai/quizzes/generate` | `AiQuizController` | Generate quiz |
| `GET /api/ai/quizzes?diseaseId=&difficulty=` | `AiQuizController` | List quizzes |
| `GET /api/ai/quizzes/{id}` | `AiQuizController` | Get quiz with questions |
| `POST /api/ai/quizzes/{diseaseId}/regenerate` | `AiQuizController` | Regenerate quiz |
| `POST /api/ai/quizzes/{quizId}/submit` | `AiQuizController` | Submit answers (grading) |

#### Prompt Templates (seed)

| Template Name | Feature | Variables | Format |
|---------------|---------|-----------|--------|
| `flashcard-gen` | `flashcard` | `diseaseName`, `sections`, `symptoms`, `count`, `difficulty` | JSON array |
| `quiz-gen` | `quiz` | `diseaseName`, `sections`, `symptoms`, `questionCount`, `difficulty` | JSON object |

#### Cache Configuration

| Cache | Key | TTL | Source § |
|-------|-----|-----|----------|
| Flashcard response | `flashcard:{diseaseId}:{difficulty}:{count}` | 7 days | §5.1 |
| Quiz response | `quiz:{diseaseId}:{difficulty}:{questionCount}` | 7 days | §5.1 |

#### Tests

| Test Suite | What It Covers |
|-----------|---------------|
| `FlashcardPipelineTest` | Generation, JSON array parsing, batch count accuracy, difficulty levels |
| `QuizPipelineTest` | Generation, JSON parsing, question count, option validation, correctAnswer validation |
| `FlashcardControllerTest` | All 4 endpoints: generate, list, get, regenerate |
| `QuizControllerTest` | All 5 endpoints: generate, list, get, regenerate, submit |
| `FlashcardBatchTest` | Single call generates exactly N flashcards |
| `QuizSubmitTest` | Grading logic (score calculation, per-question feedback) |
| `FlashcardCacheTest` | Same request within TTL returns cached (skips AiGateway) |
| `QuizCacheTest` | Same request within TTL returns cached |

#### Documentation

| Document | Content |
|----------|---------|
| Update `docs/API_CONTRACT.md` | Add flashcard + quiz endpoints |
| `docs/ai/FLASHCARD.md` | Generation pipeline, prompt design, regeneration rules |
| `docs/ai/QUIZ.md` | Generation pipeline, grading logic, difficulty levels |

### Risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| JSON parsing fails (AI returns malformed JSON) | Generation fails | `AiOutputValidator` checks JSON validity. Retry once with stricter prompt. If still fails, return error |
| Distractor quality too easy/hard | Poor quiz experience | `difficulty` parameter drives prompt instruction. EASY → obvious distractors, HARD → plausible distractors |
| Batch generation exceeds context window | Large N fails | Start with N=10 default, max 20. Document limits per model. Truncate disease content to fit |

### Dependencies

- **Sprint 0**: Full AI platform foundation
- **Sprint 1**: None (flashcards/quizzes independent of summary)

### Acceptance Criteria

1. `POST /api/ai/flashcards/generate` returns N flashcards with valid Q&A
2. `POST /api/ai/quizzes/generate` returns quiz with M questions, 4 options each, correct answer marked
3. Same request returns cached results within 7-day TTL
4. `regenerate` endpoints bypass cache and produce new content
5. Difficulty levels (EASY/MEDIUM/HARD) produce noticeably different content
6. Batch generation produces exactly the requested count
7. Generated content passes `AiOutputValidator`
8. Flashcards/quizzes persisted correctly with FK to disease
9. Quiz submission returns score + per-question feedback
10. Unauthorized requests (no `AI_FLASHCARD`/`AI_QUIZ`) return 403

### Definition of Done

- [ ] Flyway migrations `V2_2`, `V2_3` applied and reversible
- [ ] `Flashcard`, `Quiz`, `QuizQuestion` entities with correct JPA mappings
- [ ] All repositories with required queries
- [ ] `FlashcardPipeline` and `QuizPipeline` wired
- [ ] Both controllers with all endpoints
- [ ] Flashcard + quiz prompt templates seeded
- [ ] Cache configured with 7-day TTL
- [ ] Tests cover all acceptance criteria
- [ ] Integration tests for generation + persistence
- [ ] LSP diagnostics clean
- [ ] Existing tests still pass
- [ ] Documentation written

---

## Sprint 3 — AI Case Study + AI Tutor Chat

**Duration**: 2 weeks
**Complexity**: High (streaming, real-time interaction)
**Dependencies**: Sprint 0 (AiGateway, Prompt Engine, Cache, Usage Logging)

### Goal

Provide interactive clinical learning through AI-generated case studies and a context-aware AI tutor chat with SSE streaming.

### Objectives

1. Implement Case Study Pipeline (`CaseStudyPipeline`)
2. Implement Tutor Chat Pipeline (`ChatPipeline`)
3. Integrate SSE streaming for chat responses (existing `NotificationService` + `CHANNEL_AI_STREAM`)
4. Implement chat session management + message persistence
5. Implement case study generation + persistence
6. Seed case study and chat prompt templates

### Deliverables

#### Database Changes

**Flyway migrations** (from `AI_MODULE_AUDIT.md §3.3`):

| Migration | Table | Purpose |
|-----------|-------|---------|
| `V2_4__create_chat_tables.sql` | `chat_session`, `chat_message` | Chat sessions + messages |
| `V2_12__add_ai_columns_to_case_study.sql` | `case_study` | Add `ai_generated`, `prompt_version` columns |

**`chat_session` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
user_id           BIGINT NOT NULL FK → users.id
disease_id        BIGINT FK → disease.id (nullable)
title             VARCHAR(255) (nullable)
message_count     INT DEFAULT 0
total_tokens      INT DEFAULT 0
is_active         BOOLEAN DEFAULT true
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**`chat_message` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
session_id        BIGINT NOT NULL FK → chat_session.id ON DELETE CASCADE
role              VARCHAR(20) NOT NULL   -- USER, ASSISTANT, SYSTEM
content           TEXT NOT NULL
tokens            INT DEFAULT 0
model_used        VARCHAR(100)
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**`case_study` additions** (from `AI_MODULE_AUDIT.md §3.1`):

```
ALTER TABLE case_study ADD COLUMN ai_generated BOOLEAN DEFAULT false;
ALTER TABLE case_study ADD COLUMN prompt_version VARCHAR(50);
```

#### Entities

| Entity | Table | Notes |
|--------|-------|-------|
| `ChatSession` | `chat_session` | `User user` (ManyToOne), `Disease disease` (ManyToOne optional), `List<ChatMessage> messages` (OneToMany) |
| `ChatMessage` | `chat_message` | `ChatSession session` (ManyToOne), `role` enum (USER/ASSISTANT/SYSTEM) |

#### DTOs

| DTO | Fields | Notes |
|-----|--------|-------|
| `ChatSessionCreateRequest` | `diseaseId?: Long` | Optional context topic |
| `ChatSessionDTO` | `id`, `title`, `diseaseId`, `messageCount`, `createdAt`, `updatedAt` | Response |
| `ChatMessageRequest` | `message: String`, `stream?: boolean` | Input |
| `ChatMessageDTO` | `id`, `sessionId`, `role`, `content`, `tokens`, `createdAt` | Response |
| `ChatResponse` | `reply: String`, `sessionId`, `tokensUsed`, `createdAt` | Non-streaming response |
| `CaseStudyGenerateRequest` | `diseaseId: Long`, `difficulty?: String` (EASY/MEDIUM/HARD) | Input |

#### Repositories

| Repository | Key Queries |
|-----------|-------------|
| `ChatSessionRepository` | `findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(userId)` |
| `ChatMessageRepository` | `findTop20BySessionIdOrderByCreatedAtDesc(sessionId)` |

#### Services

| Component | Responsibility | Source § |
|-----------|---------------|----------|
| `CaseStudyPipeline` | Load disease + sections + existing cases → build prompt → call AiGateway → parse → persist → return | §4.2 |
| `ChatPipeline` | Load session + last 20 messages → build prompt with history → call AiGateway (stream/non-stream) → persist both messages → return | §4.2 |

**Streaming architecture** (from `AI_MODULE_AUDIT.md §1.5, §4.2 Chat Pipeline**):

```
User Request → AiChatController → ChatPipeline →
  AiOrchestrator → AiGatewayRouter → NineRouterGateway → 9router → SSE stream
                                                    ↓
                                              ChatMessage persisted
                                                    ↓
                                              ai_usage_log updated
```

SSE uses existing `NotificationService.CHANNEL_AI_STREAM`. Wire `AiGateway.chatStream()` output to `SseEmitter`.

#### Controllers

| Endpoint | Controller | Purpose |
|----------|-----------|---------|
| `POST /api/ai/cases/generate` | `AiCaseStudyController` | Generate case study |
| `POST /api/ai/cases/{diseaseId}/regenerate` | `AiCaseStudyController` | Regenerate |
| `POST /api/ai/chat/sessions` | `AiChatController` | Create session |
| `GET /api/ai/chat/sessions` | `AiChatController` | List my sessions |
| `GET /api/ai/chat/sessions/{sessionId}` | `AiChatController` | Get session with messages |
| `POST /api/ai/chat/sessions/{sessionId}/messages` | `AiChatController` | Send message (stream/non-stream) |
| `DELETE /api/ai/chat/sessions/{sessionId}` | `AiChatController` | End session |

#### Prompt Templates (seed)

| Template Name | Feature | Variables | Format |
|---------------|---------|-----------|--------|
| `casestudy-gen` | `casestudy` | `diseaseName`, `sections`, `symptoms`, `existingCases`, `difficulty` | JSON |
| `chat` | `chat` | `message`, `history`, `diseaseContext` | Text (streamable) |

#### Tests

| Test Suite | What It Covers |
|-----------|---------------|
| `CaseStudyPipelineTest` | Generation, JSON parsing, diagnosis-disease match validation |
| `ChatPipelineTest` | Message send, history injection, context trimming |
| `ChatStreamingTest` | SSE chunk emission, `done` signal, connection lifecycle |
| `ChatSessionTest` | Create, list, get, end session |
| `ChatMessageTest` | Persist user + AI messages, token tracking |
| `AiChatControllerTest` | All endpoints, auth, validation |
| `AiCaseStudyControllerTest` | Generate, regenerate, existing case study retrieval |
| `ChatPipelineHistoryTest` | Last 20 messages correctly injected, older messages dropped |
| `CaseStudyAccuracyTest` | Diagnosis in generated case matches requested disease |

#### Documentation

| Document | Content |
|----------|---------|
| Update `docs/API_CONTRACT.md` | Add case study + chat endpoints |
| `docs/ai/CASE_STUDY.md` | Generation pipeline, prompt design, accuracy validation |
| `docs/ai/CHAT.md` | Session management, streaming architecture, context trimming |
| Update `docs/FRONTEND_GUIDE.md` | SSE chat integration, session lifecycle for frontend |

### Risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| SSE connection drops mid-stream | User sees partial response | Frontend reconnects and requests last N messages. ChatMessage persisted as chunks arrive |
| Chat history exceeds context window | Prompt too long → provider error | Trim oldest messages first. Max 20 messages. Estimate token count from character length |
| Case study diagnosis mismatches disease | Wrong educational content | `AiOutputValidator` checks: diagnosis in output == requested disease. Fail + retry if mismatch |
| Chat session not scoped to user | Cross-user data leak | Every chat endpoint verifies `session.userId == currentUserId` |

### Dependencies

- **Sprint 0**: Full AI platform foundation
- **Sprint 1**: None
- **Sprint 2**: None

### Acceptance Criteria

1. `POST /api/ai/cases/generate` returns valid case study with chief complaint, symptoms, diagnosis matching disease
2. `POST /api/ai/chat/sessions/{sessionId}/messages` returns AI reply
3. Streaming (`stream=true`) returns SSE chunks with correct format
4. Chat messages persisted: both user message and AI response in `chat_message`
5. Chat history truncated to last 20 messages
6. Session creation with optional disease context works
7. Session listing scoped to current user
8. Case study accuracy validation rejects mismatched diagnosis
9. Unauthorized requests (no `AI_CASE`/`AI_CHAT`) return 403
10. SSE connection lifecycle handles reconnect gracefully

### Definition of Done

- [ ] Flyway migrations `V2_4`, `V2_12` applied and reversible
- [ ] `ChatSession`, `ChatMessage` entities with correct JPA mappings
- [ ] `case_study` table has `ai_generated`, `prompt_version` columns
- [ ] All repositories with required queries
- [ ] `CaseStudyPipeline` wired and testable
- [ ] `ChatPipeline` wired with streaming support
- [ ] Both controllers with all endpoints
- [ ] Case study + chat prompt templates seeded
- [ ] SSE streaming integrated with `NotificationService`
- [ ] Chat context trimming logic implemented
- [ ] Tests cover all acceptance criteria
- [ ] LSP diagnostics clean
- [ ] Existing tests still pass
- [ ] Documentation written

---

## Sprint 4 — Study Report + Recommendation Engine

**Duration**: 2 weeks
**Complexity**: Medium-High (cross-feature data aggregation)
**Dependencies**: Sprint 2 (flashcard + quiz data), Sprint 3 (chat data)

### Goal

Provide personalized learning experience by analyzing user study activity across flashcards, quizzes, and chat — generating study reports and learning recommendations.

### Objectives

1. Implement Study Report Pipeline (`StudyReportPipeline`)
2. Implement Recommendation Engine logic (weak topic detection from quiz/flashcard data)
3. Implement Learning Analytics aggregation (quiz scores, flashcard accuracy, chat topics)
4. Implement AI Feedback system (`ai_feedback` table, rating endpoints)
5. Implement Usage Dashboard endpoints (admin)
6. Seed study report prompt template

### Deliverables

#### Database Changes

**Flyway migrations** (from `AI_MODULE_AUDIT.md §3.3`):

| Migration | Table | Purpose |
|-----------|-------|---------|
| `V2_5__create_study_report.sql` | `study_report` | Per-user per-disease reports |
| `V2_9__create_ai_feedback.sql` | `ai_feedback` | User content quality ratings |

**`study_report` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
user_id           BIGINT NOT NULL FK → users.id
disease_id        BIGINT NOT NULL FK → disease.id
summary           TEXT
weak_areas        TEXT
strong_areas      TEXT
recommendations   TEXT
quiz_score_avg    DECIMAL(5,2)
flashcards_reviewed INT DEFAULT 0
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
UNIQUE (user_id, disease_id)
```

**`ai_feedback` schema** (from `AI_MODULE_AUDIT.md §3.2`):

```
id                BIGSERIAL PK
user_id           BIGINT NOT NULL FK → users.id
feature           VARCHAR(50) NOT NULL
reference_type    VARCHAR(50) NOT NULL
reference_id      BIGINT NOT NULL
rating            INT NOT NULL (1-5)
comment           TEXT (nullable)
created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

#### Entities

| Entity | Table | Notes |
|--------|-------|-------|
| `StudyReport` | `study_report` | `User user` (ManyToOne), `Disease disease` (ManyToOne), unique constraint on (user, disease) |
| `AiFeedback` | `ai_feedback` | Polymorphic reference (`referenceType` + `referenceId`), `User user` |

#### DTOs

| DTO | Fields | Notes |
|-----|--------|-------|
| `StudyReportRequest` | `diseaseId: Long` | Input |
| `StudyReportDTO` | `diseaseId`, `diseaseName`, `summary`, `weakAreas`, `strongAreas`, `recommendations`, `quizScoreAvg`, `flashcardsReviewed`, `generatedAt` | Response |
| `AiFeedbackRequest` | `feature`, `referenceType`, `referenceId`, `rating`, `comment` | Input |
| `AiFeedbackDTO` | `id`, `feature`, `rating`, `comment`, `createdAt` | Response |
| `LearningAnalyticsDTO` | `diseaseId`, `quizScoreAverage`, `quizAttempts`, `flashcardsReviewed`, `flashcardAccuracy`, `chatSessions`, `weakTopics` (List), `strongTopics` (List) | Aggregated data |
| `UsageStatsDTO` | Total requests, tokens, cost by feature, per-user breakdown | Admin |

#### Repositories

| Repository | Key Queries |
|-----------|-------------|
| `StudyReportRepository` | `findByUserIdAndDiseaseId(userId, diseaseId)` |
| `AiFeedbackRepository` | `findByFeatureAndReferenceTypeAndReferenceId(feature, type, refId)` |

Data queries use existing repositories:
- `QuizRepository`: scores by user + disease
- `QuizQuestionRepository`: per-question results
- `FlashcardRepository`: reviewed count, correct count by user + disease
- `ChatSessionRepository`: session count by user + disease
- `AiUsageLogRepository`: aggregated usage stats

#### Services

| Component | Responsibility | Source § |
|-----------|---------------|----------|
| `StudyReportPipeline` | Load quiz scores + flashcard stats + chat session count → build prompt with study data → call AiGateway → parse → persist (upsert) → return | §4.2 |
| — | Recommendation Engine (embedded in `StudyReportPipeline` prompt) | Weak area detection from low quiz scores + incorrect flashcards |
| `AiFeedbackService` | Submit rating (create/update), get feedback for content, aggregate ratings | §3.2 |

#### Controllers

| Endpoint | Controller | Purpose |
|----------|-----------|---------|
| `POST /api/ai/reports` | `AiStudyReportController` | Generate study report |
| `GET /api/ai/reports?diseaseId=` | `AiStudyReportController` | Get latest report |
| `GET /api/ai/reports/analytics?diseaseId=` | `AiStudyReportController` | Get raw analytics (no AI call) |
| `POST /api/ai/feedback` | `AiFeedbackController` | Submit feedback |
| `GET /api/ai/feedback?feature=&referenceType=&referenceId=` | `AiFeedbackController` | Get feedback for content |
| `GET /api/admin/ai/usage` | `AiAdminController` | Usage dashboard (aggregated stats) |
| `GET /api/admin/ai/usage/users` | `AiAdminController` | Per-user usage breakdown |
| `GET /api/admin/ai/usage/features` | `AiAdminController` | Per-feature usage breakdown |

#### Prompt Template (seed)

| Template Name | Feature | Variables | Format |
|---------------|---------|-----------|--------|
| `study-report` | `report` | `diseaseName`, `quizScoreAvg`, `flashcardsReviewed`, `flashcardAccuracy`, `chatSessions`, `weakTopics`, `strongTopics` | JSON |

#### Learning Analytics Logic (within pipeline, from `AI_MODULE_AUDIT.md §4.2 Study Report Pipeline**)

Raw data collected before AI call:
- Quiz: average score, questions answered, per-topic scores
- Flashcards: count reviewed, accuracy percentage
- Chat: sessions count, topics discussed (from session titles)
- Weak topics: quiz questions with wrong answers, flashcards marked incorrect

#### Tests

| Test Suite | What It Covers |
|-----------|---------------|
| `StudyReportPipelineTest` | Data aggregation → prompt build → AiGateway call → parse → persist (mocked) |
| `StudyReportControllerTest` | Generate, get latest, get analytics (no AI) |
| `AiFeedbackServiceTest` | Submit, update, get feedback, aggregate ratings |
| `AiFeedbackControllerTest` | All endpoints, validation |
| `LearningAnalyticsTest` | Data aggregation correctness (quiz scores averaged, flashcard accuracy calculated) |
| `AiAdminUsageControllerTest` | Usage stats endpoints, authorization |
| `StudyReportRegenerationTest` | Same disease generates new report (no caching for personal reports) |

#### Documentation

| Document | Content |
|----------|---------|
| Update `docs/API_CONTRACT.md` | Add report + feedback + admin usage endpoints |
| `docs/ai/STUDY_REPORT.md` | Report pipeline, analytics aggregation, recommendation logic |
| `docs/ai/FEEDBACK.md` | Feedback model, rating aggregation, admin usage dashboard |

### Risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Insufficient study data → poor recommendations | Report is useless | Pipeline handles empty data: explain why recommendations are limited. Prompt includes guidance for low-data scenario |
| Study report data points cross multiple features/databases | Complex query logic | Aggregation queries in `StudyReportPipeline` only. Keep queries simple — join quiz + flashcard + chat data for one disease at a time |
| Users game feedback system (always 1 or 5) | Skewed quality metrics | Track feedback distribution per user. Flag unusual patterns. Compute median + percentile, not just average |

### Dependencies

- **Sprint 0**: Full AI platform foundation
- **Sprint 2**: Quiz attempts data, flashcard review data (tables must exist with data)
- **Sprint 3**: Chat session data (sessions table must exist with data)
- **Note**: Reports work with empty data — may generate during sprints 2-3 but full value requires populated data

### Acceptance Criteria

1. `POST /api/ai/reports` returns `StudyReportDTO` with valid recommendations
2. Report includes quiz score average, flashcard stats, topic analysis
3. Report identifies weak areas based on low quiz/flashcard performance
4. `GET /api/ai/reports/analytics` returns aggregated data without AI call
5. Feedback submission persists and returns average rating
6. Admin usage endpoints return aggregated stats
7. Reports are user-specific (user A sees their data only)
8. Empty study data returns report with "not enough data" explanation
9. Unauthorized requests (no `AI_REPORT`) return 403

### Definition of Done

- [ ] Flyway migrations `V2_5`, `V2_9` applied and reversible
- [ ] `StudyReport`, `AiFeedback` entities with correct JPA mappings
- [ ] All repositories with required queries
- [ ] `StudyReportPipeline` with data aggregation + AI generation + upsert
- [ ] `AiFeedbackService` with CRUD + aggregation
- [ ] Study report controller with all endpoints
- [ ] AI feedback controller with all endpoints
- [ ] Admin usage controller with aggregated stats
- [ ] Study report prompt template seeded
- [ ] Tests cover all acceptance criteria
- [ ] LSP diagnostics clean
- [ ] Existing tests still pass
- [ ] Documentation written

---

## Final Architecture Reference

### 1. Overall Dependency Graph

```
Sprint 0 ──────────────────────────────────────────────────────
│                                                              │
│  AiGateway, NineRouterGateway, AiGatewayRouter               │
│  PromptTemplateService, PromptBuilder, PromptRenderer        │
│  PromptValidator, PromptVariables, PromptTesting             │
│  AiQuotaService, AiRateLimiter, AiCacheService               │
│  AiUsageService, PromptInjectionFilter, AiOutputValidator    │
│  Shared DTOs, Exceptions, Config, Test Infrastructure        │
│                                                              │
└────────┬──────────────┬──────────────┬──────────────┬────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
   Sprint 1        Sprint 2        Sprint 3        Sprint 4
   ┌────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐
   │Summary │    │Flashcard │    │CaseStudy │    │ Study Report │
   │Pipeline│    │Pipeline  │    │Pipeline  │    │ Pipeline     │
   │        │    │Quiz      │    │Chat      │    │ Recommendation│
   │        │    │Pipeline  │    │Pipeline  │    │ Engine       │
   │        │    │          │    │(SSE)     │    │ Analytics    │
   └────────┘    └──────────┘    └──────────┘    │ Feedback     │
                                                  └──────────────┘
                                                         ↑
                                          ┌──────────────┘
                                          │ Depends on quiz + flashcard
                                          │ + chat data from Sprints 2-3
```

**Key dependency rules:**
- Sprint 0 is required before any other sprint
- Sprints 1, 2, 3 are mutually independent (can parallelize)
- Sprint 4 requires data from Sprints 2 and 3 (but architecture is standalone)

### 2. Recommended Implementation Order

| Order | Sprint | Rationale |
|-------|--------|-----------|
| 1 | **Sprint 0** | Foundation — everything depends on this |
| 2a | **Sprint 1** | Simplest pipeline, proves the architecture works end-to-end |
| 2b | **Sprint 2** | Can run in parallel with Sprint 1 (no dependency) |
| 3 | **Sprint 3** | Higher complexity (streaming), best after gateway is proven |
| 4 | **Sprint 4** | Last — needs quiz/flashcard/chat data for full value |

**Parallel execution option**: Sprints 1 and 2 can be staffed in parallel. Sprint 3 starts when Sprint 0 is stable. Sprint 4 starts last.

### 3. High-Risk Components

| Component | Risk Level | Reason | Mitigation |
|-----------|-----------|--------|------------|
| **NineRouterGateway** | High | External dependency (9router API). If 9router changes spec or is down, all AI features fail | Isolate behind `AiGateway` interface. Keep `OpenAiGateway` as fallback. Circuit breaker |
| **SSE Streaming** | High | Real-time connection management, browser compatibility, reconnection | Use existing `NotificationService` patterns. Frontend `EventSource` with reconnect |
| **Prompt Injection Filter** | High | Both false positives (blocks legitimate input) and false negatives (misses attack) | Start conservative (known patterns only). Allowlist medical terms. Test with real user queries |
| **Context Window Management** | Medium | Disease sections can exceed model limits. Silent truncation loses data | Log truncation events. Pre-calculate token budget. Reserve minimum for response |
| **JSON Parsing (Batch)** | Medium | AI may return malformed JSON for flashcard/quiz arrays | Wrap in retry. Validate before persist. Reject + prompt retry with stricter format instruction |
| **Async Usage Logging** | Low | Best-effort means data loss possible | Acceptable for Phase 2. Add message queue in Phase 4 |

### 4. Expected Backend Package Structure After Phase 2

```
src/main/java/com/duoq/medlearn/
├── MedlearnApplication.java
│
├── ai/                                              ← NEW
│   ├── gateway/
│   │   ├── AiGateway.java                          (interface)
│   │   ├── NineRouterGateway.java
│   │   ├── OpenAiGateway.java                      (future sprint)
│   │   ├── ClaudeGateway.java                      (future sprint)
│   │   ├── AiGatewayRouter.java
│   │   └── dto/
│   │       ├── AiChatRequest.java
│   │       ├── AiChatResponse.java
│   │       ├── AiMessage.java
│   │       ├── AiRole.java
│   │       ├── AiChunk.java
│   │       └── ModelInfo.java
│   │
│   ├── prompt/
│   │   ├── PromptTemplateService.java              (interface)
│   │   ├── PromptTemplateServiceImpl.java
│   │   ├── PromptBuilder.java
│   │   ├── PromptRenderer.java
│   │   ├── PromptValidator.java
│   │   ├── PromptVariables.java
│   │   ├── PromptTesting.java
│   │   └── entity/
│   │       ├── PromptTemplate.java
│   │       ├── PromptVersion.java
│   │       └── PromptTestResult.java
│   │
│   ├── pipeline/
│   │   ├── AiOrchestrator.java
│   │   ├── SummaryPipeline.java
│   │   ├── FlashcardPipeline.java
│   │   ├── QuizPipeline.java
│   │   ├── CaseStudyPipeline.java
│   │   ├── ChatPipeline.java
│   │   └── StudyReportPipeline.java
│   │
│   ├── security/
│   │   ├── PromptInjectionFilter.java
│   │   ├── AiOutputValidator.java
│   │   ├── AiQuotaService.java
│   │   └── AiRateLimiter.java
│   │
│   ├── usage/
│   │   ├── AiUsageService.java
│   │   ├── AiCacheService.java
│   │   └── entity/
│   │       ├── AiUsageLog.java
│   │       └── AiCacheEntry.java
│   │
│   ├── feedback/
│   │   └── AiFeedbackService.java
│   │
│   └── controller/
│       ├── AiSummaryController.java
│       ├── AiFlashcardController.java
│       ├── AiQuizController.java
│       ├── AiChatController.java
│       ├── AiCaseStudyController.java
│       ├── AiStudyReportController.java
│       ├── AiFeedbackController.java
│       └── AiAdminController.java
│
├── controller/                                       (existing — no changes)
│   ├── AuthController.java
│   ├── UserController.java
│   ├── DiseaseController.java
│   ├── SymptomController.java
│   ├── CategoryController.java
│   ├── CaseStudyController.java
│   ├── DiseaseVersionController.java
│   ├── DiseaseSectionController.java
│   ├── AdminUserController.java
│   ├── SymptomCheckerController.java
│   └── AiStreamController.java                      (existing, extended)
│
├── service/                                          (existing — add only)
│   └── impl/
│       └── ModerationServiceImpl.java                (implement existing stub)
│
├── repository/
│   ├── AiSummaryRepository.java                     ← NEW
│   ├── FlashcardRepository.java                     ← NEW
│   ├── QuizRepository.java                          ← NEW
│   ├── QuizQuestionRepository.java                  ← NEW
│   ├── ChatSessionRepository.java                   ← NEW
│   ├── ChatMessageRepository.java                   ← NEW
│   ├── StudyReportRepository.java                   ← NEW
│   ├── AiUsageLogRepository.java                    ← NEW
│   ├── AiFeedbackRepository.java                    ← NEW
│   ├── PromptTemplateRepository.java                ← NEW
│   ├── PromptVersionRepository.java                 ← NEW
│   └── PromptTestResultRepository.java              ← NEW
│   └── (all existing repositories unchanged)
│
├── domain/
│   ├── entity/
│   │   ├── AiSummary.java                           ← NEW
│   │   ├── Flashcard.java                           ← NEW
│   │   ├── Quiz.java                                ← NEW
│   │   ├── QuizQuestion.java                        ← NEW
│   │   ├── ChatSession.java                         ← NEW
│   │   ├── ChatMessage.java                         ← NEW
│   │   ├── StudyReport.java                         ← NEW
│   │   ├── AiUsageLog.java                          ← NEW
│   │   ├── AiCacheEntry.java                        ← NEW
│   │   ├── AiFeedback.java                          ← NEW
│   │   ├── PromptTemplate.java                      ← NEW
│   │   ├── PromptVersion.java                       ← NEW
│   │   └── PromptTestResult.java                    ← NEW
│   │   └── (all existing entities unchanged)
│   │
│   ├── dto/
│   │   ├── ai/                                      ← NEW
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java
│   │   │   ├── SummaryRequest.java
│   │   │   ├── SummaryDTO.java
│   │   │   ├── SummaryType.java
│   │   │   ├── FlashcardGenerateRequest.java
│   │   │   ├── FlashcardDTO.java
│   │   │   ├── QuizGenerateRequest.java
│   │   │   ├── QuizDTO.java
│   │   │   ├── QuizQuestionDTO.java
│   │   │   ├── CaseStudyGenerateRequest.java
│   │   │   ├── StudyReportRequest.java
│   │   │   ├── StudyReportDTO.java
│   │   │   ├── LearningAnalyticsDTO.java
│   │   │   ├── UsageStatsDTO.java
│   │   │   ├── AiFeedbackRequest.java
│   │   │   ├── AiFeedbackDTO.java
│   │   │   ├── PromptTemplateDTO.java
│   │   │   ├── PromptTestResultDTO.java
│   │   │   ├── VersionComparisonDTO.java
│   │   │   └── (existing ai DTOs unchanged)
│   │   └── (all existing DTOs unchanged)
│   │
│   └── enums/
│       ├── PermissionCode.java                      (extended: +10 AI values)
│       ├── AuditAction.java                         (extended: +8 AI values)
│       └── (all existing enums unchanged)
│
├── config/
│   └── (existing — AI config properties added, no structural changes)
│
├── security/
│   └── (existing — AI endpoint patterns added to SecurityConfig)
│
├── mapper/
│   └── (existing — may add AI-specific mappers if needed)
│
├── exception/
│   ├── GlobalExceptionHandler.java                  (extended: +8 AI exception handlers)
│   └── (existing exceptions unchanged)
│   └── (new AI exceptions — see Sprint 0)
│
└── resources/
    └── db/
        └── migration/
            ├── V2_0__add_user_ai_columns.sql         ← NEW
            ├── V2_1__create_ai_summary.sql           ← NEW
            ├── V2_2__create_flashcard.sql            ← NEW
            ├── V2_3__create_quiz_tables.sql          ← NEW
            ├── V2_4__create_chat_tables.sql          ← NEW
            ├── V2_5__create_study_report.sql         ← NEW
            ├── V2_6__create_ai_usage_log.sql         ← NEW
            ├── V2_7__create_prompt_template.sql      ← NEW
            ├── V2_8__create_prompt_test_result.sql   ← NEW
            ├── V2_9__create_ai_feedback.sql          ← NEW
            ├── V2_10__create_ai_cache.sql            ← NEW
            ├── V2_11__seed_prompt_templates.sql      ← NEW
            ├── V2_12__add_ai_columns_to_case_study.sql ← NEW
            └── (all existing migrations unchanged)
```

### 5. Expected REST API Surface After Phase 2

#### AI Summary (`/api/ai/summary`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/summary` | Yes | `AI_SUMMARY` | 1 |
| `POST` | `/api/ai/summary?stream=true` | Yes | `AI_SUMMARY` | 1 |
| `GET` | `/api/ai/summary/{diseaseId}?type=` | Yes | `AI_SUMMARY` | 1 |
| `POST` | `/api/ai/summary/{diseaseId}/regenerate` | Yes | `AI_SUMMARY` | 1 |

#### AI Flashcards (`/api/ai/flashcards`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/flashcards/generate` | Yes | `AI_FLASHCARD` | 2 |
| `GET` | `/api/ai/flashcards?diseaseId=&difficulty=` | Yes | `AI_FLASHCARD` | 2 |
| `GET` | `/api/ai/flashcards/{id}` | Yes | `AI_FLASHCARD` | 2 |
| `POST` | `/api/ai/flashcards/{diseaseId}/regenerate` | Yes | `AI_FLASHCARD` | 2 |

#### AI Quiz (`/api/ai/quizzes`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/quizzes/generate` | Yes | `AI_QUIZ` | 2 |
| `GET` | `/api/ai/quizzes?diseaseId=&difficulty=` | Yes | `AI_QUIZ` | 2 |
| `GET` | `/api/ai/quizzes/{id}` | Yes | `AI_QUIZ` | 2 |
| `POST` | `/api/ai/quizzes/{diseaseId}/regenerate` | Yes | `AI_QUIZ` | 2 |
| `POST` | `/api/ai/quizzes/{quizId}/submit` | Yes | `AI_QUIZ` | 2 |

#### AI Case Study (`/api/ai/cases`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/cases/generate` | Yes | `AI_CASE` | 3 |
| `POST` | `/api/ai/cases/{diseaseId}/regenerate` | Yes | `AI_CASE` | 3 |

#### AI Chat (`/api/ai/chat`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/chat/sessions` | Yes | `AI_CHAT` | 3 |
| `GET` | `/api/ai/chat/sessions` | Yes | `AI_CHAT` | 3 |
| `GET` | `/api/ai/chat/sessions/{sessionId}` | Yes | `AI_CHAT` | 3 |
| `POST` | `/api/ai/chat/sessions/{sessionId}/messages` | Yes | `AI_CHAT` | 3 |
| `DELETE` | `/api/ai/chat/sessions/{sessionId}` | Yes | `AI_CHAT` | 3 |

#### AI Study Report (`/api/ai/reports`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/reports` | Yes | `AI_REPORT` | 4 |
| `GET` | `/api/ai/reports?diseaseId=` | Yes | `AI_REPORT` | 4 |
| `GET` | `/api/ai/reports/analytics?diseaseId=` | Yes | `AI_REPORT` | 4 |

#### AI Feedback (`/api/ai/feedback`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `POST` | `/api/ai/feedback` | Yes | `AI_USE` | 4 |
| `GET` | `/api/ai/feedback?feature=&type=&refId=` | Yes | `AI_USE` | 4 |

#### AI Admin (`/api/admin/ai`)

| Method | Endpoint | Auth | Permission | Sprint |
|--------|----------|------|-----------|--------|
| `GET` | `/api/admin/ai/usage` | Yes | `AI_ADMIN` | 4 |
| `GET` | `/api/admin/ai/usage/users` | Yes | `AI_ADMIN` | 4 |
| `GET` | `/api/admin/ai/usage/features` | Yes | `AI_ADMIN` | 4 |
| `GET` | `/api/admin/ai/templates` | Yes | `AI_MANAGE` | 0 |
| `POST` | `/api/admin/ai/templates` | Yes | `AI_MANAGE` | 0 |
| `PUT` | `/api/admin/ai/templates/{id}` | Yes | `AI_MANAGE` | 0 |
| `POST` | `/api/admin/ai/templates/{id}/activate` | Yes | `AI_MANAGE` | 0 |
| `GET` | `/api/admin/ai/templates/{id}/versions` | Yes | `AI_MANAGE` | 0 |
| `GET` | `/api/admin/ai/templates/{id}/test` | Yes | `AI_MANAGE` | 0 |
| `POST` | `/api/admin/ai/quota/{userId}` | Yes | `AI_ADMIN` | 0 |
| `GET` | `/api/admin/ai/quota` | Yes | `AI_ADMIN` | 0 |
| `GET` | `/api/admin/ai/quota` | Yes | `AI_ADMIN` | 0 |

### 6. Expected Database Additions After Phase 2

| # | Table | Sprint | Type | Rows Estimate (dev) |
|---|-------|--------|------|---------------------|
| 1 | `ai_summary` | 1 | New | Diseases × 4 types |
| 2 | `flashcard` | 2 | New | Diseases × 30 cards |
| 3 | `quiz` | 2 | New | Diseases × 2 quizzes |
| 4 | `quiz_question` | 2 | New | Quizzes × 10 questions |
| 5 | `chat_session` | 3 | New | Active users × 5 sessions |
| 6 | `chat_message` | 3 | New | Sessions × 20 messages |
| 7 | `study_report` | 4 | New | Users × diseases studied |
| 8 | `ai_usage_log` | 0 | New | All AI requests × 1 row |
| 9 | `ai_cache` | 0 | New | Unique request hashes |
| 10 | `prompt_template` | 0 | New | Seed: 9 templates |
| 11 | `prompt_test_result` | 0 | New | A/B test runs |
| 12 | `ai_feedback` | 4 | New | User ratings |

**Column additions to existing tables:**
- `users`: +4 columns (monthly_token_quota, tokens_used_this_month, quota_reset_at, ai_enabled)
- `case_study`: +2 columns (ai_generated, prompt_version)

**Total new tables**: 12
**Total modified tables**: 2 (users, case_study)
**Total Flyway migrations**: 12 (V2_0 through V2_12)

### 7. Phase 3 Candidates

These are explicitly OUT OF SCOPE for Phase 2. They are documented as future enhancements in `AI_MODULE_AUDIT.md` and listed here for roadmap continuity.

| Candidate | Description | When to Start | Source § |
|-----------|-------------|--------------|----------|
| **Vector Search** | Store disease content as embeddings. Enable semantic search across diseases, sections, and symptoms | When users need "find similar content" or free-text search beyond SQL `LIKE` | Future |
| **RAG (Retrieval-Augmented Generation)** | Inject relevant disease content into prompts via vector similarity instead of fixed context | When fixed context window limits prompt quality for complex diseases | Future |
| **Redis Cache** | Replace Caffeine in-memory cache with Redis for multi-instance cache sharing | When 2+ backend instances run behind load balancer (cache state needs to be shared) | §7.2 |
| **AI Service Extraction** | Extract AI features into separate `ai-service` microservice | When AI request latency impacts CRUD endpoint performance. See §7.4 for triggers (avg latency > 5s, CRUD degrades > 20%, > 10 concurrent AI req/s) | §7.4 |
| **Multi-model Routing** | Route different features to different providers/models based on cost/quality optimization | When cost management requires granular per-feature provider selection (e.g. flashcard → Ollama, summary → Claude) | §1.3, §5.2 |
| **Evaluation Framework** | Automated prompt output evaluation using LLM-as-judge. Track output quality over time per template version | When prompt changes need quantitative quality measurement before production deployment | §2.2 PromptTesting |
| **Offline Batch Queue** | RabbitMQ/SQS-based job queue for large AI workloads (summarize all diseases, seed all flashcards) | When nightly batch jobs hit HTTP timeout or need retry persistence | §5.5 |
| **Model Fine-tuning** | Fine-tune open-source model on medical content for lower cost per token | When monthly AI costs exceed $500 and a smaller fine-tuned model achieves comparable quality | Future |

---

*End of AI Phase 2 Roadmap. Architecture authority: `docs/architecture/AI_MODULE_AUDIT.md`. This is the official implementation guide — no deviations from the approved architecture.*
