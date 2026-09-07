package com.duoq.medlearn.ai.summary;
import com.duoq.medlearn.ai.summary.impl.AiSummaryServiceImpl;

import com.duoq.medlearn.ai.dto.request.SummaryRequest;
import com.duoq.medlearn.ai.enums.SummaryType;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.dto.response.AiUsage;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.repository.AiSummaryRepository;
import com.duoq.medlearn.ai.security.AiOutputValidator;
import com.duoq.medlearn.ai.summary.entity.AiSummary;
import com.duoq.medlearn.ai.usage.AiUsageService;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.section.entity.SectionType;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSummaryServiceImplTest {

    @Mock private AiSummaryRepository aiSummaryRepository;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private DiseaseVersionRepository diseaseVersionRepository;
    @Mock private DiseaseSectionRepository diseaseSectionRepository;
    @Mock private PromptTemplateService promptTemplateService;
    @Mock private PromptBuilder promptBuilder;
    @Mock private AiGatewayRouter gatewayRouter;
    @Mock private AiOutputValidator aiOutputValidator;
    @Mock private AiUsageService aiUsageService;

    @Captor private ArgumentCaptor<AiSummary> summaryCaptor;

    private AiSummaryServiceImpl service;
    private Disease disease;
    private DiseaseVersion version;
    private DiseaseSection overviewSection;
    private DiseaseSection symptomsSection;
    private PromptTemplate template;
    private AiChatResponse chatResponse;

    @BeforeEach
    void setUp() {
        service = new AiSummaryServiceImpl(
                aiSummaryRepository, diseaseRepository, diseaseVersionRepository,
                diseaseSectionRepository, promptTemplateService, promptBuilder,
                gatewayRouter, aiOutputValidator, aiUsageService);

        disease = Disease.builder().id(1L).name("Diabetes").build();
        disease.setCurrentVersion(DiseaseVersion.builder().id(10L).build());

        version = DiseaseVersion.builder()
                .id(10L).disease(disease).versionNumber(1)
                .status(VersionStatus.APPROVED).build();

        var overviewType = new SectionType();
        overviewType.setName("OVERVIEW");
        overviewSection = DiseaseSection.builder()
                .id(100L).diseaseVersion(version).sectionType(overviewType)
                .title("Overview").content("Diabetes is a metabolic disorder.")
                .orderIndex(0).build();

        var symptomsType = new SectionType();
        symptomsType.setName("SYMPTOMS");
        symptomsSection = DiseaseSection.builder()
                .id(101L).diseaseVersion(version).sectionType(symptomsType)
                .title("Symptoms").content("Polyuria, polydipsia, weight loss.")
                .orderIndex(1).build();

        template = PromptTemplate.builder()
                .code("disease-summary").version("1.0")
                .systemPrompt("You are a medical educator.")
                .userPromptTemplate("Generate {{summary_type}} summary for {{disease_name}}")
                .model("gpt-4o").temperature(0.7).maxTokens(4096)
                .requiredVariables("disease_name,summary_type")
                .status("ACTIVE").build();

        chatResponse = AiChatResponse.builder()
                .id("resp-1").model("gpt-4o").provider("nine-router")
                .choices(List.of(AiMessage.builder().role(AiRole.ASSISTANT).content("Generated summary content.").build()))
                .usage(AiUsage.builder().promptTokens(50).completionTokens(100).totalTokens(150).build())
                .latencyMs(500).cached(false).build();
    }

    @Test
    void generate_shouldCreateSummarySuccessfully() {
        var request = new SummaryRequest(SummaryType.STUDENT, null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(diseaseSectionRepository.findAllByVersionIdWithType(10L))
                .thenReturn(List.of(overviewSection, symptomsSection));
        when(promptTemplateService.getActiveEntity("disease-summary")).thenReturn(template);
        when(promptBuilder.buildFromTemplate(eq(template), any())).thenReturn(List.of(
                AiMessage.builder().role(AiRole.SYSTEM).content("You are a medical educator.").build(),
                AiMessage.builder().role(AiRole.USER).content("Generate STUDENT summary for Diabetes").build()
        ));
        when(gatewayRouter.chat(any(), any())).thenReturn(chatResponse);
        when(aiOutputValidator.validate("Generated summary content.")).thenReturn("Generated summary content.");
        when(aiSummaryRepository.findMaxVersionByDiseaseIdAndSummaryType(1L, SummaryType.STUDENT)).thenReturn(0);
        when(aiSummaryRepository.save(any())).thenAnswer(inv -> inv.<AiSummary>getArgument(0));

        var result = service.generate(1L, request, 42L);

        assertThat(result.getDiseaseId()).isEqualTo(1L);
        assertThat(result.getDiseaseName()).isEqualTo("Diabetes");
        assertThat(result.getSummaryType()).isEqualTo(SummaryType.STUDENT);
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo("Generated summary content.");
        assertThat(result.getModel()).isEqualTo("gpt-4o");
        assertThat(result.getTotalTokens()).isEqualTo(150);

        verify(aiSummaryRepository).save(summaryCaptor.capture());
        var saved = summaryCaptor.getValue();
        assertThat(saved.getSummaryType()).isEqualTo(SummaryType.STUDENT);
        assertThat(saved.getCreatedBy().getId()).isEqualTo(42L);

        verify(aiUsageService).log(any(), eq("SUMMARY"), eq("gpt-4o"), eq("nine-router"),
                eq(50), eq(100), eq(150), any(Long.class), eq(true), eq(null), eq(false));
    }

    @Test
    void generate_shouldThrowWhenDiseaseNotFound() {
        when(diseaseRepository.findById(99L)).thenReturn(Optional.empty());
        var request = new SummaryRequest(SummaryType.STUDENT, null, null);

        assertThatThrownBy(() -> service.generate(99L, request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generate_shouldThrowWhenVersionNotApproved() {
        var draftVersion = DiseaseVersion.builder()
                .id(11L).disease(disease).versionNumber(1)
                .status(VersionStatus.DRAFT).build();
        disease.setCurrentVersion(draftVersion);
        var request = new SummaryRequest(SummaryType.STUDENT, null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseVersionRepository.findById(11L)).thenReturn(Optional.of(draftVersion));

        assertThatThrownBy(() -> service.generate(1L, request, 1L))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiConfigurationException.class);
    }

    @Test
    void generate_shouldIncrementVersionOnRegenerate() {
        var request = new SummaryRequest(SummaryType.STUDENT, null, null);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(diseaseVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(diseaseSectionRepository.findAllByVersionIdWithType(10L))
                .thenReturn(List.of(overviewSection, symptomsSection));
        when(promptTemplateService.getActiveEntity("disease-summary")).thenReturn(template);
        when(promptBuilder.buildFromTemplate(eq(template), any())).thenReturn(List.of());
        when(gatewayRouter.chat(any(), any())).thenReturn(chatResponse);
        when(aiOutputValidator.validate("Generated summary content.")).thenReturn("Generated summary content.");
        when(aiSummaryRepository.findMaxVersionByDiseaseIdAndSummaryType(1L, SummaryType.STUDENT)).thenReturn(2);
        when(aiSummaryRepository.save(any())).thenAnswer(inv -> inv.<AiSummary>getArgument(0));

        var result = service.generate(1L, request, 42L);

        assertThat(result.getVersion()).isEqualTo(3);
    }

    @Test
    void listByDisease_shouldReturnAllSummaries() {
        var summary = AiSummary.builder().id(1L).disease(disease).diseaseVersion(version)
                .summaryType(SummaryType.STUDENT).version(1).content("Content")
                .model("gpt-4o").provider("nine-router").build();
        when(aiSummaryRepository.findAllByDiseaseIdAndDeletedAtIsNullOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(summary));

        var results = service.listByDisease(1L);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getDiseaseId()).isEqualTo(1L);
    }

    @Test
    void getLatest_shouldReturnLatestSummary() {
        var summary = AiSummary.builder().id(1L).disease(disease).diseaseVersion(version)
                .summaryType(SummaryType.STUDENT).version(2).content("Latest content")
                .model("gpt-4o").provider("nine-router").build();
        when(aiSummaryRepository.findTopByDiseaseIdAndSummaryTypeAndDeletedAtIsNullOrderByCreatedAtDesc(1L, SummaryType.STUDENT))
                .thenReturn(Optional.of(summary));

        var result = service.getLatest(1L, "STUDENT");

        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getContent()).isEqualTo("Latest content");
    }

    @Test
    void getLatest_shouldThrowWhenNotFound() {
        when(aiSummaryRepository.findTopByDiseaseIdAndSummaryTypeAndDeletedAtIsNullOrderByCreatedAtDesc(1L, SummaryType.STUDENT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest(1L, "STUDENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByVersion_shouldReturnSpecificVersion() {
        var summary = AiSummary.builder().id(1L).disease(disease).diseaseVersion(version)
                .summaryType(SummaryType.CLINICAL).version(1).content("Version 1 content")
                .model("gpt-4o").provider("nine-router").build();
        when(aiSummaryRepository.findByDiseaseIdAndSummaryTypeAndVersionAndDeletedAtIsNull(1L, SummaryType.CLINICAL, 1))
                .thenReturn(Optional.of(summary));

        var result = service.getByVersion(1L, "CLINICAL", 1);

        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getSummaryType()).isEqualTo(SummaryType.CLINICAL);
    }

    @Test
    void getByVersion_shouldThrowOnInvalidType() {
        assertThatThrownBy(() -> service.getByVersion(1L, "INVALID", 1))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiConfigurationException.class);
    }
}
