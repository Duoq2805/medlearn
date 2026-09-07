# BUSINESS_WORKFLOW.md

**Ngày:** 2026-08-07. Mô tả business flow thực tế (sau khi hoàn thiện).

## 1. Disease → Version → User (pipeline chính)

```
POST /api/diseases {name, slug, categoryId}
  └─ DiseaseServiceImpl.createDisease()
      ├─ validate name/slug unique
      ├─ tạo Disease (chưa currentVersion)
      └─ DiseaseVersionService.createDraftVersion() → version #1, DRAFT, rỗng
              ↓
Contributor thêm nội dung:
  POST /api/versions/{id}/sections  (DiseaseSectionService.createSection — sanitize HTML)
  hoặc POST /api/versions/clone/{diseaseId} (clone approved → DRAFT mới)
              ↓
POST /api/versions/{id}/submit  (DiseaseVersionServiceImpl.submitForReview)
  ├─ validateRequiredSections: bắt buộc SectionType "Definition","Symptoms","Treatment"
  ├─ validateWorkflowTransition DRAFT→PENDING_REVIEW
  └─ validateVersionOwnership (creator hoặc SECTION_EDIT_ANY)
              ↓
POST /api/versions/{id}/approve  (DiseaseVersionServiceImpl.approveVersion)
  ├─ validateReviewerPermission (VERSION_REVIEW)
  ├─ chặn reviewer == creator
  ├─ diseaseVersionRepository.findByIdForUpdate (lock)
  ├─ archiveApprovedVersionsExcept (các version APPROVED cũ → ARCHIVED)
  ├─ status APPROVED, reviewedBy/At set
  └─ disease.currentVersion = version   ★
              ↓
GET /api/diseases/{slug} (public)
  └─ buildDiseaseDetail: dùng disease.currentVersion → sections → render cho User
```

**Rollback:** `POST /api/versions/rollback?diseaseId=&targetVersionId=` — tạo snapshot mới từ target, set currentVersion, archive các approved khác (DiseaseWorkflowServiceImpl.rollback).

**Reject:** `POST /api/versions/{id}/reject` — status REJECTED, `canTransitionTo` cho phép REJECTED→DRAFT (resubmit).

## 2. DiseaseDraft → Version (pipeline draft — đã hoàn thiện)

```
POST /api/drafts {diseaseId?, title, sections[]}   (DraftService.create — manual)
hoặc POST /api/ai/drafts {diseaseId?, documentId?, sections[]}  (AiDraftGeneratorImpl — AI)
  └─ DiseaseDraft (disease nullable, sourceMethod MANUAL/AI_FULL, sourceDocument)
  └─ DiseaseDraftSection[] (aiGenerated flag)
              ↓
POST /api/drafts/{id}/submit  (DraftLifecycleServiceImpl.submitForReview)
  ├─ cho phép từ DRAFT hoặc REJECTED  ★ (bổ sung resubmit)
  └─ status → PENDING_REVIEW
              ↓
POST /api/drafts/{id}/approve  (DraftLifecycleServiceImpl.approve)
  ├─ require DRAFT_REVIEW permission
  ├─ status → APPROVED, reviewedBy/At + note
  └─ (reject tương tự → REJECTED)
              ↓
POST /api/drafts/{id}/apply  (DraftLifecycleServiceImpl.applyToDisease)  ★ IMPLEMENTED
  ├─ require APPROVED
  ├─ require draft.disease != null (nếu null → IllegalStateException)
  ├─ tạo DiseaseVersion mới:
  │     versionNumber = max+1, status = APPROVED, createdBy = actor
  ├─ clone DraftSection → DiseaseSection (map DraftSectionType → SectionType bằng lowercase name;
  │     nếu SectionType chưa tồn tại → null, chấp nhận nhưng validateRequiredSections sẽ chặn khi submit)
  ├─ archiveApprovedVersionsExcept (archive version cũ)
  ├─ disease.currentVersion = version mới
  ├─ draft → ARCHIVED (đã tiêu thụ)
  └─ audit VERSION_APPROVED (sourceDraftId)
              ↓
User đọc qua currentVersion (giống pipeline chính)
```

## 3. Quyền theo actor

| Actor | Disease | Version | Draft |
|---|---|---|---|
| Contributor | tạo (tự động version #1), update metadata (owner), soft delete (owner) | tạo draft, clone, edit draft (owner), submit | tạo manual, edit (owner), submit, clone |
| Reviewer | — | approve/reject/archive/rollback (VERSION_REVIEW), xem pending queue | approve/reject (DRAFT_REVIEW) |
| Admin | manage mọi thứ, restore | archive/delete/restore | delete/archive |

## 4. State machines

```
DraftStatus:  DRAFT → PENDING_REVIEW → APPROVED → (apply) → ARCHIVED
                       ↘ REJECTED → (submit lại) → PENDING_REVIEW
                       
VersionStatus: DRAFT → PENDING_REVIEW → APPROVED → ARCHIVED
                       ↘ REJECTED → (edit) → DRAFT → PENDING_REVIEW
```

## 5. Ghi chú ràng buộc

- **validateRequiredSections** áp cho `submit` version: SectionType bắt buộc Definition/Symptoms/Treatment. Draft không có gate này — draft có thể submit thiếu section, nhưng apply sẽ tạo version với SectionType null → **version submit sau đó bị chặn**. → Khuyến nghị: thêm gate tương tự cho draft submit (TODO).
- Draft không disease → không apply được (chặn rõ ràng). Có thể bind disease lúc tạo draft (`diseaseId` optional nhưng apply bắt buộc).
- `DiseaseDraft.approve` không tự động apply — bước apply tách riêng, người dùng có thể tạo nhiều version từ cùng draft (chỉ draft đầu tiên ARCHIVED, các lần sau fail vì không còn APPROVED).