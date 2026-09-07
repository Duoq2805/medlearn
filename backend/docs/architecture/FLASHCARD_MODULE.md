# Flashcard Module

## Architecture

```
FlashcardDeck (aggregate root)  ─── AiGeneration (shared)
    │
    ├── Flashcard ─── FlashcardSource ─── DocumentChunk
    │
    └── FlashcardProgress (per-user, @Version optimistic lock)
            │
            └── FlashcardReviewLog (audit trail)
```

## Dependencies

- `ai.gateway.AiGatewayRouter` — LLM provider routing
- `ai.prompt.PromptBuilder` — template → message construction
- `ai.dto.request.AiChatRequest` — standard AI request DTO
- `ai.dto.response.AiChatResponse` — standard AI response DTO
- `ai.model.AiModel` — supported model enum
- `ai.security.AiOutputValidator` — refusal detection
- `ai.security.AiQuotaService` — token quota enforcement
- `ai.security.AiRateLimiter` — request rate limiting
- `ai.usage.AiUsageService` — async token logging
- `ai.generation.entity.AiGeneration` — shared generation metadata
- `ai.feedback.entity.AiFeedback` — shared feedback entity
- `security.CurrentUserResolver` — auth context
- `service.PermissionService` — RBAC via SpEL

## Entities

| Entity | Table | Key | Notes |
|---|---|---|---|
| FlashcardDeck | flashcard_deck | source_type + source_id | Polymorphic FK to any content source |
| Flashcard | flashcard | deck_id → FlashcardDeck | Content + aggregate counters |
| FlashcardSource | flashcard_source | flashcard_id + (source_type, source_id) | Links to DocumentChunk |
| FlashcardProgress | flashcard_progress | (flashcard_id, user_id) UNIQUE | SM-2 state per user, @Version lock |
| FlashcardReviewLog | flashcard_review_log | flashcard_id, user_id | Audit trail |

## SM-2 Review Algorithm

Pure util at `com.duoq.medlearn.util.Sm2Algorithm`. Accepts quality 0-5, returns new EF/interval/reps/nextReviewAt. Minimum EF clamped to 1.3.

| Quality | Meaning |
|---|---|
| 0 | Complete blackout |
| 1 | Incorrect, saw answer |
| 2 | Incorrect but felt easy |
| 3 | Correct with difficulty |
| 4 | Correct after hesitation |
| 5 | Perfect recall |

## API Endpoints

| Method | Path | Permission | Description |
|---|---|---|---|
| POST | /api/flashcards/generate | FLASHCARD_GENERATE | Generate from disease or document |
| GET | /api/flashcards/{id} | FLASHCARD_VIEW | Get single card |
| PUT | /api/flashcards/{id} | FLASHCARD_EDIT | Update card content |
| DELETE | /api/flashcards/{id} | FLASHCARD_DELETE | Soft delete own card |
| GET | /api/flashcards | FLASHCARD_VIEW | List own cards (paginated) |
| GET | /api/flashcards/decks | FLASHCARD_VIEW | List decks (user or admin) |
| GET | /api/flashcards/decks/{id}/cards | FLASHCARD_VIEW | List cards in a deck |
| GET | /api/flashcards/due | FLASHCARD_REVIEW | Cards due for review (SM-2) |
| POST | /api/flashcards/{id}/review | FLASHCARD_REVIEW | Submit quality rating 0-5 |
| POST | /api/flashcards/batch-update | FLASHCARD_EDIT | Batch status change |
| GET | /api/flashcards/export/{deckId} | FLASHCARD_EXPORT | CSV or JSON export |
| GET | /api/flashcards/stats | FLASHCARD_VIEW | Aggregate statistics |

## Generation Pipeline

1. Validate XOR: diseaseId OR documentId
2. Resolve source → build context text
3. Quota check + Rate limit check
4. Build system prompt + user prompt
5. AiChatRequest → AiGatewayRouter.chat()
6. AiOutputValidator.validate(raw response)
7. JsonFlashcardParser.parse(JSON) → List<ParsedCard>
8. Create FlashcardDeck + Flashcard entities
9. Save FlashcardSource links (if document)
10. Save AiGeneration record (full provenance)
11. Async AiUsageService.log()

## Permissions

| Permission | USER | REVIEWER | ADMIN |
|---|---|---|---|
| FLASHCARD_GENERATE | ✓ | ✓ | ✓ |
| FLASHCARD_VIEW | ✓ | ✓ | ✓ |
| FLASHCARD_REVIEW | ✓ | ✓ | ✓ |
| FLASHCARD_EDIT | ✓ | ✓ | ✓ |
| FLASHCARD_DELETE | ✓ | ✓ | ✓ |
| FLASHCARD_MANAGE | | ✓ | ✓ |
| FLASHCARD_EXPORT | ✓ | ✓ | ✓ |
