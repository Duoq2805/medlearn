---
session: ses_0d21
updated: 2026-07-04T16:22:51.861Z
---

Now I have a complete picture of the entire backend. Let me produce the report.

<｜｜DSML｜｜tool_calls>
<｜｜DSML｜｜invoke name="write">
<｜｜DSML｜｜parameter name="filePath" string="true">D:\PersonalProject\medlearn_FirstVersion\medlearn\docs\architecture\AI_MODULE_AUDIT.md</｜｜DSML｜｜parameter>
<｜｜DSML｜｜parameter name="content" string="true"># AI Module Architecture Audit

> Produced: 2026-07-03  
> Phase 2 — Complete audit before implementation. NO CODE.

---

## 1. Current AI Architecture

### 1.1 What Already Exists?

Deterministic symptom checker only. No LLM/AI provider integration exists.

### 1.2 Endpoints

| Method | Path | Purpose | Controller |
|--------|------|---------|------------|
| `POST` | `/api/symptom-checker/check` | V0 basic scoring | `SymptomCheckerController` |
| `POST` | `/api/symptom-checker/v1/analyze` | V1 deterministic engine | `SymptomCheckerController` |
| `POST` | `/api/symptom-checker/v2/analyze` | V2 enhanced (confidence, severity, clinical explanation) | `SymptomCheckerController` |
| `GET` | `/api/ai/stream` | SSE stream for AI output | `AiStreamController` |

### 1.3 Services

| Interface | Impl | Purpose | Status |
|-----------|------|---------|--------|
| `SymptomCheckerService` | `SymptomCheckerServiceImpl` | V0 basic matching via weight scoring | Prod |
| `SymptomCheckerServiceV1` | `SymptomCheckerServiceImplV1` | V1 deterministic rule-based (score >= 20%, top 10) | Prod |
| `SymptomCheckerServiceV2` | `SymptomCheckerServiceImplV2` | V2 enhanced (coverage + avg weight + critical + specificity = confidence, severity rules) | Prod |
| `NotificationService` | `NotificationServiceImpl` | SSE infrastructure (two channels: notification, ai-stream) | Prod |
| `ModerationService` | (empty interface only) | No impl | Stub |

### 1.4 Entities

No dedicated AI entities exist.

Content entities used by symptom checker (as data source):
- `Disease` — metadata, `currentVersionId` points to approved version
- `DiseaseVersion` — versioned content, only `APPROVED` status queried
- `DiseaseVersionSymptom` — symptom-weight mapping (the core matching table)
- `DiseaseSection` — structured clinical content per version
- `SectionType` — section type taxonomy (definition, symptoms, causes, etc.)
- `Symptom` — symptom catalog
- `CaseStudy` — clinical cases (pre-seeded content, not AI-generated)
- `Category` — disease categorization

### 1.5 DTOs

| DTO | Location | Purpose |
|-----|----------|---------|
| `SymptomCheckerRequest` | `domain.dto.ai` | Input: `symptomIds`, `limit` |
| `SymptomMatchResult` | `domain.dto.ai` | V0 result: diseaseId, name, slug, matchCount, matchScore, matchedSymptoms, shortDescription |
| `DiseaseMatchResultDTO` | `domain.dto.ai` | V1 result: diseaseId, name, matchScore, matchedSymptoms, missingSymptoms, explanation + nested `SymptomInfo` |
| `DiseaseMatchResultV2DTO` | `domain.dto.ai` | V2 result: diseaseId, name, matchScore, confidence, severity + explanation, clinicalExplanation, all symptom groups + `Severity` enum |
| `AiStreamChunkResponse` | `domain.dto.ai` | SSE chunk: `chunk`, `done`, `createdAt` |

### 1.6 Repositories

| Repository | Key Queries |
|------------|-------------|
| `DiseaseVersionSymptomRepository` | `findMatchingDiseasesRaw` — core matching query; `findDiseaseSymptomDetailsForV2` — batch symptom load; `findInternalSymptomDetails` |
| `DiseaseSectionRepository` | Section content for shortDescription |
| `DiseaseRepository` | Disease metadata lookup |
| `SymptomRepository` | Symptom entity lookup |

### 1.7 Documents

