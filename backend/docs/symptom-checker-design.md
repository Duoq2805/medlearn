# Symptom Checker V1 — Design Document

**Version:** 1.0  
**Status:** Implemented  
**Date:** 2026-06-21

---

## Architecture

```
┌──────────────────┐     ┌──────────────────────────┐
│ Client (Frontend)│────▶│ SymptomCheckerController  │
│ POST /analyze    │     │ POST /symptom-checker/    │
│ {symptomIds}     │     │   analyze                 │
└──────────────────┘     └───────────┬──────────────┘
                                     │
                                     ▼
                      ┌──────────────────────────────┐
                      │  SymptomCheckerServiceImplV1  │
                      │  (Deterministic Engine)       │
                      ├──────────────────────────────┤
                      │ 1. findMatchingDiseasesRaw()  │
                      │ 2. findTotalSymptomCountBatch │
                      │ 3. findDiseaseSymptomMappings │
                      │ 4. Score = matched / total    │
                      │ 5. Filter ≥ 20%, sort DESC   │
                      └──────────┬───────────────────┘
                                 │
                     ┌───────────┴───────────┐
                     ▼                       ▼
          ┌──────────────────┐   ┌──────────────────────┐
          │ DiseaseVersion   │   │ Symptom Repository   │
          │ Symptom Repo     │   │ (name lookup)        │
          └──────────────────┘   └──────────────────────┘
```

## Algorithm

### Input
```json
{ "symptomIds": [1, 2, 3] }
```

### Steps

**Step 1 — Candidate Discovery**
```sql
-- Native query: find all diseases with at least 1 matching symptom
SELECT d.id, d.name, d.slug,
       COUNT(DISTINCT dvs.symptom_id) as matchCount,
       SUM(dvs.weight_score) as totalWeight
FROM disease_version dv
JOIN disease d ON d.id = dv.disease_id
JOIN disease_version_symptom dvs ON dvs.disease_version_id = dv.id
WHERE dvs.symptom_id IN (:symptomIds)
  AND dv.id = d.current_version_id        -- Only current version
  AND dv.status = 'APPROVED'              -- Only approved
  AND dv.deleted_at IS NULL               -- Not soft-deleted
  AND d.deleted_at IS NULL
GROUP BY d.id, d.name, d.slug
HAVING COUNT(DISTINCT dvs.symptom_id) > 0
ORDER BY totalWeight DESC, matchCount DESC
LIMIT :limit
```

- Filter: APPROVED version, current version only
- Exclude: DRAFT, REJECTED, ARCHIVED, soft-deleted
- Limit: 100 candidates in raw query, later narrowed to 10

**Step 2 — Batch Count**
```sql
-- Single query for all candidates
SELECT d.id, COUNT(dvs)
FROM Disease d
JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = d.currentVersion.id
WHERE d.id IN :candidateIds
  AND d.currentVersion IS NOT NULL
  AND d.deletedAt IS NULL
GROUP BY d.id
```

**Step 3 — Batch Mappings**
```sql
-- Single query for all disease-to-symptom mappings
SELECT d.id, dvs.symptom.id
FROM Disease d
JOIN DiseaseVersionSymptom dvs ON dvs.diseaseVersion.id = d.currentVersion.id
WHERE d.id IN :candidateIds
  AND d.currentVersion IS NOT NULL
  AND d.deletedAt IS NULL
```

**Step 4 — Score Calculation**
```
matchedSymptoms = intersection(userIds, diseaseSymptomIds)
missingSymptoms = diseaseSymptomIds - userIds
score = (matchedSymptoms.size / diseaseSymptomIds.size) × 100

Example:
  Disease has: [fever, cough, fatigue, headache]  = 4
  User has:    [fever, cough]                      = 2
  Score:       2 / 4 × 100 = 50%
```

**Step 5 — Filter & Sort**
- Remove entries with score < 20%
- Sort by score DESC
- Return top 10

