# Flashcard Module — Production Readiness Review

## Files Inspected (36 files)

**Controllers (3):** FlashcardController, FlashcardDeckController, FlashcardReviewController
**Services + impls (6):** FlashcardGenerator/Impl, FlashcardService/Impl, FlashcardReviewService/Impl
**Entities (5):** FlashcardDeck, Flashcard, FlashcardSource, FlashcardProgress, FlashcardReviewLog
**Enums (3):** FlashcardStatus, FlashcardDifficulty, SourceType
**Repositories (5):** All 5 with custom JPQL queries
**DTOs (11):** 4 request + 7 response DTOs
**Mapper (1):** FlashcardMapper
**Utils (2):** Sm2Algorithm, JsonFlashcardParser
**Shared infra (5):** AiGeneration, AiFeedback, FeatureType, AiGenerationRepository, AiFeedbackRepository
**Migration (1):** V3_0__create_flashcard_module.sql

---

## Findings

### CRITICAL — 2 issues

| # | Issue | File | Root cause |
|---|---|---|---|
| **C1** | **PromptBuilder not reused — prompts hardcoded** | `FlashcardGeneratorImpl.java:119-131` | Builds system/user prompt strings manually instead of calling `PromptBuilder.build("flashcard-gen", variables)`. Bypasses `PromptInjectionFilter.sanitizeVariables()`, required-variable validation, and template-version tracking. |
| **C2** | **PromptTemplate version not persisted** | `FlashcardGeneratorImpl.java:207-222` | `AiGeneration.promptTemplateVersion` is never set (always null). Violates architecture requirement: "Persist prompt template code AND prompt version inside AiGeneration." |

**Fix C1:** Replace manual prompt construction with `PromptBuilder`:

```java
// Inject PromptBuilder + PromptTemplateService
var template = promptTemplateService.getActiveEntity(PROMPT_CODE);
var variables = Map.of("disease", diseaseName, "count", String.valueOf(request.getCount()), ...);
var messages = promptBuilder.buildFromTemplate(template, sanitizedVariables);
var chatRequest = AiChatRequest.builder()
    .messages(messages)
    .promptTemplateCode(PROMPT_CODE)
    .model(template.getModel()) // model from template, not hardcoded
    ...
```

**Fix C2:** After fix C1, add `promptTemplateVersion` to AiGeneration:

```java
AiGeneration.builder()
    .promptTemplateCode(PROMPT_CODE)
    .promptTemplateVersion(template.getVersion()) // ← add this
    ...
```

---

### HIGH — 3 issues

| # | Issue | File | Root cause |
|---|---|---|---|
| **H1** | **N+1 queries in getDueCards** | `FlashcardProgressRepository.java:18-26` | `findDueByUserId` uses JOIN FETCH for flashcard but `toProgressResponse` → `flashcardMapper.toResponse()` triggers lazy load of `createdBy` User for each card. |
| **H2** | **Individual INSERTs in loop** | `FlashcardGeneratorImpl.java:178-204` | `flashcardRepository.save(card)` called per card and per source. `saveAll()` available but unused. 25 cards = 25+ INSERT statements. |
| **H3** | **Export endpoint lacks deck ownership check** | `FlashcardServiceImpl.java:124-150` | `exportDeck` ignores `userId` — any user with `FLASHCARD_EXPORT` can export any deck by guessing its ID (IDOR). |

**Fix H1:** Add `JOIN FETCH f.createdBy` to the query OR accept the lazy load (User entity is cached and the deck title is already loaded via JOIN FETCH). Simplest fix — add `JOIN FETCH f.createdBy`:

```java
@Query("SELECT fp FROM FlashcardProgress fp " +
       "JOIN FETCH fp.flashcard f " +
       "JOIN FETCH f.createdBy " +
       "JOIN FETCH f.deck " +
       "WHERE fp.user.id = :userId ...")
```

**Fix H2:** Replace loop with `flashcardRepository.saveAll(cards)` and `sourceRepository.saveAll(sources)`.

**Fix H3:** Add ownership check in `exportDeck`:

```java
if (!deck.getCreatedBy().getId().equals(userId)) {
    // Check FLASHCARD_MANAGE for admin bypass:
    // if not admin, throw
    throw new IllegalArgumentException("Cannot export another user's deck");
}
```

---

### MEDIUM — 4 issues

