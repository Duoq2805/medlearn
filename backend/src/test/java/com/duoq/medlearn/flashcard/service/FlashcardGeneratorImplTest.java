package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.dto.response.AiUsage;
import com.duoq.medlearn.ai.enums.FeatureType;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.generation.repository.AiGenerationRepository;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.security.AiOutputValidator;
import com.duoq.medlearn.ai.security.AiQuotaService;
import com.duoq.medlearn.ai.security.AiRateLimiter;
import com.duoq.medlearn.ai.usage.AiUsageService;
import com.duoq.medlearn.document.entity.DocumentChunk;
import com.duoq.medlearn.document.repository.DocumentChunkRepository;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.section.entity.SectionType;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.flashcard.dto.request.FlashcardGenerateRequest;
import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.mapper.FlashcardMapper;
import com.duoq.medlearn.flashcard.repository.FlashcardDeckRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardSourceRepository;
import com.duoq.medlearn.flashcard.service.impl.FlashcardGeneratorImpl;
import com.duoq.medlearn.flashcard.util.JsonFlashcardParser;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardGeneratorImplTest {

    @Mock private DiseaseRepository diseaseRepository;
    @Mock private DiseaseSectionRepository diseaseSectionRepository;
    @Mock private DocumentChunkRepository documentChunkRepository;
    @Mock private FlashcardDeckRepository deckRepository;
    @Mock private FlashcardRepository flashcardRepository;
    @Mock private FlashcardSourceRepository sourceRepository;
    @Mock private AiGenerationRepository aiGenerationRepository;
    @Mock private AiGatewayRouter gatewayRouter;
    @Mock private PromptBuilder promptBuilder;
    @Mock private PromptTemplateService promptTemplateService;
    @Mock private AiOutputValidator aiOutputValidator;
    @Mock private AiQuotaService aiQuotaService;
    @Mock private AiRateLimiter aiRateLimiter;
    @Mock private AiUsageService aiUsageService;
    @Mock private FlashcardMapper flashcardMapper;

    private FlashcardGenerator generator;
    private Disease disease;
    private DiseaseVersion approvedVersion;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        var parser = new JsonFlashcardParser(objectMapper);
        generator = new FlashcardGeneratorImpl(
                diseaseRepository, diseaseSectionRepository, documentChunkRepository, deckRepository,
                flashcardRepository, sourceRepository, aiGenerationRepository,
                gatewayRouter, promptBuilder, promptTemplateService,
                aiOutputValidator, aiQuotaService, aiRateLimiter,
                aiUsageService, parser, flashcardMapper);

        approvedVersion = DiseaseVersion.builder()
                .id(10L).versionNumber(1).status(VersionStatus.APPROVED).build();
        disease = Disease.builder().id(1L).name("Tăng huyết áp")
                .currentVersion(approvedVersion).build();
    }

    private void stubPromptBuilder() {
        var template = PromptTemplate.builder()
                .code("flashcard-gen").version("1.0").model("gpt-5-mini")
                .systemPrompt("You are a medical educator.")
                .userPromptTemplate("Generate {{count}} cards about {{disease}}.")
                .temperature(0.3).maxTokens(4000).build();
        when(promptTemplateService.getActiveEntity("flashcard-gen")).thenReturn(template);
        when(promptBuilder.buildFromTemplate(any(), any())).thenReturn(List.of(
                AiMessage.builder().role(AiRole.SYSTEM).content(template.getSystemPrompt()).build(),
                AiMessage.builder().role(AiRole.USER).content("Generate cards.").build()
        ));
    }

    private AiChatResponse stubAiResponse(String json) {
        return AiChatResponse.builder()
                .model("gpt-5-mini").provider("nine-router")
                .choices(List.of(AiMessage.builder().role(AiRole.ASSISTANT).content(json).build()))
                .usage(new AiUsage(100, 200, 300)).build();
    }

    private List<DiseaseSection> sampleSections() {
        var st1 = SectionType.builder().id(1).name("definition").build();
        var st2 = SectionType.builder().id(2).name("symptoms").build();
        return List.of(
                DiseaseSection.builder().id(1L).sectionType(st1).title("Định nghĩa")
                        .content("Tăng huyết áp là tình trạng áp lực máu cao.").orderIndex(0).build(),
                DiseaseSection.builder().id(2L).sectionType(st2).title("Triệu chứng")
                        .content("Đau đầu, chóng mặt, mờ mắt.").orderIndex(1).build()
        );
    }

    // ── DISEASE FLOW ─────────────────────────────────────────────────────────

    @Test
    void generateFromDisease_usesCurrentVersionSections() {
        stubPromptBuilder();
        var request = new FlashcardGenerateRequest(
                1L, null, "Tăng huyết áp", 5,
                FlashcardDifficulty.MEDIUM, "SYMPTOMS", null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseSectionRepository.findAllByVersionIdWithType(10L)).thenReturn(sampleSections());
        doNothing().when(aiQuotaService).checkQuota(any(), anyLong());
        doNothing().when(aiRateLimiter).checkRequestLimit(any());

        var json = "{\"flashcards\": [{\"question\": \"Q1?\", \"answer\": \"A1\", \"explanation\": \"E1\", \"source\": \"S1\"}]}";
        var aiResponse = stubAiResponse(json);
        when(gatewayRouter.chat(any(), any())).thenReturn(aiResponse);
        when(aiOutputValidator.validate(any())).thenReturn(json);

        var deck = com.duoq.medlearn.flashcard.entity.FlashcardDeck.builder()
                .id(1L).title("Tăng huyết áp").build();
        when(deckRepository.save(any())).thenReturn(deck);
        var card = com.duoq.medlearn.flashcard.entity.Flashcard.builder()
                .id(1L).deck(deck).question("Q1?").answer("A1").build();
        when(flashcardRepository.saveAll(any())).thenReturn(List.of(card));
        when(flashcardMapper.toResponse(any())).thenReturn(
                com.duoq.medlearn.flashcard.dto.response.FlashcardResponse.builder()
                        .id(1L).question("Q1?").answer("A1").build());

        var response = generator.generate(request, 1L);

        assertNotNull(response);
        assertEquals(1, response.getTotalGenerated());

        // Verify context was built from currentVersion sections (not empty)
        verify(diseaseSectionRepository).findAllByVersionIdWithType(10L);
        // Verify promptBuilder received non-empty context variable
        verify(promptBuilder).buildFromTemplate(any(), argThat(vars -> {
            var ctx = vars.get("context");
            return ctx != null && ctx.contains("definition") && ctx.contains("Tăng huyết áp là tình trạng");
        }));
    }

    @Test
    void generateFromDisease_shouldThrow_whenNoCurrentVersion() {
        var diseaseNoVersion = Disease.builder().id(2L).name("Bệnh chưa có version").build(); // currentVersion=null
        var request = new FlashcardGenerateRequest(
                2L, null, "Bệnh chưa có version", 5, FlashcardDifficulty.MEDIUM, null, null, null);

        when(diseaseRepository.findById(2L)).thenReturn(Optional.of(diseaseNoVersion));
        doNothing().when(aiQuotaService).checkQuota(any(), anyLong());
        doNothing().when(aiRateLimiter).checkRequestLimit(any());

        assertThrows(IllegalStateException.class, () -> generator.generate(request, 1L));
        // LLM must NOT be called
        verify(gatewayRouter, never()).chat(any(), any());
    }

    @Test
    void generateFromDisease_shouldThrow_whenCurrentVersionHasNoSections() {
        var request = new FlashcardGenerateRequest(
                1L, null, "Tăng huyết áp", 5, FlashcardDifficulty.MEDIUM, null, null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseSectionRepository.findAllByVersionIdWithType(10L)).thenReturn(List.of()); // no sections
        doNothing().when(aiQuotaService).checkQuota(any(), anyLong());
        doNothing().when(aiRateLimiter).checkRequestLimit(any());

        assertThrows(IllegalStateException.class, () -> generator.generate(request, 1L));
        verify(gatewayRouter, never()).chat(any(), any());
    }

    // ── ORIGINAL TESTS (preserved, updated for new constructor) ───────────────

    @Test
    void generateFromDisease_Success() {
        stubPromptBuilder();
        var request = new FlashcardGenerateRequest(
                1L, null, "Tăng huyết áp", 5,
                FlashcardDifficulty.MEDIUM, "SYMPTOMS", null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseSectionRepository.findAllByVersionIdWithType(10L)).thenReturn(sampleSections());
        doNothing().when(aiQuotaService).checkQuota(any(), anyLong());
        doNothing().when(aiRateLimiter).checkRequestLimit(any());

        var json = """
                {"flashcards": [
                  {"question": "Q1?", "answer": "A1", "explanation": "E1", "source": "S1"}
                ]}
                """;
        var aiResponse = stubAiResponse(json);
        when(gatewayRouter.chat(any(), any())).thenReturn(aiResponse);
        when(aiOutputValidator.validate(any())).thenReturn(aiResponse.getChoices().getFirst().getContent());

        var deck = com.duoq.medlearn.flashcard.entity.FlashcardDeck.builder()
                .id(1L).title("Tăng huyết áp").build();
        when(deckRepository.save(any())).thenReturn(deck);

        var card = com.duoq.medlearn.flashcard.entity.Flashcard.builder()
                .id(1L).deck(deck).question("Q1?").answer("A1").build();
        when(flashcardRepository.saveAll(any())).thenReturn(List.of(card));
        when(flashcardMapper.toResponse(any()))
                .thenReturn(com.duoq.medlearn.flashcard.dto.response.FlashcardResponse.builder()
                        .id(1L).question("Q1?").answer("A1").build());

        var response = generator.generate(request, 1L);

        assertNotNull(response);
        assertEquals(1, response.getTotalGenerated());
        assertEquals(1, response.getFlashcards().size());
        verify(aiGenerationRepository).save(any());
        verify(aiUsageService).log(any(), any(), any(), any(), anyInt(), anyInt(), anyInt(), anyLong(), anyBoolean(), any(), anyBoolean());
    }

    @Test
    void generateFromDocument_Success() {
        stubPromptBuilder();
        var request = new FlashcardGenerateRequest(
                null, 1L, "Tài liệu tim mạch", 3,
                FlashcardDifficulty.EASY, null, null, null);

        var chunk = DocumentChunk.builder()
                .id(1L).chunkIndex(0).content("Nội dung về tim mạch...").build();
        when(documentChunkRepository.findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(1L))
                .thenReturn(List.of(chunk));
        doNothing().when(aiQuotaService).checkQuota(any(), anyLong());
        doNothing().when(aiRateLimiter).checkRequestLimit(any());

        var aiResponse = stubAiResponse("{\"flashcards\": []}");
        when(gatewayRouter.chat(any(), any())).thenReturn(aiResponse);
        when(aiOutputValidator.validate(any())).thenReturn(aiResponse.getChoices().getFirst().getContent());

        var deck = com.duoq.medlearn.flashcard.entity.FlashcardDeck.builder()
                .id(2L).title("Tài liệu tim mạch").build();
        when(deckRepository.save(any())).thenReturn(deck);

        var response = generator.generate(request, 1L);

        assertEquals(0, response.getTotalGenerated()); // empty array
        verify(sourceRepository, never()).save(any()); // no cards, no sources
        // diseaseSectionRepository must NOT be called for document path
        verify(diseaseSectionRepository, never()).findAllByVersionIdWithType(any());
    }

    @Test
    void bothDiseaseAndDocument_ShouldThrow() {
        var request = new FlashcardGenerateRequest(
                1L, 1L, "Test", 5, FlashcardDifficulty.MEDIUM, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> generator.generate(request, 1L));
    }

    @Test
    void neitherDiseaseNorDocument_ShouldThrow() {
        var request = new FlashcardGenerateRequest(
                null, null, "Test", 5, FlashcardDifficulty.MEDIUM, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> generator.generate(request, 1L));
    }

    @Test
    void aiGatewayFailure_ShouldThrow() {
        stubPromptBuilder();
        var request = new FlashcardGenerateRequest(
                1L, null, "Test", 5, FlashcardDifficulty.MEDIUM, null, null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseSectionRepository.findAllByVersionIdWithType(10L)).thenReturn(sampleSections());
        doNothing().when(aiQuotaService).checkQuota(any(), anyLong());
        doNothing().when(aiRateLimiter).checkRequestLimit(any());
        when(gatewayRouter.chat(any(), any())).thenThrow(new RuntimeException("AI provider unavailable"));

        assertThrows(RuntimeException.class, () -> generator.generate(request, 1L));
    }
}

