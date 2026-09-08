package com.duoq.medlearn.quiz.service.impl;

import com.duoq.medlearn.ai.enums.FeatureType;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.generation.entity.AiGeneration;
import com.duoq.medlearn.ai.generation.repository.AiGenerationRepository;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.definition.QuizGenerationPrompt;
import com.duoq.medlearn.ai.security.AiOutputValidator;
import com.duoq.medlearn.ai.security.AiQuotaService;
import com.duoq.medlearn.ai.security.AiRateLimiter;
import com.duoq.medlearn.ai.usage.AiUsageService;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.document.repository.DocumentChunkRepository;
import com.duoq.medlearn.flashcard.enums.SourceType;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.quiz.dto.request.QuizGenerateRequest;
import com.duoq.medlearn.quiz.dto.request.QuizSubmitRequest;
import com.duoq.medlearn.quiz.dto.response.*;
import com.duoq.medlearn.quiz.entity.Question;
import com.duoq.medlearn.quiz.entity.Quiz;
import com.duoq.medlearn.quiz.entity.QuizAttempt;
import com.duoq.medlearn.quiz.entity.QuizAttemptAnswer;
import com.duoq.medlearn.quiz.mapper.QuizMapper;
import com.duoq.medlearn.quiz.repository.*;
import com.duoq.medlearn.quiz.service.QuizService;
import com.duoq.medlearn.quiz.util.JsonQuizParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private static final String REQUEST_TYPE = "QUIZ";

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAttemptAnswerRepository answerRepository;
    private final DiseaseRepository diseaseRepository;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final AiGenerationRepository aiGenerationRepository;
    private final AiGatewayRouter gatewayRouter;
    private final PromptBuilder promptBuilder;
    private final QuizGenerationPrompt quizPrompt;   // in-code prompt definition
    private final AiOutputValidator aiOutputValidator;
    private final AiQuotaService aiQuotaService;
    private final AiRateLimiter aiRateLimiter;
    private final AiUsageService aiUsageService;
    private final JsonQuizParser jsonQuizParser;
    private final QuizMapper quizMapper;

    @Override
    @Transactional
    public QuizGenerateResponse generate(QuizGenerateRequest request, Long userId) {
        if (request.getDiseaseId() != null && request.getDocumentId() != null) {
            throw new IllegalArgumentException("Provide either diseaseId or documentId, not both");
        }
        if (request.getDiseaseId() == null && request.getDocumentId() == null) {
            throw new IllegalArgumentException("Either diseaseId or documentId is required");
        }

        aiQuotaService.checkQuota(userId, request.getCount() * 300L);
        aiRateLimiter.checkRequestLimit(String.valueOf(userId));

        // Resolve source
        String contextText;
        SourceType sourceType;
        Long sourceId;
        String topicName;

        if (request.getDiseaseId() != null) {
            var disease = diseaseRepository.findById(request.getDiseaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disease not found: " + request.getDiseaseId()));
            if (disease.getCurrentVersion() == null) {
                throw new IllegalStateException("Disease '" + disease.getName() + "' has no approved version.");
            }
            var sections = diseaseSectionRepository.findAllByVersionIdWithType(disease.getCurrentVersion().getId());
            if (sections.isEmpty()) {
                throw new IllegalStateException("Disease '" + disease.getName() + "' has no sections.");
            }
            sourceType = SourceType.DISEASE;
            sourceId = disease.getId();
            topicName = disease.getName();
            contextText = sections.stream()
                    .map(s -> "[" + s.getSectionType().getName() + "]: " + s.getContent())
                    .collect(Collectors.joining("\n\n"));
        } else {
            var chunks = documentChunkRepository
                    .findAllByDocumentIdAndDeletedAtIsNullOrderByChunkIndex(request.getDocumentId());
            if (chunks.isEmpty()) {
                throw new ResourceNotFoundException("No chunks found for document: " + request.getDocumentId());
            }
            sourceType = SourceType.DOCUMENT;
            sourceId = request.getDocumentId();
            topicName = request.getTitle();
            contextText = chunks.stream()
                    .map(c -> "[Chunk " + c.getChunkIndex() + "]: " + c.getContent())
                    .collect(Collectors.joining("\n\n"));
        }

        if (contextText.length() > 50000) {
            contextText = contextText.substring(0, 50000) + "\n\n[...truncated]";
        }

        // Build prompt variables — injection filter applied inside buildFromDefinition
        var variables = new HashMap<String, String>();
        variables.put("topic", topicName);
        variables.put("count", String.valueOf(request.getCount()));
        variables.put("context", contextText);

        var messages = promptBuilder.buildFromDefinition(quizPrompt, variables);

        var model = request.getModel() != null
                ? AiModel.fromModelId(request.getModel())
                : AiModel.fromModelId(quizPrompt.getModel());
        var temperature = request.getTemperature() != null
                ? request.getTemperature() : quizPrompt.getTemperature();

        var chatRequest = AiChatRequest.builder()
                .messages(messages)
                .model(model.getModelId())
                .temperature(temperature)
                .maxTokens(quizPrompt.getMaxTokens())
                .promptTemplateCode(quizPrompt.getCode())
                .userId(String.valueOf(userId))
                .build();

        var startTime = System.currentTimeMillis();
        AiChatResponse response;
        try {
            response = gatewayRouter.chat(chatRequest, model);
        } catch (Exception e) {
            log.error("Quiz AI generation failed for userId={}: {}", userId, e.getMessage());
            throw e;
        }
        var latencyMs = System.currentTimeMillis() - startTime;

        var rawContent = response.getChoices() != null && !response.getChoices().isEmpty()
                ? response.getChoices().getFirst().getContent() : "";
        rawContent = aiOutputValidator.validate(rawContent);

        var parsed = jsonQuizParser.parse(rawContent);
        if (parsed.isEmpty()) {
            log.warn("AI returned no valid questions for userId={}", userId);
        }

        // Persist quiz
        var quiz = Quiz.builder()
                .title(request.getTitle())
                .sourceType(sourceType)
                .sourceId(sourceId)
                .questionCount(parsed.size())
                .createdBy(User.builder().id(userId).build())
                .build();
        var savedQuiz = quizRepository.save(quiz);

        // Persist questions
        var questions = new ArrayList<Question>();
        for (int i = 0; i < parsed.size(); i++) {
            var p = parsed.get(i);
            questions.add(Question.builder()
                    .quiz(savedQuiz)
                    .content(p.content())
                    .optionA(p.optionA())
                    .optionB(p.optionB())
                    .optionC(p.optionC())
                    .optionD(p.optionD())
                    .correctAnswer(p.correctAnswer())
                    .explanation(p.explanation())
                    .displayOrder(i)
                    .build());
        }
        var savedQuestions = questionRepository.saveAll(questions);

        // AiGeneration audit record
        var usage = response.getUsage();
        var generation = AiGeneration.builder()
                .featureType(FeatureType.QUIZ)
                .featureId(savedQuiz.getId())
                .promptTemplateCode(quizPrompt.getCode())
                .promptTemplateVersion("code")   // prompt is in source code, not DB
                .model(response.getModel())
                .provider(response.getProvider())
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                .latencyMs((int) latencyMs)
                .systemPrompt(quizPrompt.getSystemPrompt())
                .userPrompt(quizPrompt.getUserPromptTemplate())
                .rawResponse(rawContent)
                .status("SUCCESS")
                .createdBy(User.builder().id(userId).build())
                .build();
        aiGenerationRepository.save(generation);

        aiUsageService.log(
                User.builder().id(userId).build(), REQUEST_TYPE,
                response.getModel(), response.getProvider(),
                usage != null ? usage.getPromptTokens() : 0,
                usage != null ? usage.getCompletionTokens() : 0,
                usage != null ? usage.getTotalTokens() : 0,
                latencyMs, true, null, response.isCached());

        log.info("Quiz generated: quizId={}, questions={}, tokens={}",
                savedQuiz.getId(), parsed.size(), usage != null ? usage.getTotalTokens() : 0);

        var questionResponses = quizMapper.toQuestionResponseList(savedQuestions);
        var quizResponse = quizMapper.toResponse(savedQuiz, questionResponses);

        return QuizGenerateResponse.builder()
                .quizId(savedQuiz.getId())
                .title(savedQuiz.getTitle())
                .totalGenerated(parsed.size())
                .quiz(quizResponse)
                .usage(usage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getById(Long quizId, Long userId) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
        var questions = questionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId);
        return quizMapper.toResponse(quiz, quizMapper.toQuestionResponseList(questions));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> listByUser(Long userId, Pageable pageable) {
        return quizRepository.findByCreatedById(userId, pageable)
                .map(quiz -> {
                    var questions = questionRepository.findByQuizIdOrderByDisplayOrderAsc(quiz.getId());
                    return quizMapper.toResponse(quiz, quizMapper.toQuestionResponseList(questions));
                });
    }

    @Override
    @Transactional
    public QuizSubmitResponse submit(Long quizId, QuizSubmitRequest request, Long userId) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        var questions = questionRepository.findByQuizIdOrderByDisplayOrderAsc(quizId);
        var questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        // Grade answers server-side
        var results = new ArrayList<QuizSubmitResponse.QuestionResult>();
        int correct = 0;
        for (var answer : request.getAnswers()) {
            var q = questionMap.get(answer.getQuestionId());
            if (q == null) continue;
            boolean isCorrect = q.getCorrectAnswer().equals(answer.getSelectedAnswer());
            if (isCorrect) correct++;
            results.add(QuizSubmitResponse.QuestionResult.builder()
                    .questionId(q.getId())
                    .selectedAnswer(answer.getSelectedAnswer())
                    .correctAnswer(q.getCorrectAnswer())
                    .correct(isCorrect)
                    .explanation(q.getExplanation())
                    .build());
        }

        int total = results.size();
        double percentage = total > 0 ? (double) correct / total * 100 : 0;

        // Persist attempt
        var attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(User.builder().id(userId).build())
                .correctCount(correct)
                .totalQuestions(total)
                .percentage(percentage)
                .status("COMPLETED")
                .build();
        var savedAttempt = attemptRepository.save(attempt);

        // Persist per-question answers
        var attemptAnswers = new ArrayList<QuizAttemptAnswer>();
        for (var answer : request.getAnswers()) {
            var q = questionMap.get(answer.getQuestionId());
            if (q == null) continue;
            attemptAnswers.add(QuizAttemptAnswer.builder()
                    .attempt(savedAttempt)
                    .question(q)
                    .selectedAnswer(answer.getSelectedAnswer())
                    .correctAnswer(q.getCorrectAnswer())
                    .isCorrect(q.getCorrectAnswer().equals(answer.getSelectedAnswer()))
                    .build());
        }
        answerRepository.saveAll(attemptAnswers);

        return QuizSubmitResponse.builder()
                .attemptId(savedAttempt.getId())
                .quizId(quizId)
                .correctCount(correct)
                .totalQuestions(total)
                .percentage(percentage)
                .results(results)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttemptResponse> listAttempts(Long userId, Pageable pageable) {
        return attemptRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(a -> QuizAttemptResponse.builder()
                        .id(a.getId())
                        .quizId(a.getQuiz().getId())
                        .quizTitle(a.getQuiz().getTitle())
                        .correctCount(a.getCorrectCount())
                        .totalQuestions(a.getTotalQuestions())
                        .percentage(a.getPercentage())
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .build());
    }
}
