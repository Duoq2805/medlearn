# Symptom Checker V2 API

## Endpoint
`POST /api/symptom-checker/v2/analyze`

## Request
Same request DTO as V1.

```json
{
  "symptomIds": [1, 2, 3],
  "limit": 10
}
```

## Response
Wrapped in `ApiResponse<List<DiseaseMatchResultV2DTO>>`.

```json
{
  "success": true,
  "message": "Symptom analysis V2 completed",
  "data": [
    {
      "diseaseId": 1,
      "diseaseName": "Influenza",
      "matchScore": 72.7,
      "confidence": 78,
      "severity": "NORMAL",
      "severityExplanation": "No deterministic red flag rule matched.",
      "clinicalExplanation": "Patient matches 4 of 5 characteristic symptoms. Critical symptoms are present. Confidence is high.",
      "matchedSymptoms": [{ "id": 1, "name": "Fever" }],
      "missingSymptoms": [{ "id": 2, "name": "Headache" }],
      "recommendedNextSymptoms": [{ "id": 3, "name": "Muscle pain" }],
      "matchedCriticalSymptoms": [{ "id": 1, "name": "Fever" }],
      "missingCriticalSymptoms": [{ "id": 4, "name": "Shortness of breath" }]
    }
  ],
  "timestamp": "2026-07-03T00:00:00Z"
}
```

## DTO Fields
- `diseaseId`: disease id
- `diseaseName`: disease name
- `matchScore`: weighted matched percentage 0-100
- `confidence`: deterministic confidence 0-100
- `severity`: `NORMAL | MODERATE | URGENT | EMERGENCY`
- `severityExplanation`: red flag explanation
- `clinicalExplanation`: deterministic reasoning text
- `matchedSymptoms`: selected symptoms present in disease profile
- `missingSymptoms`: characteristic symptoms not selected
- `recommendedNextSymptoms`: top 3 unmatched weighted symptoms
- `matchedCriticalSymptoms`: matched critical symptoms
- `missingCriticalSymptoms`: critical symptoms not yet selected

## Notes
- V1 remains at `/api/symptom-checker/analyze`
- Request validation unchanged: `symptomIds` must be non-empty
