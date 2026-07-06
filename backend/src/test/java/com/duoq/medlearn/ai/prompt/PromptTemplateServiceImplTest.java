package com.duoq.medlearn.ai.prompt;

import com.duoq.medlearn.ai.dto.response.PromptTemplateResponse;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.repository.PromptTemplateRepository;
import com.duoq.medlearn.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptTemplateServiceImplTest {

    @Mock
    private PromptTemplateRepository repository;

    @Captor
    private ArgumentCaptor<PromptTemplate> entityCaptor;

    private PromptTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PromptTemplateServiceImpl(repository);
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        var dto = PromptTemplateResponse.builder()
                .code("test-code")
                .name("Test Prompt")
                .systemPrompt("You are a helpful assistant")
                .userPromptTemplate("Answer: {{question}}")
                .version("1.0")
                .active(true)
                .build();

        when(repository.existsByCodeAndVersion("test-code", "1.0")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            var entity = invocation.<PromptTemplate>getArgument(0);
            entity.setId(1L);
            return entity;
        });

        var result = service.create(dto, null);

        assertThat(result.getCode()).isEqualTo("test-code");
        assertThat(result.getVersion()).isEqualTo("1.0");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void create_shouldThrowWhenDuplicateExists() {
        var dto = PromptTemplateResponse.builder()
                .code("test-code")
                .version("1.0")
                .build();
        when(repository.existsByCodeAndVersion("test-code", "1.0")).thenReturn(true);
        assertThatThrownBy(() -> service.create(dto, null))
                .isInstanceOf(AiConfigurationException.class);
    }

    @Test
    void findByCodeAndVersion_shouldReturnDto() {
        var entity = PromptTemplate.builder()
                .id(1L).code("test").version("1.0")
                .systemPrompt("sys").userPromptTemplate("user")
                .status("ACTIVE")
                .build();
        when(repository.findByCodeAndVersion("test", "1.0")).thenReturn(Optional.of(entity));

        var result = service.findByCodeAndVersion("test", "1.0");
        assertThat(result.getCode()).isEqualTo("test");
    }

    @Test
    void findByCodeAndVersion_shouldThrowWhenNotFound() {
        when(repository.findByCodeAndVersion("missing", "1.0")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByCodeAndVersion("missing", "1.0"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findActiveByCode_shouldReturnActiveDto() {
        var entity = PromptTemplate.builder()
                .id(1L).code("test").version("2.0")
                .systemPrompt("sys").userPromptTemplate("user")
                .status("ACTIVE")
                .build();
        when(repository.findTopByCodeAndStatusOrderByCreatedAtDesc("test", "ACTIVE"))
                .thenReturn(Optional.of(entity));

        var result = service.findActiveByCode("test");
        assertThat(result.getVersion()).isEqualTo("2.0");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void listAll_shouldReturnAllDtos() {
        when(repository.findAll()).thenReturn(List.of(
                PromptTemplate.builder().id(1L).code("a").systemPrompt("s").userPromptTemplate("u").status("ACTIVE").build(),
                PromptTemplate.builder().id(2L).code("b").systemPrompt("s").userPromptTemplate("u").status("ACTIVE").build()
        ));

        assertThat(service.listAll()).hasSize(2);
    }

    @Test
    void delete_shouldRemoveExisting() {
        var entity = PromptTemplate.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        service.delete(1L);
        verify(repository).delete(entity);
    }
}
