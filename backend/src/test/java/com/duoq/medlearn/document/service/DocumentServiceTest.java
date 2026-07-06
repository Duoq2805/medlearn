package com.duoq.medlearn.document.service;

import com.duoq.medlearn.document.dto.DocumentChunkResponse;
import com.duoq.medlearn.document.dto.DocumentResponse;
import com.duoq.medlearn.document.entity.Document;
import com.duoq.medlearn.document.entity.DocumentChunk;
import com.duoq.medlearn.document.exception.DocumentProcessingException;
import com.duoq.medlearn.document.mapper.DocumentMapper;
import com.duoq.medlearn.document.repository.DocumentChunkRepository;
import com.duoq.medlearn.document.repository.DocumentRepository;
import com.duoq.medlearn.document.service.impl.DocumentServiceImpl;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentChunkRepository documentChunkRepository;
    @Mock private LocalFileStorageService fileStorageService;
    @Mock private DocumentMapper documentMapper;
    @InjectMocks private DocumentServiceImpl documentService;

    private final Long userId = 1L;
    private Document testDocument;
    private DocumentResponse testResponse;
    private DocumentChunk testChunk;
    private DocumentChunkResponse testChunkResponse;

    @BeforeEach
    void setUp() {
        testDocument = Document.builder()
                .id(1L)
                .title("test.pdf")
                .fileName("test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .storagePath("uploads/documents/1_test.pdf")
                .checksum("abc123")
                .status("UPLOADED")
                .createdBy(User.builder().id(userId).build())
                .build();

        testResponse = DocumentResponse.builder()
                .id(1L)
                .title("test.pdf")
                .fileName("test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .status("UPLOADED")
                .createdBy(userId)
                .build();

        testChunk = DocumentChunk.builder()
                .id(1L)
                .document(testDocument)
                .chunkIndex(0)
                .content("test content")
                .build();

        testChunkResponse = DocumentChunkResponse.builder()
                .id(1L)
                .chunkIndex(0)
                .content("test content")
                .build();
    }

    @Test
    void upload_shouldSucceed() {
        var file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileStorageService.computeChecksum(file)).thenReturn("abc123");
        when(fileStorageService.store(file)).thenReturn("uploads/documents/1_test.pdf");
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        when(documentMapper.toResponse(testDocument)).thenReturn(testResponse);

        var result = documentService.upload(file, "test.pdf", userId);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("test.pdf");
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void upload_shouldUseFilenameAsTitle_whenTitleNull() {
        var file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileStorageService.computeChecksum(file)).thenReturn("abc123");
        when(fileStorageService.store(file)).thenReturn("uploads/documents/1_test.pdf");
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        when(documentMapper.toResponse(testDocument)).thenReturn(testResponse);

        documentService.upload(file, null, userId);

        verify(documentRepository).save(argThat(d ->
                d.getTitle().equals("test.pdf") && d.getFileName().equals("test.pdf")));
    }

    @Test
    void importFromUrl_shouldThrow_whenUrlUnreachable() {
        assertThatThrownBy(() -> documentService.importFromUrl("https://example.com/doc.pdf", "test.pdf", userId))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Failed to import from URL");
    }

    @Test
    void importFromUrl_shouldThrow_whenInvalidUrl() {
        assertThatThrownBy(() -> documentService.importFromUrl("not-a-url", "t", userId))
                .isInstanceOf(DocumentProcessingException.class);
    }

    @Test
    void getById_shouldReturnDocument() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentMapper.toResponse(testDocument)).thenReturn(testResponse);

        var result = documentService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdAndUser_shouldReturnDocument() {
        when(documentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDocument));
        when(documentMapper.toResponse(testDocument)).thenReturn(testResponse);

        var result = documentService.getByIdAndUser(1L, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void listByUser_shouldReturnPage() {
        var page = new PageImpl<>(List.of(testDocument));
        when(documentRepository.findAllByCreatedByIdAndDeletedAtIsNull(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        when(documentMapper.toResponse(testDocument)).thenReturn(testResponse);

        var result = documentService.listByUser(userId, Pageable.unpaged());

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void listAll_shouldReturnPage() {
        var page = new PageImpl<>(List.of(testDocument));
        when(documentRepository.findAllByDeletedAtIsNull(any(Pageable.class)))
                .thenReturn(page);
        when(documentMapper.toResponse(testDocument)).thenReturn(testResponse);

        var result = documentService.listAll(Pageable.unpaged());

        assertThat(result).hasSize(1);
    }

    @Test
    void getChunks_shouldReturnList() {
        when(documentChunkRepository.findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(1L))
                .thenReturn(List.of(testChunk));
        when(documentMapper.toChunkResponse(testChunk)).thenReturn(testChunkResponse);

        var result = documentService.getChunks(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContent()).isEqualTo("test content");
    }

    @Test
    void getChunks_shouldReturnEmpty_whenNone() {
        when(documentChunkRepository.findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(1L))
                .thenReturn(List.of());

        var result = documentService.getChunks(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void download_shouldReturnBytes() {
        when(documentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDocument));
        when(fileStorageService.read("uploads/documents/1_test.pdf")).thenReturn("data".getBytes());

        var result = documentService.download(1L);

        assertThat(result).isNotEmpty();
    }

    @Test
    void download_shouldThrow_whenNotFound() {
        when(documentRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.download(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldSoftDeleteOwnDocument() {
        when(documentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        documentService.delete(1L, userId);

        assertThat(testDocument.getDeletedAt()).isNotNull();
        verify(documentRepository).save(testDocument);
    }

    @Test
    void delete_shouldThrow_whenNotOwner() {
        when(documentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDocument));

        assertThatThrownBy(() -> documentService.delete(1L, 999L))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Cannot delete another user's document");
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(documentRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(999L, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAsAdmin_shouldSoftDeleteAnyDocument() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        documentService.deleteAsAdmin(1L);

        assertThat(testDocument.getDeletedAt()).isNotNull();
        verify(documentRepository).save(testDocument);
    }

    @Test
    void deleteAsAdmin_shouldThrow_whenNotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteAsAdmin(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
