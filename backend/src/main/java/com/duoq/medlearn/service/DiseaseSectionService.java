package com.duoq.medlearn.service;

import com.duoq.medlearn.dto.request.CreateDiseaseSectionRequest;
import com.duoq.medlearn.dto.request.SectionOrderRequest;
import com.duoq.medlearn.dto.request.UpdateDiseaseSectionRequest;
import com.duoq.medlearn.dto.response.DiseaseSectionDTO;
import com.duoq.medlearn.dto.response.SectionTemplateDTO;
import com.duoq.medlearn.dto.response.SectionTypeDTO;

import java.util.List;

public interface DiseaseSectionService {

    // ================================
    // CREATE
    // ================================

    DiseaseSectionDTO createSection(
            Long versionId,
            CreateDiseaseSectionRequest request
    );

    List<DiseaseSectionDTO> createSections(
            Long versionId,
            List<CreateDiseaseSectionRequest> requests
    );

    // ================================
    // READ
    // ================================

    DiseaseSectionDTO getSectionById(Long sectionId);

    List<DiseaseSectionDTO> getSectionsByVersion(Long versionId);

    DiseaseSectionDTO getSectionByType(
            Long versionId,
            String sectionType
    );

    // ================================
    // UPDATE
    // ================================

    DiseaseSectionDTO updateSection(
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

    boolean existsSectionType(
            Long versionId,
            Long sectionTypeId
    );

    boolean isRequiredSection(String sectionType);

    void validateSectionEditable(Long sectionId);

    // ================================
    // CONTENT
    // ================================

    String renderMarkdownContent(Long sectionId);

    String sanitizeHtmlContent(String content);

    // ================================
    // TEMPLATE
    // ================================

    List<SectionTypeDTO> getAllSectionTypes();

    List<SectionTemplateDTO> getDefaultTemplates();
}