| File | Content |
|------|---------|
| `docs/symptom-checker-v2-design.md` | V2 algorithm: query plan, ranking, confidence formula, red flag rules |
| `docs/symptom-checker-v2-api.md` | V2 endpoint contract, request/response examples |
| `docs/ARCHITECTURE_AUDIT.md` | General backend audit (not AI-specific) |
| `docs/BACKEND_ARCHITECTURE.md` | Layer diagram, dependency flow, conventions |
| `docs/API_CONTRACT.md` | All API endpoints list |
| `docs/summaries/SESSION_SUMMARY.md` | Symptom Checker V2 session notes |

---

## 2. Gap Analysis

### P0 — Missing for Minimal Viable AI Platform

- **AI Provider Gateway**: No abstraction. No LLM integration at all.
- **9router Provider**: Zero implementation for target provider.
- **AI Summary**: No endpoint, service, entity, or DTO for generating/storing disease summaries.
- **Flashcards**: No generation pipeline or storage for flashcards.
- **Quiz Generation**: No quiz system (questions, options, answers, difficulty tiers).
- **AI Chat**: No conversational endpoint, session management, or message history.
- **Study Report**: No report generation or progress tracking.
- **Database Schema for AI**: No tables for any AI feature.
- **Usage/Token Tracking**: No cost or token counters.
- **AI Audit Trail**: No AI-specific audit actions in `AuditAction` enum.

### P1 — Important for Production Quality

- **Rate Limiting**: No per-endpoint or per-user rate limits for AI calls.
- **User Quota**: No per-user daily/monthly token budgets.
- **Prompt Template System**: No templates, variables, versioning, or storage.
- **Prompt Injection Protection**: No input sanitization for AI prompts.
- **Streaming Endpoint**: SSE infrastructure exists but no actual streaming AI endpoint wired.
- **Error Handling for AI**: No provider-specific error mapping, no retry logic, no fallback.
- **Configuration**: No per-provider config (API keys, base URLs, models, timeouts).

### P2 — Valuable for User Experience

- **AI Response Caching**: Identical requests hit provider each time.
- **Batch Generation**: No queue for generating content in bulk (e.g., summarize all diseases).
- **Feedback Loop**: No user rating for AI-generated content (thumb up/down).
- **A/B Prompt Testing**: No prompt variant testing infrastructure.
- **Cost Dashboard**: No per-user/per-feature cost breakdown.
- **Provider Health Check**: No endpoint to verify provider availability.

### P3 — Future

- **Model Fine-tuning**: No support for custom model tuning.
- **Multi-language AI**: Responses in single language only.
- **Offline Batch Queue**: No async job queue for large AI workloads.
- **Automated Prompt Optimization**: No metrics-driven prompt tuning.
- **AI Analytics Dashboard**: No usage trends, popular features, error rates visualization.

---

## 3. Current Database Schema

### Schema Support Matrix

| Feature | Supported? | Gaps |
|---------|-----------|------|
| **AI Summary** | ❌ | No storage for generated summaries. Need `ai_summary` table linking to disease/version. |
| **Flashcards** | ❌ | No flashcards table. Need `flashcard` (question, answer, disease_id, difficulty, tags). |
| **Quiz** | ❌ | No quiz tables. Need `quiz` (title, difficulty, disease_id), `quiz_question` (question, options[], correctAnswer, explanation). |
| **Case Study** | ⚠️ Partial | `CaseStudy` entity exists for manual content. Need field for `ai_generated` flag, `prompt_version`, or separate flow. |
| **Chat** | ❌ | No chat storage. Need `chat_session` (user_id, disease_id, title), `chat_message` (session_id, role, content, tokens). |
| **Study Report** | ❌ | No report table. Need `study_report` (user_id, disease_id, summary, weakAreas, recommendations, generatedAt). |
| **Usage Logging** | ❌ | `AuditLog` exists but lacks AI-specific fields (tokens, model, cost, feature). Need `ai_usage_log`. |
| **Prompt Templates** | ❌ | No template storage. Need `prompt_template` (name, content, version, variables[], active). |
| **AI History** | ❌ | No request/response log. Need `ai_request_log` (user_id, feature, prompt, response, tokens, model, latency, success). |

### Required New Tables

