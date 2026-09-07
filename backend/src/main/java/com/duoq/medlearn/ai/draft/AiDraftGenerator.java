package com.duoq.medlearn.ai.draft;

import com.duoq.medlearn.draft.dto.response.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.request.AiDraftRequest;

public interface AiDraftGenerator {

    DiseaseDraftResponse generate(AiDraftRequest request, Long userId);
}
