# Symptom Checker V2 Design

## Goal
Build deterministic Symptom Checker V2 beside V1.

## Flow
Patient Symptoms -> Candidate Diseases -> Weighted Clinical Ranking -> Confidence Score -> Clinical Explanation -> Recommended Next Symptoms -> Red Flag Detection.

## Data Source
Use approved `Disease.currentVersion` only.
Exclude deleted diseases, deleted versions, draft versions, rejected versions, archived versions.

## Query Plan
1. `findMatchingDiseasesRaw(symptomIds, limit)` for candidate diseases.
2. `findDiseaseSymptomDetailsForV2(diseaseIds)` for all candidate symptom details.

No query inside loops. Happy-path query count: 2.

## Ranking
Sort by:
1. confidence DESC
2. matchedSymptoms DESC
3. averageWeight DESC
4. diseaseName ASC

## Confidence Formula
Confidence range: 0-100.

Components:
- symptom coverage: matched weight / total disease weight -> 55%
- average symptom weight: avg matched weight normalized by max disease weight -> 20%
- matched critical symptoms: matched critical / total critical -> 15%
- disease specificity: 1 / sqrt(total symptoms) -> 10%

Critical symptom rule:
- symptom weight >= max(3.0, 80% of disease max weight)

Threshold:
- include candidate when confidence >= 20

## Clinical Explanation
Structured deterministic text:
- matched symptom count
- total characteristic symptom count
- critical symptom presence
- confidence band: high / moderate / low

## Recommended Next Symptoms
For each candidate disease:
- take unmatched symptoms
- sort by weight DESC, name ASC
- return top 3

## Red Flags
Rules:
- `Chest pain` + `Shortness of breath` -> `EMERGENCY`
- `Loss of consciousness` -> `EMERGENCY`
- `Hemoptysis` or `Coughing blood` -> `URGENT`
- >= 8 user symptoms -> `MODERATE`
- else `NORMAL`

## Complexity
Let C = candidate diseases, M = total candidate disease symptoms.
- time: O(M log M) worst-case due to recommendation sorting per disease, practical bounded by candidate size
- memory: O(M)

## Compatibility
- V1 endpoint unchanged: `/api/symptom-checker/analyze`
- V2 endpoint added: `/api/symptom-checker/v2/analyze`