```
ai_summary
├── id BIGSERIAL PK
├── disease_id BIGINT FK → disease
├── disease_version_id BIGINT FK → disease_version
├── summary_type VARCHAR(50)   -- short, detailed, clinical, patient
├── content TEXT
├── model_used VARCHAR(100)
├── prompt_version VARCHAR(50)
├── tokens_input INT
├── tokens_output INT
├── created_at TIMESTAMPTZ

flashcard
├── id BIGSERIAL PK
├── disease_id BIGINT FK → disease
├── question TEXT
├── answer TEXT
├── difficulty VARCHAR(20)     -- EASY, MEDIUM, HARD
├── tags VARCHAR(255)[]
├── ai_generated BOOLEAN
├── prompt_version VARCHAR(50)
├── created_at TIMESTAMPTZ

quiz
├── id BIGSERIAL PK
├── title VARCHAR(255)
├── disease_id BIGINT FK → disease
├── difficulty VARCHAR(20)
├── ai_generated BOOLEAN
├── created_at TIMESTAMPTZ

quiz_question
├── id BIGSERIAL PK
├── quiz_id BIGINT FK → quiz
├── question TEXT
├── options JSONB            -- [{"key":"A","value":"..."}, ...]
├── correct_answer VARCHAR(10)
├── explanation TEXT
├── order_index INT

chat_session
├── id BIGSERIAL PK
├── user_id BIGINT FK → users
├── disease_id BIGINT FK → disease (optional context)
├── title VARCHAR(255)
├── created_at TIMESTAMPTZ
├── updated_at TIMESTAMPTZ

chat_message
├── id BIGSERIAL PK
├── session_id BIGINT FK → chat_session
├── role VARCHAR(20)         -- USER, ASSISTANT, SYSTEM
├── content TEXT
├── tokens INT
├── created_at TIMESTAMPTZ

study_report
├── id BIGSERIAL PK
├── user_id BIGINT FK → users
├── disease_id BIGINT FK → disease
├── summary TEXT
├── weak_areas TEXT
├── recommendations TEXT
├── generated_at TIMESTAMPTZ

ai_usage_log
├── id BIGSERIAL PK
├── user_id BIGINT FK → users
├── feature VARCHAR(50)       -- summary, flashcard, quiz, chat, symptom-checker
├── provider VARCHAR(50)      -- 9router, openai, claude, gemini, ollama
├── model VARCHAR(100)
├── tokens_input INT
├── tokens_output INT
├── cost DECIMAL(10,6)
├── latency_ms INT
├── success BOOLEAN
├── error_message TEXT
├── created_at TIMESTAMPTZ

prompt_template
├── id BIGSERIAL PK
├── name VARCHAR(100)
├── description TEXT
├── content TEXT              -- template with {{variable}} placeholders
├── variables JSONB           -- ["diseaseName", "symptoms", ...]
├── version INT
├── active BOOLEAN
├── feature VARCHAR(50)
├── created_at TIMESTAMPTZ
├── updated_at TIMESTAMPTZ

ai_request_log
├── id BIGSERIAL PK
├── user_id BIGINT FK → users
├── feature VARCHAR(50)
├── prompt TEXT
├── response TEXT
├── tokens_input INT
├── tokens_output INT
├── model VARCHAR(100)
├── provider VARCHAR(50)
├── latency_ms INT
├── success BOOLEAN
├── created_at TIMESTAMPTZ
```

### Existing Tables That Support AI

| Table | Role |
|-------|------|
| `users` | User identity, quota tracking (need new column for token budget) |
| `disease` | Content root for AI generation |
| `disease_version` | Versioned source content for AI |
| `disease_section` | Structured content sections — the primary data LLM summarizes |
| `disease_version_symptom` | Symptom data for context |
| `symptom` | Symptom definitions for context |
| `case_study` | Clinical cases (potential AI training/context data) |
| `section_type` | Section taxonomy for structured prompts |
| `audit_log` | General audit (can extend with AI actions) |
| `role` / `role_permission` | Access control for AI features |

---

## 4. Existing Reusable Components

### Do NOT Rewrite — Fully Reusable

