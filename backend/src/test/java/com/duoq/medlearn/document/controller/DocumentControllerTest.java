package com.duoq.medlearn.document.controller;

import com.duoq.medlearn.document.dto.DocumentChunkResponse;
import com.duoq.medlearn.document.dto.DocumentResponse;
import com.duoq.medlearn.document.service.DocumentService;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.common.PagedResponse;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.security.JwtService;
import com.duoq.medlearn.security.OAuth2LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = DocumentController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
class DocumentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DocumentService documentService;
    @MockBean private CurrentUserResolver currentUserResolver;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final DocumentResponse testDocument = DocumentResponse.builder()
            .id(1L).title("test.pdf").fileName("test.pdf").fileSize(1024L)
            .mimeType("application/pdf").status("UPLOADED").createdBy(1L)
            .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
            .build();

    private final DocumentChunkResponse testChunk = DocumentChunkResponse.builder()
            .id(1L).chunkIndex(0).content("chunk content").build();

    @Test
    void upload_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(documentService.upload(any(), any(), eq(1L))).thenReturn(testDocument);

        var file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("title", "test.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Document uploaded"));
    }

    @Test
    void importFromUrl_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(documentService.importFromUrl(anyString(), any(), eq(1L))).thenReturn(testDocument);

        mockMvc.perform(post("/api/documents/import-url")
                        .param("url", "https://example.com/doc.pdf")
                        .param("title", "doc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Document imported"));
    }

    @Test
    void importFromUrl_shouldReturn400_whenUrlBlank() throws Exception {
        mockMvc.perform(post("/api/documents/import-url")
                        .param("url", ""))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void listDocuments_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        var page = new PageImpl<>(List.of(testDocument));
        when(documentService.listByUser(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listDocuments_shouldUseListAll_whenAdmin() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        var page = new PageImpl<>(List.of(testDocument));
        when(documentService.listAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/documents").param("admin", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(documentService).listAll(any(Pageable.class));
        verify(documentService, never()).listByUser(anyLong(), any());
    }

    @Test
    void getDocument_shouldReturn200() throws Exception {
        when(documentService.getById(1L)).thenReturn(testDocument);

        mockMvc.perform(get("/api/documents/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("test.pdf"));
    }

    @Test
    void getChunks_shouldReturn200() throws Exception {
        when(documentService.getChunks(1L)).thenReturn(List.of(testChunk));

        mockMvc.perform(get("/api/documents/{id}/chunks", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("chunk content"));
    }

    @Test
    void download_shouldReturn200() throws Exception {
        when(documentService.getById(1L)).thenReturn(testDocument);
        when(documentService.download(1L)).thenReturn("data".getBytes());

        mockMvc.perform(get("/api/documents/{id}/download", 1L))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        doNothing().when(documentService).delete(1L, 1L);

        mockMvc.perform(delete("/api/documents/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Document deleted"));

        verify(documentService).delete(1L, 1L);
    }

    @Test
    void adminDelete_shouldReturn200() throws Exception {
        doNothing().when(documentService).deleteAsAdmin(1L);

        mockMvc.perform(delete("/api/documents/{id}/admin", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Document deleted by admin"));

        verify(documentService).deleteAsAdmin(1L);
    }
}
