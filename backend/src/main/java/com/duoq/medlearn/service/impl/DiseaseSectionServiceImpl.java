package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.DiseaseSection;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.entity.SectionType;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.dto.section.CreateDiseaseSectionRequest;
import com.duoq.medlearn.domain.dto.section.SectionOrderRequest;
import com.duoq.medlearn.domain.dto.section.UpdateDiseaseSectionRequest;
import com.duoq.medlearn.domain.dto.section.DiseaseSectionDTO;
import com.duoq.medlearn.domain.dto.section.SectionTemplateDTO;
import com.duoq.medlearn.domain.dto.section.SectionTypeDTO;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.DiseaseMapper;
import com.duoq.medlearn.repository.DiseaseSectionRepository;
import com.duoq.medlearn.repository.DiseaseVersionRepository;
import com.duoq.medlearn.repository.SectionTypeRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.domain.enums.PermissionCode;
import com.duoq.medlearn.service.DiseaseSectionService;
import com.duoq.medlearn.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiseaseSectionServiceImpl implements DiseaseSectionService {

    private final DiseaseSectionRepository diseaseSectionRepository;
    private final DiseaseVersionRepository diseaseVersionRepository;
    private final SectionTypeRepository sectionTypeRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;
    private final DiseaseMapper diseaseMapper;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public DiseaseSectionDTO createSection(Long versionId, CreateDiseaseSectionRequest request) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));
        validateVersionEditable(version);
        SectionType sectionType = sectionTypeRepository.findById(request.getSectionTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Section type not found"));

        DiseaseSection section = DiseaseSection.builder()
                .diseaseVersion(version)
                .sectionType(sectionType)
                .title(request.getTitle())
                .content(sanitizeHtmlContent(request.getContent()))
                .orderIndex(request.getOrderIndex() == null ? 0 : request.getOrderIndex())
                .build();

        return diseaseMapper.toDiseaseSectionDTO(diseaseSectionRepository.save(section));
    }

    @Override
    @Transactional
    public List<DiseaseSectionDTO> createSections(Long versionId, List<CreateDiseaseSectionRequest> requests) {
        return requests.stream()
                .map(req -> createSection(versionId, req))
                .toList();
    }

    @Override
    public DiseaseSectionDTO getSectionById(Long sectionId) {
        DiseaseSection section = diseaseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease section not found"));
        return diseaseMapper.toDiseaseSectionDTO(section);
    }

    @Override
    public List<DiseaseSectionDTO> getSectionsByVersion(Long versionId) {
        return diseaseSectionRepository.findAllByDiseaseVersionIdAndDeletedAtIsNullOrderByOrderIndexAsc(versionId)
                .stream()
                .map(diseaseMapper::toDiseaseSectionDTO)
                .toList();
    }

    @Override
    public DiseaseSectionDTO getSectionByType(Long versionId, String sectionType) {
        return diseaseSectionRepository.findAllByVersionIdWithType(versionId).stream()
                .filter(section -> section.getSectionType() != null
                        && sectionType.equalsIgnoreCase(section.getSectionType().getName()))
                .findFirst()
                .map(diseaseMapper::toDiseaseSectionDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Section type not found"));
    }

    @Override
    @Transactional
    public DiseaseSectionDTO updateSection(Long sectionId, UpdateDiseaseSectionRequest request) {
        DiseaseSection section = diseaseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease section not found"));
        validateVersionEditable(section.getDiseaseVersion());

        if (request.getSectionTypeId() != null) {
            SectionType sectionType = sectionTypeRepository.findById(request.getSectionTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section type not found"));
            section.setSectionType(sectionType);
        }
        if (request.getTitle() != null) {
            section.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            section.setContent(sanitizeHtmlContent(request.getContent()));
        }
        if (request.getOrderIndex() != null) {
            section.setOrderIndex(request.getOrderIndex());
        }

        return diseaseMapper.toDiseaseSectionDTO(diseaseSectionRepository.save(section));
    }

    @Override
    @Transactional
    public void reorderSections(Long versionId, List<SectionOrderRequest> requests) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));
        validateVersionEditable(version);

        for (SectionOrderRequest req : requests) {
            DiseaseSection section = diseaseSectionRepository.findById(req.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disease section not found"));
            if (section.getDiseaseVersion() == null || !section.getDiseaseVersion().getId().equals(versionId)) {
                throw new IllegalStateException("Section does not belong to version");
            }
            section.setOrderIndex(req.getOrderIndex());
            diseaseSectionRepository.save(section);
        }
    }

    @Override
    @Transactional
    public void deleteSection(Long sectionId) {
        DiseaseSection section = diseaseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease section not found"));
        validateVersionEditable(section.getDiseaseVersion());
        section.setDeletedAt(OffsetDateTime.now());
        diseaseSectionRepository.save(section);
    }

    @Override
    @Transactional
    public void softDeleteSectionsByVersion(Long versionId) {
        diseaseSectionRepository.findAllByDiseaseVersionIdAndDeletedAtIsNullOrderByOrderIndexAsc(versionId)
                .forEach(section -> {
                    section.setDeletedAt(OffsetDateTime.now());
                    diseaseSectionRepository.save(section);
                });
    }

    @Override
    public String renderMarkdownContent(Long sectionId) {
        DiseaseSection section = diseaseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease section not found"));
        return section.getContent() == null ? "" : section.getContent();
    }

    @Override
    public List<SectionTypeDTO> getAllSectionTypes() {
        return sectionTypeRepository.findAll().stream()
                .map(type -> SectionTypeDTO.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .description(type.getDescription())
                        .build())
                .toList();
    }

    @Override
    public List<SectionTemplateDTO> getDefaultTemplates() {
        return List.of(
                SectionTemplateDTO.builder().sectionType("definition").title("Definition").template("Định nghĩa bệnh...").build(),
                SectionTemplateDTO.builder().sectionType("symptoms").title("Symptoms").template("Triệu chứng chính...").build(),
                SectionTemplateDTO.builder().sectionType("causes").title("Causes").template("Nguyên nhân...").build(),
                SectionTemplateDTO.builder().sectionType("diagnosis").title("Diagnosis").template("Chẩn đoán...").build(),
                SectionTemplateDTO.builder().sectionType("treatment").title("Treatment").template("Điều trị...").build(),
                SectionTemplateDTO.builder().sectionType("prevention").title("Prevention").template("Phòng ngừa...").build()
        );
    }

    // ===================================
    // HELPER METHODS
    // ===================================

    @Override
    public void validateRequiredSections(Long versionId) {
        List<String> required = List.of("Definition", "Symptoms", "Treatment");
        List<String> existing = diseaseSectionRepository.findAllByVersionIdWithType(versionId).stream()
                .map(section -> section.getSectionType() != null ? section.getSectionType().getName() : null)
                .filter(name -> name != null)
                .toList();

        List<String> missing = required.stream()
                .filter(req -> existing.stream().noneMatch(req::equalsIgnoreCase))
                .toList();

        if (!missing.isEmpty()) {
            String details = missing.stream()
                    .map(s -> "- " + s)
                    .collect(java.util.stream.Collectors.joining("\n"));
            throw new IllegalStateException(
                    "Cannot submit version for review.\nMissing required sections:\n" + details
            );
        }
    }

    private boolean existsSectionType(Long versionId, Long sectionTypeId) {
        return diseaseSectionRepository.findAllByVersionIdWithType(versionId).stream()
                .anyMatch(section -> section.getSectionType() != null
                        && section.getSectionType().getId().longValue() == sectionTypeId.longValue());
    }

    private boolean isRequiredSection(String sectionType) {
        return List.of("definition", "symptoms", "treatment").stream()
                .anyMatch(required -> required.equalsIgnoreCase(sectionType));
    }

    private void validateSectionEditable(Long sectionId) {
        DiseaseSection section = diseaseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease section not found"));
        validateVersionEditable(section.getDiseaseVersion());
    }

    private String sanitizeHtmlContent(String content) {
        if (content == null) {
            return "";
        }
        return Jsoup.clean(content, Safelist.basic());
    }

    private void validateVersionEditable(DiseaseVersion version) {
        if (version == null) {
            throw new IllegalStateException("Disease version not found");
        }
        if (version.getStatus() != com.duoq.medlearn.domain.enums.VersionStatus.DRAFT) {
            throw new IllegalStateException("Section is not editable because version is not draft");
        }

        User currentUser = findCurrentUserWithRoles();
        // Check if user has SECTION_EDIT_ANY permission (can bypass ownership)
        if (permissionService.hasPermission(PermissionCode.SECTION_EDIT_ANY)) {
            return;
        }

        if (version.getCreatedBy() == null || !version.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Current user is not owner of this version");
        }
    }

    private User findCurrentUserWithRoles() {
        Long userId = currentUserResolver.resolveCurrentUserId();
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

}
