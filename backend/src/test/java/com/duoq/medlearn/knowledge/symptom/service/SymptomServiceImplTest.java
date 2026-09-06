package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.dto.symptom.CreateSymptomRequest;
import com.duoq.medlearn.domain.dto.symptom.SymptomResponse;
import com.duoq.medlearn.domain.dto.symptom.UpdateSymptomRequest;
import com.duoq.medlearn.domain.entity.Symptom;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.SymptomMapper;
import com.duoq.medlearn.repository.SymptomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomServiceImplTest {

    @Mock private SymptomRepository symptomRepository;
    @Mock private SymptomMapper symptomMapper;
    @InjectMocks private SymptomServiceImpl symptomService;

    private Symptom testSymptom;
    private SymptomResponse testSymptomResponse;

    @BeforeEach
    void setUp() {
        testSymptom = Symptom.builder()
                .id(1L)
                .name("Fever")
                .slug("fever")
                .description("Elevated body temperature")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        testSymptomResponse = new SymptomResponse(
                1L, "Fever", "fever", "Elevated body temperature",
                OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    @Test
    void getAllSymptoms_shouldReturnList() {
        when(symptomRepository.findAll()).thenReturn(List.of(testSymptom));
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        List<SymptomResponse> result = symptomService.getAllSymptoms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fever");
        verify(symptomRepository).findAll();
    }

    @Test
    void getAllSymptoms_shouldReturnEmptyList_whenNoneExist() {
        when(symptomRepository.findAll()).thenReturn(List.of());

        List<SymptomResponse> result = symptomService.getAllSymptoms();

        assertThat(result).isEmpty();
    }

    @Test
    void getSymptomById_shouldReturnSymptom() {
        when(symptomRepository.findById(1L)).thenReturn(Optional.of(testSymptom));
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        SymptomResponse result = symptomService.getSymptomById(1L);

        assertThat(result.getName()).isEqualTo("Fever");
        verify(symptomRepository).findById(1L);
    }

    @Test
    void getSymptomById_shouldThrow_whenNotFound() {
        when(symptomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> symptomService.getSymptomById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Symptom not found");
    }

    @Test
    void searchSymptoms_shouldReturnMatchingResults() {
        Symptom cough = Symptom.builder().id(2L).name("Cough").slug("cough").build();
        when(symptomRepository.findAll()).thenReturn(List.of(testSymptom, cough));
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        List<SymptomResponse> result = symptomService.searchSymptoms("fever");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fever");
    }

    @Test
    void searchSymptoms_shouldReturnAll_whenQueryIsBlank() {
        when(symptomRepository.findAll()).thenReturn(List.of(testSymptom));
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        List<SymptomResponse> result = symptomService.searchSymptoms("  ");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchSymptoms_shouldReturnAll_whenQueryIsNull() {
        when(symptomRepository.findAll()).thenReturn(List.of(testSymptom));
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        List<SymptomResponse> result = symptomService.searchSymptoms(null);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchSymptoms_shouldMatchDescription() {
        Symptom match = Symptom.builder().id(2L).name("Cough").slug("cough")
                .description("Elevated symptom").build();
        SymptomResponse matchDTO = new SymptomResponse(
                2L, "Cough", "cough", "Elevated symptom",
                null, null
        );
        when(symptomRepository.findAll()).thenReturn(List.of(match));
        when(symptomMapper.toSymptomResponse(match)).thenReturn(matchDTO);

        List<SymptomResponse> result = symptomService.searchSymptoms("elevated");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cough");
    }

    @Test
    void createSymptom_shouldSucceed() {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("Fever");
        request.setDescription("Elevated body temperature");

        when(symptomRepository.existsByName("Fever")).thenReturn(false);
        when(symptomRepository.existsBySlug("fever")).thenReturn(false);
        when(symptomRepository.save(any(Symptom.class))).thenReturn(testSymptom);
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        SymptomResponse result = symptomService.createSymptom(request);

        assertThat(result.getName()).isEqualTo("Fever");
        verify(symptomRepository).save(any(Symptom.class));
    }

    @Test
    void createSymptom_shouldFail_whenNameExists() {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("Fever");

        when(symptomRepository.existsByName("Fever")).thenReturn(true);

        assertThatThrownBy(() -> symptomService.createSymptom(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
        verify(symptomRepository, never()).save(any());
    }

    @Test
    void createSymptom_shouldFail_whenSlugExists() {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("Fever");

        when(symptomRepository.existsByName("Fever")).thenReturn(false);
        when(symptomRepository.existsBySlug("fever")).thenReturn(true);

        assertThatThrownBy(() -> symptomService.createSymptom(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("slug already exists");
        verify(symptomRepository, never()).save(any());
    }

    @Test
    void createSymptom_shouldHandleSpecialCharactersInSlug() {
        CreateSymptomRequest request = new CreateSymptomRequest();
        request.setName("High Fever!!!");

        when(symptomRepository.existsByName("High Fever!!!")).thenReturn(false);
        when(symptomRepository.existsBySlug("high-fever")).thenReturn(false);

        Symptom saved = Symptom.builder().id(3L).name("High Fever!!!").slug("high-fever").build();
        SymptomResponse dto = new SymptomResponse(3L, "High Fever!!!", "high-fever", null, null, null);
        when(symptomRepository.save(any(Symptom.class))).thenReturn(saved);
        when(symptomMapper.toSymptomResponse(saved)).thenReturn(dto);

        SymptomResponse result = symptomService.createSymptom(request);

        assertThat(result.getSlug()).isEqualTo("high-fever");
    }

    @Test
    void updateSymptom_shouldUpdateName() {
        UpdateSymptomRequest request = new UpdateSymptomRequest();
        request.setName("High Fever");

        when(symptomRepository.findById(1L)).thenReturn(Optional.of(testSymptom));
        when(symptomRepository.existsByName("High Fever")).thenReturn(false);
        when(symptomRepository.existsBySlug("high-fever")).thenReturn(false);
        when(symptomRepository.save(any(Symptom.class))).thenReturn(testSymptom);
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        symptomService.updateSymptom(1L, request);

        assertThat(testSymptom.getName()).isEqualTo("High Fever");
        assertThat(testSymptom.getSlug()).isEqualTo("high-fever");
    }

    @Test
    void updateSymptom_shouldUpdateDescription() {
        UpdateSymptomRequest request = new UpdateSymptomRequest();
        request.setDescription("Very high body temperature");

        when(symptomRepository.findById(1L)).thenReturn(Optional.of(testSymptom));
        when(symptomRepository.save(any(Symptom.class))).thenReturn(testSymptom);
        when(symptomMapper.toSymptomResponse(testSymptom)).thenReturn(testSymptomResponse);

        symptomService.updateSymptom(1L, request);

        assertThat(testSymptom.getDescription()).isEqualTo("Very high body temperature");
    }

    @Test
    void updateSymptom_shouldFail_whenNameExists() {
        UpdateSymptomRequest request = new UpdateSymptomRequest();
        request.setName("Cough");

        testSymptom.setName("Fever");
        when(symptomRepository.findById(1L)).thenReturn(Optional.of(testSymptom));
        when(symptomRepository.existsByName("Cough")).thenReturn(true);

        assertThatThrownBy(() -> symptomService.updateSymptom(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name already exists");
    }

    @Test
    void updateSymptom_shouldThrow_whenNotFound() {
        when(symptomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> symptomService.updateSymptom(999L, new UpdateSymptomRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Symptom not found");
    }

    @Test
    void deleteSymptom_shouldSucceed() {
        when(symptomRepository.existsById(1L)).thenReturn(true);

        symptomService.deleteSymptom(1L);

        verify(symptomRepository).deleteById(1L);
    }

    @Test
    void deleteSymptom_shouldThrow_whenNotFound() {
        when(symptomRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> symptomService.deleteSymptom(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Symptom not found");
        verify(symptomRepository, never()).deleteById(any());
    }
}