| Component | File | Reason |
|-----------|------|--------|
| **JwtService** | `security/JwtService.java` | Token auth for AI endpoints — no changes needed. |
| **CurrentUserResolver** | `security/CurrentUserResolver.java` | Resolve caller for audit, quota — directly reusable. |
| **SecurityConfig** | `config/SecurityConfig.java` | JWT filter chain — AI endpoints just add matchers. |
| **PermissionService** | `service/PermissionService.java` | `@PreAuthorize` guards — extend enum, reuse service. |
| **AuditService** | `service/AuditService.java` | Best-effort audit — reuse for AI request logging. |
| **AuditLogRepository** | `repository/AuditLogRepository.java` | Audit persistence — queries work as-is. |
| **NotificationService** | `service/NotificationService.java` | SSE channels — reuse for streaming AI responses. |
| **NotificationServiceImpl** | `service/impl/NotificationServiceImpl.java` | ConcurrentHashMap emitter registry — works. |
| **ApiResponse** | `domain.dto.common.ApiResponse.java` | Standard wrapper — all AI endpoints use same format. |
| **PagedResponse** | `domain.dto.common/PagedResponse.java` | Pagination for list endpoints. |
| **GlobalExceptionHandler** | `exception/GlobalExceptionHandler.java` | Extend with AI-specific exceptions, keep existing. |
| **MapStructConfig** | `mapper/MapStructConfig.java` | Mapper convention — use for any new AI DTOs. |
| **BaseRepository** | `repository/BaseRepository.java` | Slug lookup — reuse for new AI entities if they have slugs. |
| **DiseaseMapper** | `mapper/DiseaseMapper.java` | Disease-to-DTO — AI generation outputs may reference disease IDs. |
| **CaseStudyMapper** | `mapper/CaseStudyMapper.java` | Case study mapping — reuse if AI generates case studies. |
| **PermissionCode** enum | `domain.enums/PermissionCode.java` | Add AI permissions, keep existing structure. |
| **AuditAction** enum | `domain.enums/AuditAction.java` | Add AI audit actions, keep existing values. |

### Extend — Not Rewrite

| Component | Extension Needed |
|-----------|-----------------|
| `PermissionCode` | Add `AI_USE`, `AI_MANAGE`, `AI_VIEW_USAGE`, `AI_ADMIN` |
| `AuditAction` | Add `AI_REQUEST`, `AI_RESPONSE`, `AI_QUOTA_EXCEEDED` |
| `ModerationService` | Implement interface with AI content moderation |
| `User` entity | Add `monthly_token_quota`, `tokens_used_this_month`, `ai_enabled` |
| `application.yml` | Add `ai.provider.9router.*` config block |
| `DiseaseVersionSymptomRepository` | Keep queries — they power symptom context for AI prompts |

### Patterns to Replicate

| Pattern | Source | Apply To |
|---------|--------|----------|
| service interface + impl separation | All services | All AI services |
| `@Transactional(readOnly = true)` for reads | DiseaseServiceImpl | AI read endpoints |
| MapStruct DTO mapping | DiseaseMapper | AI DTO mapping |
| `@PreAuthorize` + SpEL with `@permissionService` | DiseaseController | AI endpoint access control |
| SSE streaming via NotificationService | NotificationServiceImpl | AI streaming responses |
| Best-effort audit in REQUIRES_NEW | AuditServiceImpl | AI usage logging |

---

## 5. AI Gateway Design

### 5.1 Provider Abstraction — Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    AiGateway (interface)                 │
│  + chat(request: AiChatRequest): AiResponse             │
│  + chatStream(request: AiChatRequest): Flux<AiChunk>    │
│  + embed(text: String): List<Float>                     │
│  + isAvailable(): boolean                               │
│  + getModelInfo(): ModelInfo                            │
└────────────────────────┬────────────────────────────────┘
                         │ implements
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌─────────────────┐ ┌────────────┐ ┌──────────────┐
│ NineRouterGateway │ │ OpenAiGate │ │ ClaudeGateway│
│ (target)         │ │ way        │ │              │
│                  │ │ (future)   │ │ (future)      │
└─────────────────┘ └────────────┘ └──────────────┘
```

### 5.2 Core DTOs

```
AiChatRequest
├── model: String (optional — use default if null)
├── messages: List<AiMessage>
├── temperature: Double (optional)
├── maxTokens: Integer (optional)
├── stream: boolean
├── userId: Long (for audit)

AiMessage
├── role: AiRole (SYSTEM, USER, ASSISTANT)
├── content: String

AiResponse
├── content: String
├── model: String
├── tokensInput: Integer
├── tokensOutput: Integer
├── finishReason: String

