package com.duoq.medlearn.flashcard.service.impl;

import com.duoq.medlearn.ai.enums.FeatureType;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.dto.response.AiUsage;
import com.duoq.medlearn.ai.generation.entity.AiGeneration;
import com.duoq.medlearn.ai.generation.repository.AiGenerationRepository;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.security.AiOutputValidator;
import com.duoq.medlearn.ai.security.AiQuotaService;
import com.duoq.medlearn.ai.security.AiRateLimiter;
import com.duoq.medlearn.ai.usage.AiUsageService;
import com.duoq.medlearn.document.repository.DocumentChunkRepository;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.flashcard.dto.request.FlashcardGenerateRequest;
import com.duoq.medlearn.flashcard.dto.response.FlashcardGenerateResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardResponse;
import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import com.duoq.medlearn.flashcard.entity.FlashcardSource;
import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.enums.SourceType;
import com.duoq.medlearn.flashcard.mapper.FlashcardMapper;
import com.duoq.medlearn.flashcard.repository.FlashcardDeckRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardSourceRepository;
import com.duoq.medlearn.flashcard.service.FlashcardGenerator;
import com.duoq.medlearn.flashcard.util.JsonFlashcardParser;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardGeneratorImpl implements FlashcardGenerator {

    private static final String REQUEST_TYPE = "FLASHCARD";
    private static final String PROMPT_CODE = "flashcard-gen";

    private final DiseaseRepository diseaseRepository;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final FlashcardDeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardSourceRepository sourceRepository;
    private final AiGenerationRepository aiGenerationRepository;
    private final AiGatewayRouter gatewayRouter;
    private final PromptBuilder promptBuilder;
    private final PromptTemplateService promptTemplateService;
    private final AiOutputValidator aiOutputValidator;
    private final AiQuotaService aiQuotaService;
    private final AiRateLimiter aiRateLimiter;
    private final AiUsageService aiUsageService;
    private final JsonFlashcardParser jsonParser;
    private final FlashcardMapper flashcardMapper;

    @Override
    @Transactional
    public FlashcardGenerateResponse generate(FlashcardGenerateRequest request, Long userId) {
        // Validate XOR: diseaseId or documentId, not both, not neither
        if (request.getDiseaseId() != null && request.getDocumentId() != null) {
            throw new IllegalArgumentException("Provide either diseaseId or documentId, not both");
        }
        if (request.getDiseaseId() == null && request.getDocumentId() == null) {
            throw new IllegalArgumentException("Either diseaseId or documentId is required");
        }

        aiQuotaService.checkQuota(userId, request.getCount() * 200L);
        aiRateLimiter.checkRequestLimit(String.valueOf(userId));

        // Resolve source and build context
        String contextText;
        SourceType sourceType;
        Long sourceId;
        String diseaseName;

        if (request.getDiseaseId() != null) {
            var disease = diseaseRepository.findById(request.getDiseaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disease not found: " + request.getDiseaseId()));
            if (disease.getCurrentVersion() == null) {
                throw new IllegalStateException(
                        "Disease '" + disease.getName() + "' has no approved version. "
                                + "Approve a version before generating flashcards.");
            }
            var sections = diseaseSectionRepository.findAllByVersionIdWithType(disease.getCurrentVersion().getId());
            if (sections.isEmpty()) {
                throw new IllegalStateException(
                        "Disease '" + disease.getName() + "' current version has no sections. "
                                + "Add content to the disease version before generating flashcards.");
            }
            sourceType = SourceType.DISEASE;
            sourceId = disease.getId();
            diseaseName = disease.getName();
            contextText = sections.stream()
                    .map(s -> "[" + s.getSectionType().getName() + "]: " + s.getContent())
                    .collect(Collectors.joining("\n\n"));
            if (contextText.length() > 50000) {
                contextText = contextText.substring(0, 50000) + "\n\n[...truncated]";
            }
        } else {
            var document = documentChunkRepository
                    .findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(request.getDocumentId());
            if (document.isEmpty()) {
                throw new ResourceNotFoundException("No chunks found for document: " + request.getDocumentId());
            }
            sourceType = SourceType.DOCUMENT;
            sourceId = request.getDocumentId();
            diseaseName = request.getTitle();
            contextText = document.stream()
                    .map(c -> "[Chunk " + c.getChunkIndex() + "]: " + c.getContent())
                    .collect(Collectors.joining("\n\n"));
            // Truncate context to avoid exceeding token limits
            if (contextText.length() > 50000) {
                contextText = contextText.substring(0, 50000) + "\n\n[...truncated]";
            }
        }

        // Build prompt variables
        var variables = new HashMap<String, String>();
        variables.put("disease", diseaseName);
        variables.put("count", String.valueOf(request.getCount()));
        variables.put("difficulty", request.getDifficulty() != null
                ? request.getDifficulty().name().toLowerCase() : "medium");
        variables.put("context", contextText);

        // Use PromptBuilder: loads PromptTemplate, renders via PromptRenderer, filters via PromptInjectionFilter
        var template = promptTemplateService.getActiveEntity(PROMPT_CODE);
        var messages = promptBuilder.buildFromTemplate(template, variables);

        var model = request.getModel() != null
                ? AiModel.fromModelId(request.getModel())
                : AiModel.fromModelId(template.getModel() != null ? template.getModel() : AiModel.GPT_5_MINI.getModelId());
        var temperature = request.getTemperature() != null ? request.getTemperature()
                : (template.getTemperature() != null ? template.getTemperature() : 0.3);
        var maxTokens = template.getMaxTokens() != null ? template.getMaxTokens() : 4000;

        var chatRequest = AiChatRequest.builder()
                .messages(messages)
                .model(model.getModelId())
                .temperature(temperature)
                .maxTokens(maxTokens)
                .promptTemplateCode(PROMPT_CODE)
                .userId(String.valueOf(userId))
                .build();

        var startTime = System.currentTimeMillis();
        AiChatResponse response;
        try {
            response = gatewayRouter.chat(chatRequest, model);
        } catch (Exception e) {
            log.error("AI generation failed for userId={}: {}", userId, e.getMessage());
            throw e;
        }
        var latencyMs = System.currentTimeMillis() - startTime;

        var rawContent = response.getChoices() != null && !response.getChoices().isEmpty()
                ? response.getChoices().getFirst().getContent()
                : "";

        // Validate AI output
        rawContent = aiOutputValidator.validate(rawContent);

        // Parse JSON
        var parsedCards = jsonParser.parse(rawContent);
        if (parsedCards.isEmpty()) {
            log.warn("AI returned no valid flashcards for userId={}", userId);
        }

        // Create deck
        var deck = FlashcardDeck.builder()
                .title(request.getTitle())
                .sourceType(sourceType)
                .sourceId(sourceId)
                .createdBy(User.builder().id(userId).build())
                .cardCount(parsedCards.size())
                .build();
        var savedDeck = deckRepository.save(deck);

        // Batch persist flashcards
        var usage = response.getUsage();
        var cards = new ArrayList<Flashcard>();
        for (var parsed : parsedCards) {
            cards.add(Flashcard.builder()
                    .deck(savedDeck)
                    .question(parsed.question())
                    .answer(parsed.answer())
                    .explanation(parsed.explanation())
                    .source(parsed.source())
                    .tag(request.getTag())
                    .difficulty(request.getDifficulty() != null ? request.getDifficulty() : FlashcardDifficulty.MEDIUM)
                    .createdBy(User.builder().id(userId).build())
                    .build());
        }
        var savedCards = flashcardRepository.saveAll(cards);

        var flashcardResponses = new ArrayList<FlashcardResponse>();
        for (var card : savedCards) {
            flashcardResponses.add(flashcardMapper.toResponse(card));
        }

        // Batch save source entries for document chunks
        if (request.getDocumentId() != null && !savedCards.isEmpty()) {
            var sources = new ArrayList<FlashcardSource>();
            for (var card : savedCards) {
                sources.add(FlashcardSource.builder()
                        .flashcard(card)
                        .sourceType("CHUNK")
                        .sourceId(request.getDocumentId())
                        .build());
            }
            sourceRepository.saveAll(sources);
        }

        // Save AiGeneration record (full provenance for reproducibility)
        var generation = AiGeneration.builder()
                .featureType(FeatureType.FLASHCARD)
                .featureId(savedDeck.getId())
                .promptTemplateCode(PROMPT_CODE)
                .promptTemplateVersion(template.getVersion())
                .model(response.getModel())
                .provider(response.getProvider())
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                .latencyMs((int) latencyMs)
                .systemPrompt(template.getSystemPrompt())
                .userPrompt(template.getUserPromptTemplate())
                .rawResponse(rawContent)
                .status("SUCCESS")
                .createdBy(User.builder().id(userId).build())
                .build();
        aiGenerationRepository.save(generation);

        // Async usage logging
        aiUsageService.log(
                User.builder().id(userId).build(),
                REQUEST_TYPE,
                response.getModel(),
                response.getProvider(),
                usage != null ? usage.getPromptTokens() : 0,
                usage != null ? usage.getCompletionTokens() : 0,
                usage != null ? usage.getTotalTokens() : 0,
                latencyMs,
                true,
                null,
                response.isCached()
        );

        log.info("Flashcards generated: deckId={}, count={}, tokens={}",
                savedDeck.getId(), parsedCards.size(),
                usage != null ? usage.getTotalTokens() : 0);

        return FlashcardGenerateResponse.builder()
                .generationId(String.valueOf(generation.getId()))
                .totalGenerated(parsedCards.size())
                .flashcards(flashcardResponses)
                .usage(usage != null ? usage : new AiUsage(0, 0, 0))
                .build();
    }

    private String buildSystemPrompt(String tag) {
        var sb = new StringBuilder();
        sb.append("You are a medical education expert creating flashcards for medical students.\n");
        sb.append("Generate flashcards in Vietnamese. Use clear, precise medical language.\n");
        sb.append("Each flashcard must have a question and answer that tests understanding, not just recall.\n");
        sb.append("Return ONLY a JSON object with a 'flashcards' array.\n");
        sb.append("Each element in the array must have: 'question', 'answer', 'explanation', 'source'.\n");
        sb.append("Do NOT include markdown code fences, markdown formatting, or any text outside the JSON.\n");
        sb.append("Example format:\n");
        sb.append("{\"flashcards\":[{\"question\":\"...\",\"answer\":\"...\",\"explanation\":\"...\",\"source\":\"...\"}]}\n");
        return sb.toString();
    }

    private String buildUserPrompt(Map<String, String> vars) {
        var sb = new StringBuilder();
        sb.append("Generate ").append(vars.get("count")).append(" flashcards about \"")
                .append(vars.get("disease")).append("\".\n\n");

        var context = vars.get("context");
        if (context != null && !context.isBlank()) {
            sb.append("Use the following source material:\n").append(context).append("\n\n");
        }

        sb.append("Difficulty level: ").append(vars.get("difficulty")).append("\n");
        sb.append("Language: Vietnamese\n");
        sb.append("Return ONLY valid JSON as specified in the system prompt.\n");
        return sb.toString();
    }
}
