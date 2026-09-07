# Session Summary — Sprint 3

## Flashcard Module

### Created

**36 new files** across the `flashcard/` module and shared infrastructure:

| Layer | Files |
|---|---|
| AI shared entities | `FeatureType`, `AiGeneration`, `AiFeedback`, `AiGenerationRepository`, `AiFeedbackRepository` |
| Enums | `FlashcardStatus`, `FlashcardDifficulty`, `SourceType` |
| Entities | `FlashcardDeck`, `Flashcard`, `FlashcardSource`, `FlashcardProgress`, `FlashcardReviewLog` |
| Repositories | All 5 JPA repositories with custom queries |
| DTOs | 4 request + 7 response DTOs |
| Mapper | `FlashcardMapper` (MapStruct) |
| Utils | `Sm2Algorithm`, `JsonFlashcardParser` |
| Services | `FlashcardGenerator`, `FlashcardService`, `FlashcardReviewService` + impls |
| Controllers | `FlashcardController`, `FlashcardDeckController`, `FlashcardReviewController` |
| Migration | `V3_0__create_flashcard_module.sql` |
| Tests | 28 new tests across 7 test classes |

### Modified

- `PermissionCode.java` — 7 new flashcard permissions
- `FlashcardProgress.java` — `@Version` optimistic lock on `version` field

### Test Results

- Total tests: **340** passed, 0 failed, 0 errors
- All existing tests continue to pass
- SM-2 algorithm: 13 unit tests covering all quality branches
- Service: 14 tests covering CRUD, permissions, review flow, AI failures
- Controller: 8 tests covering auth, validation, permissions

### Architecture Decisions

- FlashcardDeck is aggregate root with polymorphic `source_type + source_id`
- AiGeneration is shared entity reused across all AI features
- AiFeedback is generic (supports Summary/Flashcard/Quiz/CaseStudy/Chat)
- SM-2 algorithm in pure utility class (no Spring dependency)
- `@Version` optimistic locking on FlashcardProgress
- Synchronous generation (async planned for v2)

### Remaining for Sprint 4

1. Deck controller: regeneration endpoint, deck merge/split
2. AiFeedback controller + integration
3. FlashcardDeckController: deck CRUD (update/delete deck metadata)
4. Frontend integration
5. Performance optimization (batch inserts for generation)
6. Anki APKG export format
7. Scheduled review reminders
8. Prompt template `flashcard-gen` seed data registration