---

## Complexity Analysis

### Time Complexity: O(n log n)

| Step | Complexity | Notes |
|------|------------|-------|
| Candidate query | O(c) | Database-indexed query, c = candidate count |
| Batch count | O(c) | Single query, c = candidate count |
| Batch mappings | O(m) | m = total disease-symptom mappings |
| In-memory score | O(m × s) | For each mapping, O(1) hash lookup |
| Sort | O(k log k) | k ≤ 10, effectively constant |
| **Total** | **O(n log n)** | n = total symptoms across all diseases |

### Query Count: Exactly 4 (no N+1)

| # | Query | Result Type |
|---|-------|-------------|
| 1 | `findMatchingDiseasesRaw` | Native query → candidate diseases |
| 2 | `findTotalSymptomCountBatch` | Batch → disease ID → count map |
| 3 | `findDiseaseSymptomMappings` | Batch → disease ID → symptom IDs |
| 4 | `symptomRepository.findAllByIdIn` | Batch → symptom ID → name map |

**No N+1 problem.** All disease data fetched in 3 batch queries total.

### Memory
- Candidate disease IDs: ≤ 100 entries
- Symptom mappings: ≤ 500 entries (avg 5 symptoms × 100 diseases)
- All in-memory processing uses O(1) hash lookups

---

## Data Model

```
Disease (1) ──────────▶ DiseaseVersion (1) ◀── current_version_id
                              │
                              │ (1:N)
                              ▼
                   DiseaseVersionSymptom
                              │
                              │ (N:1)
                              ▼
                         Symptom
```

### Key Constraints
- `disease_version_symptom.disease_version_id` must point to disease's `current_version_id`
- Only `status = 'APPROVED'` versions are considered
- Soft-deleted rows excluded via `@SQLRestriction("deleted_at IS NULL")`

---

## Edge Cases Handled

| Case | Behavior | Test Coverage |
|------|----------|---------------|
| No symptoms input | Return empty list | ✅ |
| Null input | Return empty list | ✅ |
| No matching diseases | Return empty list | ✅ |
| Below 20% threshold | Excluded from results | ✅ |
| Above threshold | Included, sorted by score | ✅ |
| More than 10 results | Limited to 10 | ✅ |
| Exact match (all symptoms) | Score = 100% | ✅ |
| Partial match (some symptoms) | Score = matched/total × 100 | ✅ |
| Draft disease version | Not included in candidates | ✅ |
| Archived disease version | Not included in candidates | ✅ |
| No currentVersion set | Not included in candidates | ✅ |
| User symptom not in any disease | Ignored (no candidate) | ✅ |

---

## Scoring History (Future V2)

V1 uses simple `matched/total × 100`. Future enhancements:

### V2 Proposal: Weighted Score
```
score = Σ(weight(matched)) / Σ(weight(all)) × 100
```
- Each symptom gets a `weight_score` (unique to disease)
- High-specificity symptoms (e.g., "Rash" for measles) have higher weight
- Requires client to provide symptoms with confidence values

### V3 Proposal: Disease Prevalence
```
score = matchScore × prevalenceMultiplier
```
- Common diseases weighted down
- Rare diseases get slight boost
- Prevents "cold" from always dominating results

---

## Security & Safety

- **No AI/LLM used** — fully deterministic, auditable
- **No medical advice** — engine is a ranker, not a diagnostic tool
- **No sensitive data** — symptom and disease names only
- **Authorization required** — all endpoints require JWT + authenticated user

---

## Future Improvements

| Item | Priority | Effort |
|------|----------|--------|
| Boolean search (AND/OR toggle) | Low | 1 day |
| Age/gender filtering | Medium | 2 days |
| Weighted symptoms per disease | Medium | 3 days |
| Paginated results (for >10) | Low | 0.5 day |
| Cache disease-symptom mappings | Low | 1 day |
| Exclude user-selected diseases | Low | 0.5 day |
