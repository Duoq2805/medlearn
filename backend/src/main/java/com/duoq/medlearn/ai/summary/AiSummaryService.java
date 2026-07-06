package com.duoq.medlearn.ai.summary;

import com.duoq.medlearn.ai.dto.response.SummaryResponse;
import com.duoq.medlearn.ai.dto.request.SummaryRequest;

import java.util.List;

public interface AiSummaryService {

    SummaryResponse generate(Long diseaseId, SummaryRequest request, Long userId);

    List<SummaryResponse> listByDisease(Long diseaseId);

    SummaryResponse getLatest(Long diseaseId, String summaryType);

    SummaryResponse getByVersion(Long diseaseId, String summaryType, Integer version);
}