| # | Issue | File | Root cause |
|---|---|---|---|
| **M1** | `FlashcardReviewServiceImpl` retry doesn't re-fetch card | `FlashcardReviewServiceImpl.java:52-72` | Optimistic lock retry re-calls `doReview()` with same `card` object (stale counters). Progress is re-read, so SM-2 state is correct, but `card.correct_count` may double-increment. |
| **M2** | `batchUpdate()` saves individually | `FlashcardServiceImpl.java:112-121` | Each card gets `save()` inside a loop. Should use `saveAll()`. |
| **M3** | `listByDeck()` no deck ownership check | `FlashcardServiceImpl.java:59-64` | Any user with FLASHCARD_VIEW can view any deck's cards by ID. May be intentional (permission gate). Mark as design decision. |
| **M4** | Unused `FeatureType` import on `FlashcardDeck` | `FlashcardDeck.java:3` | Imported but never used. |

**Fix M1:** Re-fetch card in retry loop:

```java
while (true) {
    try {
        if (retries < 3) { // re-fetch on retry
            card = flashcardRepository.findByIdAndDeletedAtIsNull(flashcardId)
                .orElseThrow(...);
        }
        return doReview(card, quality, responseTimeMs, userId);
    } catch (ObjectOptimisticLockingFailureException e) { ... }
}
```

**Fix M2:** Collect then `saveAll()`.

**Fix M3:** Add comment documenting the design decision.

**Fix M4:** Remove unused import.

---

### LOW — 5 issues

| # | Issue | File | Root cause |
|---|---|---|---|
| **L1** | `getStats()` counts ALL flashcards system-wide | `FlashcardServiceImpl.java:155-156` | `flashcardRepository.count()` returns total cards across all users, not just the requesting user's cards. |
| **L2** | No `@Transactional` on `FlashcardDeckController` | `FlashcardDeckController.java:25-34` | Controller delegates to service which has `@Transactional(readOnly = true)`, so no real issue. |
| **L3** | Prompt language hardcoded to Vietnamese | `FlashcardGeneratorImpl.java:252-262` | All prompts are Vietnamese. Should be configurable via template. |
| **L4** | `Sm2Algorithm.calculate()` creates new `OffsetDateTime.now()` | `Sm2Algorithm.java:61` | Impure function — depends on system clock. Makes unit testing harder (need to account for time). |
| **L5** | CSV export vulnerable to injection | `FlashcardServiceImpl.java:190-192` | CSV escaping handles `"` but newlines in content could break row structure. |

**Fix L1:** Change to `countByCreatedByIdAndDeletedAtIsNull(userId)` for per-user stats. Better yet, make it count due cards for the user, not all cards.

**Fix L3:** Move prompt text to `PromptTemplate` in database. Already planned in C1 fix.

**Fix L4:** Pass `OffsetDateTime.now()` as parameter to `calculate()`.
**Fix L5:** Strip `\r\n` from CSV cell values or use dedicated CSV library.

---

## Architecture Verification

| Check | Status | Notes |
|---|---|---|
| Document → DocumentChunk → FlashcardDeck → Flashcard → FlashcardProgress → FlashcardReviewLog | ✅ | Correct chain |
| FlashcardDeck → AiGeneration | ✅ | FK via feature_type+feature_id |
| Flashcard → AiFeedback | ✅ | FK via feature_type+feature_id |
| No reverse dependencies | ✅ | No module imports flashcard |
| Business logic in services only | ✅ | Controllers delegate to services |
| No repository access from controllers | ✅ | All through services |

---

## Database Audit

| Entity | OK | Issues |
|---|---|---|
| `ai_generation` | ✅ Indexes, FKs, soft delete not needed | `metadata` TEXT not JSONB (H2 compat) |
| `ai_feedback` | ✅ Unique constraint, CHECK rating, indexes | No FK on feature_id (polymorphic) |
| `flashcard_deck` | ✅ Indexes, triggers, soft delete | `card_count` denormalized (stale risk) |
| `flashcard` | ✅ FKs, indexes, soft delete, triggers | Deck cascade OK |
| `flashcard_source` | ✅ Indexes | No soft delete, no FK on source_id (polymorphic) |
| `flashcard_progress` | ✅ @Version, unique constraint, soft delete, indexes | `next_review_at` NOT NULL means every card needs progress at creation |
| `flashcard_review_log` | ✅ CHECK quality 0-5, indexes | No soft delete (correct — audit trail) |

---

## SM-2 Algorithm Verification