AiChunk
├── content: String
├── done: boolean

ModelInfo
├── name: String
├── contextWindow: Integer
├── supportsStreaming: boolean
├── supportsEmbedding: boolean
├── costPer1kInputTokens: BigDecimal
├── costPer1kOutputTokens: BigDecimal
```

### 5.3 Configuration Model

```
ai:
  default-provider: 9router
  providers:
    9router:
      base-url: ${NINEROUTER_BASE_URL}
      api-key: ${NINEROUTER_API_KEY}
      default-model: ${NINEROUTER_DEFAULT_MODEL}
      timeout: 30000
      max-retries: 3
      retry-backoff-ms: 1000
    openai:
      base-url: https://api.openai.com/v1
      api-key: ${OPENAI_API_KEY}
      default-model: gpt-4o
      timeout: 30000
      max-retries: 2
    claude:
      base-url: https://api.anthropic.com/v1
      api-key: ${CLAUDE_API_KEY}
      default-model: claude-sonnet-4-20250514
    gemini:
      base-url: https://generativelanguage.googleapis.com/v1
      api-key: ${GEMINI_API_KEY}
      default-model: gemini-2.0-flash
    ollama:
      base-url: http://localhost:11434
      default-model: llama3
      api-key: ""     # not required
```

### 5.4 Provider Selection Strategy

```
AiGatewayRouter
├── Routes to configured provider based on feature or request param
├── Falls back to secondary provider if primary fails
├── Enforces per-provider rate limits and quotas

Strategy:
  1. AiGatewayRouter receives AiChatRequest
  2. Determines target provider from: request.provider -> feature config -> default
  3. Wraps provider gateway with retry/degradation
  4. Returns AiResponse (or Flux<AiChunk> for streaming)
```

### 5.5 Error Mapping

| Provider Error | Application Exception | HTTP Status |
|---------------|----------------------|-------------|
| 401 Unauthorized | `AiAuthenticationException` | 502 |
| 429 Rate Limited | `AiRateLimitException` | 429 |
| 500 Server Error | `AiProviderException` (with retry) | 502 |
| Timeout | `AiTimeoutException` | 504 |
| Context Length | `AiContextLengthException` | 400 |
| Model Deprecated | `AiModelException` | 502 |

---

## 6. Prompt System

### 6.1 Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    PromptTemplateService                  │
│  + getTemplate(name, feature): PromptTemplate            │
│  + render(template, variables): String                   │
│  + createTemplate(request): PromptTemplate               │
│  + updateTemplate(id, request): PromptTemplate           │
│  + activateVersion(id, version): void                    │
│  + listTemplates(feature): List<PromptTemplate>          │
│  + testPrompt(templateId, testInput): PromptTestResult   │
└──────────────────────────────────────────────────────────┘
```

### 6.2 Components

**PromptTemplate**
```
PromptTemplate
├── id: Long
├── name: String              # "disease-summary-short", "flashcard-gen"
├── description: String
├── content: String           # "Summarize {{diseaseName}} in {{maxWords}} words."
├── variables: List<String>   # ["diseaseName", "maxWords"]
├── version: Integer
├── active: Boolean           # Only one version active per name
├── feature: String           # summary, flashcard, quiz, chat
├── systemPrompt: String      # Optional base system instruction
├── temperature: Double       # Optional override
├── maxTokens: Integer        # Optional override
├── createdAt: OffsetDateTime
├── updatedAt: OffsetDateTime
```

**PromptBuilder**
```
PromptBuilder
├── start(templateName)       # Load active template by name
├── with(key, value)           # Set variable value
├── withContext(content)      # Append context (disease sections, symptoms)
├── withHistory(messages)     # Append conversation history (chat only)
├── withFormat(format)        # Output format: JSON, TEXT, MARKDOWN
├── build(): AiChatRequest    # Produce final AiChatRequest with system + user messages
```

**PromptVariables**
```
PromptVariables
├── keys(): Set<String>               # Extract {{variable}} from template text
├── validate(template, values): ValidationResult  # Check all required variables provided
├── missing(template, values): List<String>       # List unset required variables
```

**PromptVersion**
```
PromptVersion
├── templateId: Long
├── version: Integer
├── content: String
├── variables: List<String>
├── changelog: String
├── createdBy: Long
├── createdAt: OffsetDateTime
├── testResults: List<PromptTestResult>  # Historical test runs for this version
```

