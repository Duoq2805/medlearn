package com.duoq.medlearn.document.service;

import com.duoq.medlearn.document.dto.response.DocumentChunkResponse;
import com.duoq.medlearn.document.dto.response.DocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse upload(MultipartFile file, String title, Long userId);

    DocumentResponse importFromUrl(String url, String title, Long userId);

    DocumentResponse getById(Long id);

    DocumentResponse getByIdAndUser(Long id, Long userId);

    Page<DocumentResponse> listByUser(Long userId, Pageable pageable);

    Page<DocumentResponse> listAll(Pageable pageable);

    List<DocumentChunkResponse> getChunks(Long documentId);

    byte[] download(Long documentId);

    void delete(Long documentId, Long userId);

    void deleteAsAdmin(Long documentId);
}
