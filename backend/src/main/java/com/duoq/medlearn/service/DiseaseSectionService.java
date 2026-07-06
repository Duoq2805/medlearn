package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.section.CreateDiseaseSectionRequest;
import com.duoq.medlearn.domain.dto.section.SectionOrderRequest;
import com.duoq.medlearn.domain.dto.section.UpdateDiseaseSectionRequest;
import com.duoq.medlearn.domain.dto.section.DiseaseSectionResponse;
import com.duoq.medlearn.domain.dto.section.SectionTemplateResponse;
import com.duoq.medlearn.domain.dto.section.SectionTypeResponse;

import java.util.List;

public interface DiseaseSectionService {

    // ================================
    // CREATE
    // ================================

    DiseaseSectionResponse createSection(
            Long versionId,
            CreateDiseaseSectionRequest request
    );

    List<DiseaseSectionResponse> createSections(
            Long versionId,
            List<CreateDiseaseSectionRequest> requests
    );

    // ================================
    // READ
    // ================================

    DiseaseSectionResponse getSectionById(Long sectionId);

    List<DiseaseSectionResponse> getSectionsByVersion(Long versionId);

    DiseaseSectionResponse getSectionByType(
            Long versionId,
            String sectionType
    );

    // ================================
    // UPDATE
    // ================================

    DiseaseSectionResponse updateSection(
            Long sectionId,
            UpdateDiseaseSectionRequest request
    );

    void reorderSections(
            Long versionId,
            List<SectionOrderRequest> requests
    );

    // ================================
    // DELETE
    // ================================

    void deleteSection(Long sectionId);

    void softDeleteSectionsByVersion(Long versionId);

    // ================================
    // VALIDATION
    // ================================

    void validateRequiredSections(Long versionId);

    // ================================
    // CONTENT
    // ================================

    String renderMarkdownContent(Long sectionId);

    // ================================
    // TEMPLATE
    // ================================

    List<SectionTypeResponse> getAllSectionTypes();

    List<SectionTemplateResponse> getDefaultTemplates();
}