**PromptStorage**
```
PromptStorage
│   Table: prompt_template
│   ├── One row per name/version
│   ├── active = true marks current production version
│   └── Index on (name, active) for fast lookup
│
│   Table: prompt_test_result
│   ├── template_id, version, test_input, test_output
│   ├── tokens_used, latency_ms
│   └── human_rating (nullable, for feedback loop)
```

**PromptTesting**
```
PromptTesting
├── testPrompt(template, testVariables): PromptTestResult
│   └── Sends to configured AI provider with test input
│   └── Returns: output, tokens, latency
│
├── compareVersions(templateName, versionA, versionB, testCases): VersionComparison
│   └── Runs same test cases against both versions
│   └── Returns side-by-side output, cost diff, latency diff
│
├── evaluateOutput(output, criteria): EvaluationScore
│   └── Regex checks: JSON validity, required sections, length
│   └── Future: LLM-as-judge evaluation
```

### 6.3 Template Variables Convention

```
Disease Context:
  {{diseaseName}}       — Disease.name
  {{diseaseSlug}}       — Disease.slug
  {{categoryName}}      — Category.name
  {{sections}}          — All DiseaseSection content formatted as "## {title}\n{content}"
  {{symptoms}}          — Symptom names list

User Context:
  {{userName}}          — User.fullName
  {{difficulty}}        — "EASY" | "MEDIUM" | "HARD" (for quiz/flashcard)

Generation Control:
  {{maxWords}}          — Summary word limit
  {{questionCount}}     — Number of questions/flashcards
  {{format}}            — "json" | "markdown" | "text"

Chat:
  {{message}}           — Current user message
  {{history}}           — Previous messages in session
  {{diseaseContext}}     — Disease info if context-bound
```

---

## 7. AI Pipeline

### 7.1 Overview

```
User Request
    │
    ▼
┌──────────────────────────────────────────┐
│            AiOrchestrator                 │
│  Coordinates multi-step generation flow   │
│  Manages:                                 │
│    - Provider selection                   │
│    - Prompt rendering                     │
│    - Response parsing                     │
│    - Caching check                        │
│    - Audit logging                        │
│    - Token/usage tracking                 │
│    - Error handling                       │
└──────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────┐
│         Feature Pipelines                 │
│                                           │
│  Summary  Flashcard  Quiz  CaseStudy  Chat│
│     │        │        │        │       │  │
│     └────────┴────────┴────────┴───────┘  │
│     Each flow uses AiGateway +             │
│     PromptTemplateService                  │
└──────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────────────────┐
│            Persistence Layer              │
│  ai_summary, flashcard, quiz,             │
│  quiz_question, chat_session,             │
│  chat_message, study_report               │
└──────────────────────────────────────────┘
```

### 7.2 Feature Pipelines

**Summary Pipeline**
```
1. Request: diseaseId + summaryType (short|detailed|clinical|patient)
2. Load: Disease + currentVersion + all DiseaseSections
3. Build: PromptBuilder.start("disease-summary-{type}").with("diseaseName", name)
         .withContext(sections).with("maxWords", limit)
4. Call: AiGateway.chat(request) or chatStream(request)
5. Parse: Validate JSON response (structured summaries) or plain text
6. Persist: ai_summary (disease_id, version_id, type, content, model, tokens)
7. Return: AiSummaryDTO or stream chunks
```

**Flashcard Pipeline**
```
1. Request: diseaseId + count + difficulty
2. Load: Disease + sections + symptoms
3. Build: PromptBuilder.start("flashcard-gen").withContext(sections+symptoms)
         .with("count", count).with("difficulty", difficulty).withFormat("json")
4. Call: AiGateway.chat(request)
5. Parse: JSON array → List<FlashcardDTO>
6. Persist: flashcard rows
7. Return: List<FlashcardDTO>
```

**Quiz Pipeline**
```
1. Request: diseaseId + questionCount + difficulty
2. Load: Same as flashcard
3. Build: PromptBuilder.start("quiz-gen").withContext(sections).with("count", count)
         .with("difficulty", difficulty).withFormat("json")
4. Call: AiGateway.chat(request)
5. Parse: JSON → quiz + questions
6. Persist: quiz + quiz_question rows
7. Return: QuizDTO (with questions, options, correctAnswers)
```

