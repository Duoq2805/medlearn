package com.duoq.medlearn.ai.draft;

import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.AiDraftRequest;

public interface AiDraftGenerator {

    DiseaseDraftResponse generate(AiDraftRequest request, Long userId);
}
