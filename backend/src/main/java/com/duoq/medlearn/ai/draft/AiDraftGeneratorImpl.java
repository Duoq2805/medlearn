package com.duoq.medlearn.ai.draft.impl;

import com.duoq.medlearn.ai.draft.AiDraftGenerator;
import com.duoq.medlearn.ai.dto.enums.AiRole;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.document.entity.Document;
import com.duoq.medlearn.document.entity.DocumentChunk;
import com.duoq.medlearn.document.repository.DocumentChunkRepository;
import com.duoq.medlearn.document.repository.DocumentRepository;
import com.duoq.medlearn.draft.dto.AiDraftRequest;
import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.DiseaseDraftSectionResponse;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.entity.DraftProvenance;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.repository.DraftProvenanceRepository;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.repository.DiseaseRepository;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDraftGeneratorImpl implements AiDraftGenerator {

    private static final String SYSTEM_PROMPT = """
            You are a medical content writer creating structured disease content for a medical learning platform.
            Write in Vietnamese. Use clear, professional medical language.
            Structure the content according to the requested sections.
            Each section should be comprehensive but concise, suitable for medical students.
            """;

    private final AiGatewayRouter gatewayRouter;
    private final DiseaseRepository diseaseRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DiseaseDraftRepository draftRepository;
    private final DiseaseDraftSectionRepository sectionRepository;
    private final DraftProvenanceRepository provenanceRepository;

    @Override
    @Transactional
    public DiseaseDraftResponse generate(AiDraftRequest request, Long userId) {
        Disease disease = null;
        if (request.getDiseaseId() != null) {
            disease = diseaseRepository.findById(request.getDiseaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disease not found: " + request.getDiseaseId()));
        }

        String contextText = "";
        if (request.getDocumentId() != null) {
            var chunks = documentChunkRepository
                    .findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(request.getDocumentId());
            contextText = chunks.stream()
                    .map(c -> "[Chunk " + c.getChunkIndex() + "]: " + c.getContent())
                    .collect(Collectors.joining("\n\n"));
        }

        var draft = DiseaseDraft.builder()
                .disease(disease)
                .title(request.getTitle())
                .sourceMethod(DraftMethod.AI_FULL)
                .sourceDocument(request.getDocumentId() != null
                        ? Document.builder().id(request.getDocumentId()).build() : null)
                .createdBy(User.builder().id(userId).build())
                .build();
        var savedDraft = draftRepository.save(draft);

        var model = request.getModel() != null ? AiModel.fromModelId(request.getModel()) : AiModel.GPT_5_MINI;
        var temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;

        int totalTokens = 0;
        long totalLatency = 0;
        List<DiseaseDraftSectionResponse> sectionResponses = new ArrayList<>();

        for (var sectionType : request.getSections()) {
            var userPrompt = buildSectionPrompt(sectionType, request.getTitle(), contextText);

            var chatRequest = AiChatRequest.builder()
                    .messages(List.of(
                            createMessage(AiRole.SYSTEM, SYSTEM_PROMPT),
                            createMessage(AiRole.USER, userPrompt)
                    ))
                    .model(model.getModelId())
                    .temperature(temperature)
                    .maxTokens(2000)
                    .userId(String.valueOf(userId))
                    .build();

            var startTime = System.currentTimeMillis();
            var response = gatewayRouter.chat(chatRequest, model);
            var latencyMs = System.currentTimeMillis() - startTime;

            var content = response.getChoices() != null && !response.getChoices().isEmpty()
                    ? response.getChoices().getFirst().getContent()
                    : "";
            var usage = response.getUsage();
            int promptTokens = usage != null ? usage.getPromptTokens() : 0;
            int completionTokens = usage != null ? usage.getCompletionTokens() : 0;
            totalTokens += promptTokens + completionTokens;
            totalLatency += latencyMs;

            var section = DiseaseDraftSection.builder()
                    .draft(savedDraft)
                    .sectionType(sectionType)
                    .title(getSectionTitle(sectionType))
                    .content(content)
                    .orderIndex(sectionResponses.size())
                    .aiGenerated(true)
                    .build();
            var savedSection = sectionRepository.save(section);

            provenanceRepository.save(DraftProvenance.builder()
                    .draft(savedDraft)
                    .sectionType(sectionType)
                    .systemPrompt(SYSTEM_PROMPT)
                    .userPrompt(userPrompt)
                    .rawResponse(content)
                    .model(model.getModelId())
                    .provider(response.getProvider())
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .latencyMs((int) latencyMs)
                    .build());

            sectionResponses.add(DiseaseDraftSectionResponse.builder()
                    .id(savedSection.getId())
                    .sectionType(sectionType)
                    .title(getSectionTitle(sectionType))
                    .content(content)
                    .orderIndex(sectionResponses.size())
                    .aiGenerated(true)
                    .build());
        }

        savedDraft.setAiModel(model.getModelId());
        savedDraft.setAiTotalTokens(totalTokens);
        savedDraft.setAiLatencyMs((int) totalLatency);
        draftRepository.save(savedDraft);

        log.info("AI draft generated: id={}, sections={}, tokens={}, latency={}ms",
                savedDraft.getId(), request.getSections().size(), totalTokens, totalLatency);

        return DiseaseDraftResponse.builder()
                .id(savedDraft.getId())
                .diseaseId(disease != null ? disease.getId() : null)
                .diseaseName(disease != null ? disease.getName() : null)
                .title(savedDraft.getTitle())
                .status(savedDraft.getStatus())
                .sourceMethod(DraftMethod.AI_FULL)
                .aiModel(model.getModelId())
                .aiTotalTokens(totalTokens)
                .aiLatencyMs((int) totalLatency)
                .createdBy(userId)
                .sections(sectionResponses)
                .build();
    }

    private String buildSectionPrompt(DraftSectionType sectionType, String title, String context) {
        var sb = new StringBuilder();
        sb.append("Viết nội dung cho phần \"").append(getSectionTitle(sectionType))
                .append("\" của bài viết về \"").append(title).append("\".\n\n");

        if (!context.isBlank()) {
            sb.append("Sử dụng thông tin từ tài liệu sau:\n").append(context).append("\n\n");
        }

        sb.append("Yêu cầu:\n");
        sb.append("- Viết bằng tiếng Việt\n");
        sb.append("- Ngôn ngữ y khoa chuyên nghiệp, rõ ràng\n");
        sb.append("- Dài khoảng 300-500 từ\n");
        sb.append("- Có thể bao gồm bullet points hoặc danh sách nếu cần\n");
        sb.append("- Chính xác về mặt y khoa\n");
        return sb.toString();
    }

    private String getSectionTitle(DraftSectionType type) {
        return switch (type) {
            case OVERVIEW -> "Tổng quan";
            case DEFINITION -> "Định nghĩa";
            case CAUSES -> "Nguyên nhân";
            case SYMPTOMS -> "Triệu chứng";
            case DIAGNOSIS -> "Chẩn đoán";
            case TREATMENT -> "Điều trị";
            case PROGNOSIS -> "Tiên lượng";
            case COMPLICATIONS -> "Biến chứng";
            case PREVENTION -> "Phòng ngừa";
            case EPIDEMIOLOGY -> "Dịch tễ học";
            case PATHOPHYSIOLOGY -> "Sinh lý bệnh";
            case RISK_FACTORS -> "Yếu tố nguy cơ";
            case CLINICAL_FEATURES -> "Đặc điểm lâm sàng";
            case INVESTIGATIONS -> "Cận lâm sàng";
            case MANAGEMENT -> "Quản lý bệnh";
            case DIFFERENTIAL_DIAGNOSIS -> "Chẩn đoán phân biệt";
            case REFERENCE -> "Tài liệu tham khảo";
        };
    }

    private AiMessage createMessage(AiRole role, String content) {
        return AiMessage.builder()
                .role(role)
                .content(content)
                .build();
    }
}
