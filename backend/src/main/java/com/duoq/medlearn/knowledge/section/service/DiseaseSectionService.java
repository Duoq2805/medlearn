package com.duoq.medlearn.knowledge.section.service;

import com.duoq.medlearn.knowledge.section.dto.request.CreateDiseaseSectionRequest;
import com.duoq.medlearn.knowledge.section.dto.request.SectionOrderRequest;
import com.duoq.medlearn.knowledge.section.dto.request.UpdateDiseaseSectionRequest;
import com.duoq.medlearn.knowledge.section.dto.response.DiseaseSectionResponse;
import com.duoq.medlearn.knowledge.section.dto.response.SectionTemplateResponse;
import com.duoq.medlearn.knowledge.section.dto.response.SectionTypeResponse;

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