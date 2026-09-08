package com.duoq.medlearn.quiz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parses AI JSON into quiz questions.
 * AI must return: {"questions": [{"content":"...","optionA":"...","optionB":"...","optionC":"...","optionD":"...","correctAnswer":"A","explanation":"..."}]}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonQuizParser {

    private static final Set<String> VALID_ANSWERS = Set.of("A", "B", "C", "D");

    private final ObjectMapper objectMapper;

    public record ParsedQuestion(
            String content, String optionA, String optionB,
            String optionC, String optionD, String correctAnswer, String explanation
    ) {}

    public List<ParsedQuestion> parse(String json) {
        if (json == null || json.isBlank()) {
            log.warn("Empty JSON from AI quiz generation");
            return List.of();
        }
        try {
            var root = objectMapper.readTree(json);
            var questionsNode = root.get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                log.warn("AI response missing 'questions' array");
                return List.of();
            }
            var result = new ArrayList<ParsedQuestion>();
            for (JsonNode node : questionsNode) {
                var q = parseSingle(node);
                if (q != null) result.add(q);
            }
            return result;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI quiz JSON: {}", e.getMessage());
            return List.of();
        }
    }

    private ParsedQuestion parseSingle(JsonNode node) {
        var content    = text(node, "content");
        var optionA    = text(node, "optionA");
        var optionB    = text(node, "optionB");
        var optionC    = text(node, "optionC");
        var optionD    = text(node, "optionD");
        var correct    = text(node, "correctAnswer");
        var explanation = text(node, "explanation");

        if (content == null || optionA == null || optionB == null
                || optionC == null || optionD == null || correct == null) {
            log.warn("Skipping question with missing fields");
            return null;
        }
        correct = correct.trim().toUpperCase();
        if (!VALID_ANSWERS.contains(correct)) {
            log.warn("Skipping question with invalid correctAnswer: {}", correct);
            return null;
        }
        return new ParsedQuestion(content.trim(), optionA.trim(), optionB.trim(),
                optionC.trim(), optionD.trim(), correct,
                explanation != null ? explanation.trim() : null);
    }

    private String text(JsonNode node, String field) {
        var n = node.get(field);
        if (n == null || n.isNull()) return null;
        var v = n.asText(null);
        return (v == null || v.isBlank()) ? null : v;
    }
}
