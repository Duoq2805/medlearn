package com.duoq.medlearn.ai.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptRendererTest {

    private PromptRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new PromptRenderer();
    }

    @Test
    void render_shouldReplaceSimpleVariables() {
        var template = "Hello {{name}}, your score is {{score}}";
        var vars = Map.of("name", "Alice", "score", "95");
        assertThat(renderer.render(template, vars)).isEqualTo("Hello Alice, your score is 95");
    }

    @Test
    void render_shouldHandleMissingVariableAsEmpty() {
        var template = "Hello {{name}}";
        var vars = Map.of("other", "value");
        assertThat(renderer.render(template, vars)).isEqualTo("Hello ");
    }

    @Test
    void render_shouldHandleNullTemplate() {
        assertThat(renderer.render(null, Map.of())).isEqualTo("");
    }

    @Test
    void render_shouldHandleNullVariables() {
        assertThat(renderer.render("static text", null)).isEqualTo("static text");
    }

    @Test
    void render_shouldHandleWhitespaceInBraces() {
        var template = "Hello {{ name }}";
        assertThat(renderer.render(template, Map.of("name", "Bob"))).isEqualTo("Hello Bob");
    }

    @Test
    void hasUnresolvedVariables_shouldReturnTrueWhenVariablesExist() {
        assertThat(renderer.hasUnresolvedVariables("Hello {{name}}")).isTrue();
    }

    @Test
    void hasUnresolvedVariables_shouldReturnFalseWhenNoVariables() {
        assertThat(renderer.hasUnresolvedVariables("Hello world")).isFalse();
    }
}
