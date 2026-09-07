package com.duoq.medlearn.draft.service;

import com.duoq.medlearn.draft.dto.response.DiseaseDraftResponse;

public interface DraftLifecycleService {

    DiseaseDraftResponse submitForReview(Long draftId, Long userId);

    DiseaseDraftResponse approve(Long draftId, Long reviewerId, String note);

    DiseaseDraftResponse reject(Long draftId, Long reviewerId, String reason);

    DiseaseDraftResponse archive(Long draftId, Long userId);

    void applyToDisease(Long draftId, Long userId);

    DiseaseDraftResponse cloneDraft(Long draftId, Long userId);
}
