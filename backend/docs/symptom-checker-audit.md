# Symptom Checker V1 — Verification Audit

**Date:** 2026-06-21  
**Auditor:** Automated audit  
**Engine:** `SymptomCheckerServiceImplV1`

---

## Verification Results

| # | Requirement | Result | Notes |
|---|-------------|--------|-------|
| 1 | No N+1 query issue | **✅ PASS (FIXED)** | Was N+1 per missing symptom. Now batch-fetched. |
| 2 | Only APPROVED current versions | **✅ PASS** | Native query filters by `dv.status = 'APPROVED'` |
| 3 | Soft deleted diseases excluded | **✅ PASS** | `d.deletedAt IS NULL` in all queries |
| 4 | Rejected versions excluded | **✅ PASS** | `dv.status = 'APPROVED'` implicitly excludes REJECTED |
| 5 | Archived versions excluded | **✅ PASS** | Only APPROVED versions are candidates |
| 6 | Threshold filtering (≥20%) | **✅ PASS** | Line 114: `if (score < MIN_SCORE) continue;` |
| 7 | Sorting (score DESC) | **✅ PASS** | Line 171: `Double.compare(b.score, a.score)` |
| 8 | Top 10 limit | **✅ PASS** | Line 172: `subList(0, MAX_RESULTS)` |
| 9 | MissingSymptoms correct | **✅ PASS** | All disease symptoms not in user set included |
| 10 | Score formula matches spec | **✅ PASS** | `matchedCount / totalCount × 100` exactly |

**Overall:** 10/10 PASS ✅

---

## Bug Found and Fixed During Audit

### Bug: N+1 Query in Missing Symptoms (HIGH)

**Root cause:** `SymptomCheckerServiceImplV1.java` originally called `repo.findById()` for each missing symptom individually. The `symptomNameMap` only cached user-provided symptom names, so disease-only symptoms triggered individual queries.

**Impact before fix:** Up to 50+ extra queries for 10 diseases × 5 missing symptoms each.

### Fix Applied

1. Collect all unique symptom IDs from `diseaseSymptomMap`
2. Single batch query: `symptomRepository.findAllByIdIn(allDiseaseSymptomIds)`
3. Map result → `Map<Long, String> allSymptomNameMap`
4. Both matched and missing symptoms use `allSymptomNameMap.get()` (O(1))
5. Removed the per-symptom `findSymptomNameCached()` fallback method

**Files changed:**
- `SymptomCheckerServiceImplV1.java` — replaced per-symptom lookup with batch fetch
- `SymptomCheckerServiceImplV1Test.java` — updated mocks to match new query pattern

**Result:** Exactly 4 queries, zero N+1.

---

## Correctness Verification

### Score Examples

**Disease A: 10 symptoms, User matches 2**
```
score = 2 / 10 × 100 = 20.0%
Result: INCLUDED (≥ 20% threshold)
```

**Disease B: 4 symptoms, User matches 2**
```
score = 2 / 4 × 100 = 50.0%
```

Both produce exact expected scores ✅

---

## Complexity Analysis

| Metric | Claimed | Actual (after fix) | Verified |
|--------|---------|-------------------|----------|
| Query count | 4 | 4 | ✅ No N+1 |
| Time | O(n log n) | O(n log n) | ✅ Sorting dominates |
| Memory | O(candidates) | ~100 entries × (ID + name + mappings) | ✅ |

**Query breakdown (fixed):**
1. `findMatchingDiseasesRaw()` — native candidate query (1)
2. `findTotalSymptomCountBatch()` — batch count (1)
3. `findDiseaseSymptomMappings()` — batch mappings (1)
4. `symptomRepository.findAllByIdIn()` — batch names (1)

**Total: 4 queries, regardless of input size.** ✅

---

## Summary

| Severity | Issue | Status |
|----------|-------|--------|
| 🔴 HIGH | N+1 query for missing symptom names | ✅ **FIXED** — replaced with batch fetch |
| 🟢 ALL | 10 verification requirements | ✅ **10/10 PASS** |
| 🟢 ALL | Test suite | ✅ **58/58 PASS** |

