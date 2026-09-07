package com.duoq.medlearn.document.service.impl;

import com.duoq.medlearn.document.dto.response.DocumentChunkResponse;
import com.duoq.medlearn.document.dto.response.DocumentResponse;
import com.duoq.medlearn.document.entity.Document;
import com.duoq.medlearn.document.entity.DocumentChunk;
import com.duoq.medlearn.document.exception.DocumentProcessingException;
import com.duoq.medlearn.document.mapper.DocumentMapper;
import com.duoq.medlearn.document.repository.DocumentChunkRepository;
import com.duoq.medlearn.document.repository.DocumentRepository;
import com.duoq.medlearn.document.service.DocumentService;
import com.duoq.medlearn.document.storage.LocalFileStorageService;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final LocalFileStorageService fileStorageService;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentResponse upload(MultipartFile file, String title, Long userId) {
        var checksum = fileStorageService.computeChecksum(file);
        var storagePath = fileStorageService.store(file);
        var mimeType = file.getContentType();

        var document = Document.builder()
                .title(title != null ? title : file.getOriginalFilename())
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(mimeType)
                .storagePath(storagePath)
                .checksum(checksum)
                .status("UPLOADED")
                .createdBy(User.builder().id(userId).build())
                .build();

        var saved = documentRepository.save(document);
        log.info("Document uploaded: id={}, title={}, size={}", saved.getId(), saved.getTitle(), saved.getFileSize());
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentResponse importFromUrl(String url, String title, Long userId) {
        try {
            var urlObj = new URL(url);
            var connection = urlObj.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            var data = connection.getInputStream().readAllBytes();

            var filename = Path.of(urlObj.getPath()).getFileName().toString();
            if (filename.isBlank()) filename = "imported_" + System.currentTimeMillis();

            var storagePath = fileStorageService.storeFromUrl(data, filename);

            var document = Document.builder()
                    .title(title != null ? title : filename)
                    .fileName(filename)
                    .fileSize((long) data.length)
                    .storagePath(storagePath)
                    .sourceUrl(url)
                    .status("UPLOADED")
                    .createdBy(User.builder().id(userId).build())
                    .build();

            var saved = documentRepository.save(document);
            log.info("Document imported from URL: id={}, url={}", saved.getId(), url);
            return documentMapper.toResponse(saved);
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to import from URL: " + url, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getById(Long id) {
        var doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        return documentMapper.toResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getByIdAndUser(Long id, Long userId) {
        var doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        return documentMapper.toResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> listByUser(Long userId, Pageable pageable) {
        return documentRepository.findAllByCreatedByIdAndDeletedAtIsNull(userId, pageable)
                .map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> listAll(Pageable pageable) {
        return documentRepository.findAllByDeletedAtIsNull(pageable)
                .map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentChunkResponse> getChunks(Long documentId) {
        return documentChunkRepository.findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(documentId)
                .stream()
                .map(documentMapper::toChunkResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(Long documentId) {
        var doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        return fileStorageService.read(doc.getStoragePath());
    }

    @Override
    @Transactional
    public void delete(Long documentId, Long userId) {
        var doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        if (!doc.getCreatedBy().getId().equals(userId)) {
            throw new DocumentProcessingException("Cannot delete another user's document");
        }
        doc.setDeletedAt(OffsetDateTime.now());
        documentRepository.save(doc);
        log.info("Document soft deleted: id={}, userId={}", documentId, userId);
    }

    @Override
    @Transactional
    public void deleteAsAdmin(Long documentId) {
        var doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
        doc.setDeletedAt(OffsetDateTime.now());
        documentRepository.save(doc);
        log.info("Document soft deleted by admin: id={}", documentId);
    }
}
