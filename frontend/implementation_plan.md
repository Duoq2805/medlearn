# Frontend ↔ Backend Feature Parity Audit for Medlearn

## User Review Required

This audit will cover the entire Medlearn project, focusing on making the frontend match the backend exactly.
We will not modify the backend unless there is a genuine bug.

## Open Questions

- Are there any backend endpoints that are not documented in the code (e.g., via annotations) that we might miss?
- Should we consider the OpenAPI spec as the source of truth, or just the controller code?

## Proposed Changes

We will proceed by:

1. Auditing the backend controllers, services, and DTOs to build a complete API inventory.
2. Auditing the frontend API services and pages/components to see what is currently implemented.
3. Comparing and noting mismatches.
4. Fixing the frontend to match the backend.

### Component: Disease Management

We will start with the Disease Management feature as it is complex and central.

#### [MODIFY] [disease.ts](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/api/disease.ts)
#### [MODIFY] [diseaseDraft.ts](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/api/diseaseDraft.ts)
#### [MODIFY] [diseaseSection.ts](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/api/diseaseSection.ts)
#### [MODIFY] [CreateDiseasePage.tsx](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/disease/CreateDiseasePage.tsx)
#### [MODIFY] [EditDiseasePage.tsx](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/disease/EditDiseasePage.tsx)
#### [MODIFY] [MyDraftsPage.tsx](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/disease/MyDraftsPage.tsx)
#### [MODIFY] [DiseaseDetailPage.tsx](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/disease/DiseaseDetailPage.tsx)

### Component: Case Study

#### [MODIFY] [case.ts](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/api/case.ts)
#### [MODIFY] [pages/case/*](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/cases/)

### Component: Symptom Checker

#### [MODIFY] [symptom.ts](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/api/symptom.ts)
#### [MODIFY] [pages/symptom-checker/*](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/symptom-checker/)

### Component: Auth

#### [MODIFY] [auth.ts](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/api/auth.ts)
#### [MODIFY] [pages/auth/*](file:///d:/PersonalProject/medlearn_FirstVersion/medlearn/frontend/src/pages/auth/)

### Component: API Response Wrapper Audit

We will audit every API call in the frontend for incorrect assumptions about response wrapping.

### Component: TypeScript Types / DTO Parity

We will update the frontend types in `src/types` to match the backend DTOs.

## Verification Plan

### Automated Tests
- Run backend tests to ensure we didn't break anything.
- Run frontend tests (if any) and build the frontend.

### Manual Verification
- Start the backend and frontend.
- Test the main workflows:
  - Disease: Create → Edit → Save Draft → Reload → Preview → Submit
  - My Drafts: Create Draft → My Drafts → Open → Edit → Save → Reload
  - Case Study: Create/List/Detail/Edit/Save/Submit/Answer (according to backend support)
  - Symptom Checker: Input → API → Result → Error/Empty states
  - Auth: Login → Me → role/permission protected pages.
