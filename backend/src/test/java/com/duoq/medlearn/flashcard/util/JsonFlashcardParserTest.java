package com.duoq.medlearn.flashcard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonFlashcardParserTest {

    private JsonFlashcardParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonFlashcardParser(new ObjectMapper());
    }

    @Test
    void validJson_ShouldParseCards() {
        var json = """
                {"flashcards": [
                  {"question": "Q1?", "answer": "A1", "explanation": "E1", "source": "S1"},
                  {"question": "Q2?", "answer": "A2", "explanation": "E2", "source": "S2"}
                ]}
                """;
        var cards = parser.parse(json);
        assertEquals(2, cards.size());
        assertEquals("Q1?", cards.get(0).question());
        assertEquals("A1", cards.get(0).answer());
        assertEquals("E1", cards.get(0).explanation());
        assertEquals("S1", cards.get(0).source());
    }

    @Test
    void missingExplanation_ShouldStillParse() {
        var json = """
                {"flashcards": [
                  {"question": "Q?", "answer": "A"}
                ]}
                """;
        var cards = parser.parse(json);
        assertEquals(1, cards.size());
        assertNull(cards.get(0).explanation());
        assertNull(cards.get(0).source());
    }

    @Test
    void blankQuestion_ShouldSkipCard() {
        var json = """
                {"flashcards": [
                  {"question": "", "answer": "A"},
                  {"question": "Q2?", "answer": "A2"}
                ]}
                """;
        var cards = parser.parse(json);
        assertEquals(1, cards.size());
        assertEquals("Q2?", cards.get(0).question());
    }

    @Test
    void missingFlashcardsKey_ShouldReturnEmpty() {
        var json = "{}";
        var cards = parser.parse(json);
        assertTrue(cards.isEmpty());
    }

    @Test
    void malformedJson_ShouldReturnEmpty() {
        var cards = parser.parse("{bad json}");
        assertTrue(cards.isEmpty());
    }

    @Test
    void nullInput_ShouldReturnEmpty() {
        var cards = parser.parse(null);
        assertTrue(cards.isEmpty());
    }

    @Test
    void emptyArray_ShouldReturnEmpty() {
        var json = "{\"flashcards\": []}";
        var cards = parser.parse(json);
        assertTrue(cards.isEmpty());
    }

    @Test
    void aiRefusalText_ShouldReturnEmpty() {
        var json = "I am an AI assistant and cannot generate medical content.";
        var cards = parser.parse(json);
        assertTrue(cards.isEmpty());
    }
}