| SM-2 Spec | Implementation | Verdict |
|---|---|---|
| Quality 0-5 input | `calculate()` validates range | ✅ |
| EF minimum 1.3 | `if (ef < 1.3) ef = 1.3` | ✅ |
| EF update formula | `ef + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))` | ✅ |
| Interval: reps=0 → 1 | Code matches | ✅ |
| Interval: reps=1 → 6 | Code matches | ✅ |
| Interval: reps≥2 → round(interval × EF) | `Math.round(interval * ef)` — uses OLD ef | ✅ (spec-compliant) |
| Failed (quality<3): reset reps=0, interval=1 | Code matches | ✅ |
| Repetitions ++ only on success | Code matches | ✅ |
| Test count | 13 tests covering all branches | ✅ |

**Deviations:** None. Implementation matches SM-2 specification exactly.

---

## Test Quality

| Test class | Tests | Coverage | Issues |
|---|---|---|---|
| `Sm2AlgorithmTest` | 13 | All quality branches, EF clamping, reset, boundaries | ✅ Complete |
| `JsonFlashcardParserTest` | 8 | Valid, empty, missing fields, malformed, null, AI refusal | ✅ Complete |
| `FlashcardGeneratorImplTest` | 4 | Disease source, document source, XOR validation, AI failure | ⚠️ Missing: empty AI response, partial success, quota exceeded |
| `FlashcardReviewServiceImplTest` | 6 | First review, EF increase, reset, mastered, non-active card | ⚠️ Missing: concurrency/optimistic lock race test |
| `FlashcardServiceImplTest` | 8 | CRUD, ownership, permissions, stats, pagination | ✅ Good coverage |
| `FlashcardControllerTest` | 5 | Auth, validation, responses | ✅ Standard coverage |
| `FlashcardReviewControllerTest` | 3 | Due cards, review, invalid quality | ✅ Standard coverage |
| `FlashcardIntegrationTest` | 2 | Persistence, soft delete | ⚠️ Thin — only `@DataJpaTest` |

**Estimated practical coverage:** ~75% of service layer, ~85% of SM-2, ~60% of controller layer. Missing: concurrent review race, AI partial return, full end-to-end pipeline.

---

## Technical Debt

| Item | Effort | Priority |
|---|---|---|
| PromptBuilder integration | 2h | Critical |
| N+1 in due cards | 30min | High |
| Batch saveAll in generator | 15min | High |
| Export ownership check | 15min | High |
| Stats scoped to user | 10min | Medium |
| Card re-fetch in retry | 10min | Medium |
| CSV newline injection | 5min | Low |
| Prompt version persisting | 5min | Critical (part of C1) |

---

## Production Readiness Score

| Dimension | Score (0-10) | Notes |
|---|---|---|
| **Architecture** | 9/10 | Clean layers, correct dependencies, one violation (PromptBuilder) |
| **Performance** | 6/10 | N+1 query, looped INSERTs, no batching |
| **Security** | 7/10 | Export IDOR, CSV injection, no prompt sanitization |
| **Maintainability** | 8/10 | Clean code, good DTO separation, weak test coverage on concurrency |
| **Test quality** | 7/10 | 28 tests, 340 total pass, missing concurrency and end-to-end |
| **Database** | 9/10 | Good schema, indexes, constraints, clean migration |
| **AI pipeline** | 5/10 | Bypasses PromptBuilder, no template version, hardcoded prompts |

**Overall:** 7.3 / 10

---

## Go / No-Go for Sprint 4

**⚠️ CONDITIONAL GO**

Sprint 4 work MAY proceed IF the two CRITICAL issues (C1, C2) are resolved first. They take ~2 hours total and fix the core architectural violation (PromptBuilder reuse + template version tracking).

The HIGH issues (H1, H2, H3) should be fixed in Sprint 4.1 before any new features are added.

**Blocking issues before Sprint 4:**
1. **Critical** — Integrate `PromptBuilder` (reuse existing infrastructure)
2. **Critical** — Persist `promptTemplateVersion` on `AiGeneration`

**Recommended fixes for Sprint 4.1 (before new features):**
3. **High** — Add `JOIN FETCH` for due cards to fix N+1
4. **High** — Replace looped `save()` with `saveAll()`
5. **High** — Add ownership check to `exportDeck`
6. **Medium** — Scope `getStats()` to user's own cards
7. **Medium** — Re-fetch card in optimistic lock retry
8. **Low** — Remove unused import, fix CSV injection

### Total fix effort: ~4 hours
