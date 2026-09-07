package com.duoq.medlearn.draft.service;

import com.duoq.medlearn.draft.dto.request.CreateDraftRequest;
import com.duoq.medlearn.draft.dto.response.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.request.UpdateDraftRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DraftService {

    DiseaseDraftResponse create(CreateDraftRequest request, Long userId);

    DiseaseDraftResponse getById(Long id);

    Page<DiseaseDraftResponse> listByUser(Long userId, Pageable pageable);

    Page<DiseaseDraftResponse> listByDisease(Long diseaseId, Pageable pageable);

    DiseaseDraftResponse update(Long id, UpdateDraftRequest request, Long userId);

    void delete(Long id, Long userId);
}
