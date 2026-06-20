# Symptom Checker V1 — API Documentation

**Endpoint:** `POST /api/symptom-checker/analyze`  
**Auth Required:** Yes (JWT Bearer token)  
**Permission:** Authenticated user  
**Content-Type:** `application/json`

---

## Request

```json
POST /api/symptom-checker/analyze
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "symptomIds": [1, 2, 3]
}
```

### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `symptomIds` | `List<Long>` | Yes | Array of symptom IDs to match against diseases |

### Validation

- If `symptomIds` is null or empty → returns empty list (200)
- Duplicate IDs are tolerated (treated as single match)
- Non-existent IDs are ignored (no match contribution)

---

## Response

### Success (200)

```json
{
  "success": true,
  "message": "Symptom analysis completed",
  "data": [
    {
      "diseaseId": 1,
      "diseaseName": "Influenza",
      "matchScore": 87.5,
      "matchedSymptoms": [
        { "id": 1, "name": "Fever" },
        { "id": 2, "name": "Cough" },
        { "id": 3, "name": "Fatigue" }
      ],
      "missingSymptoms": [
        { "id": 4, "name": "Headache" }
      ],
      "explanation": "Matched 3 of 4 known symptoms"
    },
    {
      "diseaseId": 2,
      "diseaseName": "Common Cold",
      "matchScore": 66.7,
      "matchedSymptoms": [
        { "id": 1, "name": "Fever" },
        { "id": 2, "name": "Cough" }
      ],
      "missingSymptoms": [
        { "id": 5, "name": "Sneezing" }
      ],
      "explanation": "Matched 2 of 3 known symptoms"
    }
  ],
  "timestamp": "2026-06-21T10:30:45.123456"
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `diseaseId` | `Long` | Unique disease identifier |
| `diseaseName` | `String` | Display name of the disease |
| `matchScore` | `Double` | Match percentage (0.0–100.0), rounded to 1 decimal |
| `matchedSymptoms` | `List<SymptomInfo>` | User symptoms that match this disease |
| `missingSymptoms` | `List<SymptomInfo>` | Disease symptoms the user did NOT select |
| `explanation` | `String` | Human-readable match description |

### SymptomInfo

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Symptom ID |
| `name` | `String` | Symptom display name |

---

## Business Rules

| Rule | Value | Description |
|------|-------|-------------|
| **Minimum Score** | ≥ 20% | Diseases below 20% match are excluded |
| **Max Results** | 10 | At most 10 diseases returned |
| **Sort Order** | Score DESC | Highest match first |
| **Version Filter** | APPROVED only | DRAFT, REJECTED, ARCHIVED excluded |
| **Soft Delete** | Excluded | Both disease and version must not be soft-deleted |
| **Current Version** | Required | Only disease's `current_version_id` is checked |

### Score Formula

```
score = count(matchedSymptoms) / count(allDiseaseSymptoms) × 100

Example:
  Disease symptoms: [Fever, Cough, Fatigue, Headache] = 4
  User symptoms:    [Fever, Cough]                     = 2
  Score: 2 / 4 × 100 = 50.0%
```

---

## Error Responses

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Authentication failed: Full authentication is required...",
  "data": null
}
```

### 500 Server Error
```json
{
  "success": false,
  "message": "An unexpected error occurred. Please try again later.",
  "data": null
}
```

---

## Examples

### cURL
```bash
curl -X POST http://localhost:6061/api/symptom-checker/analyze \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{"symptomIds": [1, 2, 3]}'
```

### JavaScript (Axios)
```javascript
const response = await axios.post(
  '/api/symptom-checker/analyze',
  { symptomIds: [1, 2, 3] },
  { headers: { Authorization: `Bearer ${token}` } }
);

const results = response.data.data;
results.forEach(entry => {
  console.log(`${entry.diseaseName}: ${entry.matchScore}%`);
  console.log(`  Matched: ${entry.matchedSymptoms.map(s => s.name).join(', ')}`);
  console.log(`  Missing: ${entry.missingSymptoms.map(s => s.name).join(', ')}`);
});
```

---

## Rate Limits

- Same as general API rate limits (configured in `application.yml`)
- No separate rate limit for this endpoint

---

## Comparison: V1 vs Legacy `/check`

| Feature | Legacy `/check` | V1 `/analyze` |
|---------|-----------------|----------------|
| Endpoint | `POST /check` | `POST /analyze` |
| Request | `List<Long>` (raw array) | `{ "symptomIds": [...] }` |
| Score Formula | Weighted (0.6×count + 0.4×weight) | Simple ratio: matched/total × 100 |
| Missing Symptoms | Not included | Included |
| Explanation | Not included | "Matched X of Y symptoms" |
| Min Score Filter | Not applied | ≥ 20% |
| Max Results | Configurable (default 10) | Fixed at 10 |
| Sorting | By weight DESC | By score DESC |
