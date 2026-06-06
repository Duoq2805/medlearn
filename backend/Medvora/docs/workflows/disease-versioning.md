# Disease Versioning Workflow

Disease content supports version tracking with immutable history.

## Core Entities
- Disease (metadata + currentVersion pointer)
- DiseaseVersion (versioned content + status + reviewer metadata)
- DiseaseSection (content sections tied to version)
- DiseaseVersionSymptom (symptom mappings tied to version)

## State Machine
```
DRAFT → PENDING_REVIEW → APPROVED → ARCHIVED
                      ↘ REJECTED
```

Transitions enforced via `VersionStatus.canTransitionTo()` enum method.

## Versioning Flow

### Create Disease
1. Create Disease entity (no currentVersion set)
2. Create initial DRAFT version
3. Contributor edits draft sections/symptoms
4. Submit for review → PENDING_REVIEW

### Approve Version
1. Reviewer evaluates PENDING_REVIEW version
2. Pessimistic lock acquired on Disease row
3. Previous APPROVED versions archived atomically
4. New version approved + set as currentVersion
5. Only APPROVED versions can be currentVersion

### Rollback to Previous Approved
1. Reviewer selects target APPROVED version
2. Pessimistic lock acquired on Disease row
3. NEW snapshot created (not reusing target entity)
4. Snapshot inherits target's sections + symptoms (deep clone)
5. Snapshot marked APPROVED + set as currentVersion
6. Previous currentVersion archived
7. Target version remains immutable in history

### Clone for Editing
1. Contributor clones current APPROVED version
2. New DRAFT version created
3. All sections + symptoms deep-cloned to draft
4. Contributor edits draft
5. Submit for review → PENDING_REVIEW

## Versioning Goals
- preserve immutable history
- support moderation workflow
- prevent accidental data loss
- maintain educational consistency
- enable audit trail

## Important Rules
- Disease.currentVersion MUST reference APPROVED version only
- Draft versions never become currentVersion
- Rollback creates new snapshot (immutable history preserved)
- Clone includes complete section + symptom graph
- All transitions validated via enum state machine
- Pessimistic locking prevents concurrent approval races