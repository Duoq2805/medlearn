package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.section.CreateDiseaseSectionRequest;
import com.duoq.medlearn.domain.dto.section.SectionOrderRequest;
import com.duoq.medlearn.domain.dto.section.UpdateDiseaseSectionRequest;
import com.duoq.medlearn.domain.dto.section.DiseaseSectionDTO;
import com.duoq.medlearn.domain.dto.section.SectionTemplateDTO;
import com.duoq.medlearn.domain.dto.section.SectionTypeDTO;

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

    // ================================
    // CONTENT
    // ================================

    String renderMarkdownContent(Long sectionId);

    // ================================
    // TEMPLATE
    // ================================

    List<SectionTypeDTO> getAllSectionTypes();

    List<SectionTemplateDTO> getDefaultTemplates();
}