**Case Study Pipeline**
```
1. Request: diseaseId + difficulty (optional)
2. Load: Disease + sections + symptoms + treatment info
3. Build: PromptBuilder.start("casestudy-gen").withContext(allContent)
         .with("difficulty", difficulty).withFormat("json")
4. Call: AiGateway.chat(request)
5. Parse: JSON → CaseStudyCreateRequest
6. Persist: case_study with aiGenerated = true
7. Return: CaseStudyDetailDTO
```

**Chat Pipeline**
```
1. Request: sessionId + message
2. Load: Existing session messages (last N for context)
3. Build: PromptBuilder.start("chat").withHistory(messages).with("message", input)
         .withContext(diseaseContext if bound)
4. Call: AiGateway.chat(request) or chatStream(request)
5. Persist: User message + AI response to chat_message
6. Return: AiChatResponse or stream chunks
```

**Study Report Pipeline**
```
1. Request: userId + diseaseId (optional all studied diseases)
2. Load: User quiz results, case study attempts, flashcards reviewed
3. Build: PromptBuilder.start("study-report").withContext(studyHistory)
         .withFormat("json")
4. Call: AiGateway.chat(request)
5. Parse: JSON → StudyReportDTO (summary, weakAreas, recommendations)
6. Persist: study_report
7. Return: StudyReportDTO
```

### 7.3 Pipeline Cross-Cutting Concerns

```
Each pipeline step includes:
├── AiUsageLog.update(feature, tokens, cost, latency, success)
├── AuditService.log(AI_REQUEST / AI_RESPONSE)
├── Cache.check(template + variables hash) → skip if cached
├── Quota.check(userId, estimatedTokens) → reject if exceeded
├── RateLimit.check(userId, feature) → throttle if exceeded
└── ErrorHandler.wrap(providerException) → application exception
```

---

## 8. Cost Strategy

### 8.1 Caching

| Strategy | Where | What |
|----------|-------|------|
| **Response Cache** | Before AiGateway | Hash of `templateName + variables + model` → reuse identical requests. TTL: 24h for summaries, 7d for flashcards/quizzes. |
| **Context Cache** | Before PromptBuilder | Disease sections content rarely changes. Cache `DiseaseVersion.id → sections text` with version-based invalidation. |
| **Embedding Cache** | AiGateway embed() | Cache text → embedding vector for similarity search. |

Implementation: Spring `@Cacheable` on `AiOrchestrator` methods. Redis or Caffeine.

### 8.2 Reuse

- **Reuse AI output across users**: Summaries, flashcards, quizzes for same disease version are content — serve identical cached copy to all users.
- **Deduplicate requests**: Same user clicking "generate summary" twice within TTL → return cached.
- **Reuse rendered prompts**: Cache rendered prompt strings keyed by template version + variable hash.

### 8.3 Streaming

- **Always stream** for user-facing generation (summary, chat, quiz). Lower perceived latency, user sees tokens incrementally.
- **Batch (non-stream)** for pre-generation (cron-based nightly summary generation, bulk flashcard creation).
- SSE infrastructure already exists via `NotificationService` — wire `AiGateway.chatStream()` → `SseEmitter`.

### 8.4 Batching

| Scenario | Batch Strategy |
|----------|---------------|
| Generate summaries for all diseases | Background job: page diseases, batch N summaries per LLM call via multi-summary prompt, persist all |
| Generate flashcards for a disease | Single LLM call requesting N flashcards (use JSON array output), not N separate calls |
| Quiz question generation | Same as flashcards — one call for N questions |

### 8.5 Model Selection Hierarchy

| Feature | Model Tier | Rationale |
|---------|-----------|-----------|
| Short summary | Fast/cheap model | Simple extraction, low creativity |
| Detailed summary | Full model (default) | Needs synthesis |
| Clinical summary | Full model | Accuracy-critical, needs medical reasoning |
| Flashcard generation | Fast/cheap model | Extractive, well-defined format |
| Quiz generation | Full model | Needs distractor generation |
| Case study generation | Full model | High creativity + medical accuracy |
| Chat | Full model | Interactive, needs reasoning |
| Study report | Full model | Synthesis across multiple data points |

### 8.
