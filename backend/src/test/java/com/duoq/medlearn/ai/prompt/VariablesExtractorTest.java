package com.duoq.medlearn.ai.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VariablesExtractorTest {

    private VariablesExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new VariablesExtractor();
    }

    @Test
    void extract_shouldFindVariables() {
        var vars = extractor.extract("Hello {{name}}, your {{role}} is ready");
        assertThat(vars).containsExactly("name", "role");
    }

    @Test
    void extract_shouldHandleWhitespace() {
        var vars = extractor.extract("Hello {{ name }}");
        assertThat(vars).containsExactly("name");
    }

    @Test
    void extract_shouldReturnEmptyForNoVariables() {
        assertThat(extractor.extract("static text")).isEmpty();
    }

    @Test
    void extract_shouldReturnEmptyForNull() {
        assertThat(extractor.extract(null)).isEmpty();
    }

    @Test
    void extract_shouldNotDuplicate() {
        var vars = extractor.extract("{{name}} hello {{name}}");
        assertThat(vars).containsExactly("name");
    }
}
