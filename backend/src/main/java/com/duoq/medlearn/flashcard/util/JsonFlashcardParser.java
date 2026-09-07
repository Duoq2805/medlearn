package com.duoq.medlearn.flashcard.util;

import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses structured JSON from AI responses into flashcard data.
 * AI must return: {"flashcards": [{"question": "...", "answer": "...", "explanation": "..."}]}
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonFlashcardParser {

    private final ObjectMapper objectMapper;

    public record ParsedCard(String question, String answer, String explanation, String source) {}

    /**
     * Parse JSON string into a list of flashcard data.
     * Returns empty list on failure (never null).
     */
    public List<ParsedCard> parse(String json) {
        if (json == null || json.isBlank()) {
            log.warn("Empty JSON response from AI");
            return List.of();
        }

        try {
            var root = objectMapper.readTree(json);
            var cardsNode = root.get("flashcards");
            if (cardsNode == null || !cardsNode.isArray()) {
                log.warn("AI response missing 'flashcards' array");
                return List.of();
            }

            var cards = new ArrayList<ParsedCard>();
            for (JsonNode node : cardsNode) {
                var card = parseSingle(node);
                if (card != null) {
                    cards.add(card);
                }
            }
            return cards;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI JSON response: {}", e.getMessage());
            return List.of();
        }
    }

    private ParsedCard parseSingle(JsonNode node) {
        var question = node.has("question") ? node.get("question").asText(null) : null;
        var answer = node.has("answer") ? node.get("answer").asText(null) : null;

        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            log.warn("Skipping card with missing question or answer");
            return null;
        }

        var explanation = node.has("explanation") ? node.get("explanation").asText(null) : null;
        var source = node.has("source") ? node.get("source").asText(null) : null;

        return new ParsedCard(question.trim(), answer.trim(),
                explanation != null ? explanation.trim() : null,
                source != null ? source.trim() : null);
    }
}
