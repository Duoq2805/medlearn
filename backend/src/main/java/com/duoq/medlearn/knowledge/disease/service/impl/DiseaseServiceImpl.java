package com.duoq.medlearn.knowledge.disease.service.impl;

import com.duoq.medlearn.knowledge.category.entity.Category;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.audit.enums.AuditAction;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.knowledge.disease.dto.projection.DiseaseSummaryProjection;
import com.duoq.medlearn.knowledge.disease.dto.request.CreateDiseaseDraftRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.CreateDiseaseRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.DiseaseSearchRequest;
import com.duoq.medlearn.knowledge.disease.dto.request.UpdateDiseaseRequest;
import com.duoq.medlearn.knowledge.disease.dto.response.DiseaseResponse;
import com.duoq.medlearn.knowledge.disease.dto.response.DiseaseDetailResponse;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.disease.mapper.DiseaseMapper;
import com.duoq.medlearn.knowledge.category.repository.CategoryRepository;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.section.service.DiseaseSectionService;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.auth.service.PermissionService;
import com.duoq.medlearn.knowledge.disease.service.DiseaseService;
import com.duoq.medlearn.knowledge.version.service.DiseaseVersionService;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiseaseServiceImpl implements DiseaseService {

    private final DiseaseRepository diseaseRepository;
    private final DiseaseVersionRepository diseaseVersionRepository;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final DiseaseSectionService diseaseSectionService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;
    private final DiseaseVersionService diseaseVersionService;
    private final DiseaseMapper diseaseMapper;
    private final AuditService auditService;
    private final PermissionService permissionService;
    private final DiseaseDraftRepository diseaseDraftRepository;

    @Override
    @Transactional
    public DiseaseResponse createDisease(CreateDiseaseRequest request) {
        if (existsByName(request.getName())) {
            throw new IllegalStateException("Disease name already exists");
        }
        if (existsBySlug(request.getSlug())) {
            throw new IllegalStateException("Disease slug already exists");
        }

        Disease disease = new Disease();
        disease.setName(request.getName());
        disease.setSlug(request.getSlug());
        if (request.getCategoryId() != null) {
            disease.setCategory(resolveCategory(request.getCategoryId()));
        }

        Disease savedDisease = diseaseRepository.save(disease);
        diseaseVersionService.createDraftVersion(savedDisease.getId(), null);
        return diseaseMapper.toDiseaseResponse(savedDisease);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseResponse getDiseaseById(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        return diseaseMapper.toDiseaseResponse(disease);
    }

    

    @Override
    @Transactional
    public DiseaseResponse updateDiseaseMetadata(Long diseaseId, UpdateDiseaseRequest request) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        validateDiseaseOwnership(diseaseId);

        if (request.getName() != null) {
            if (!disease.getName().equals(request.getName()) && existsByName(request.getName())) {
                throw new IllegalStateException("Disease name already exists");
            }
            disease.setName(request.getName());
        }
        // slug is @NaturalId(mutable=false) — never updated after creation
        if (request.getCategoryId() != null) {
            disease.setCategory(resolveCategory(request.getCategoryId()));
        }

        return diseaseMapper.toDiseaseResponse(diseaseRepository.save(disease));
    }

    @Override
    @Transactional
    public DiseaseResponse createDiseaseDraft(CreateDiseaseDraftRequest request) {
        if (existsByName(request.getName())) {
            throw new IllegalStateException("Disease name already exists");
        }
        if (existsBySlug(request.getSlug())) {
            throw new IllegalStateException("Disease slug already exists");
        }

        Disease disease = new Disease();
        disease.setName(request.getName());
        disease.setSlug(request.getSlug());
        if (request.getCategoryId() != null) {
            disease.setCategory(resolveCategory(request.getCategoryId()));
        }

        Disease savedDisease = diseaseRepository.save(disease);
        DiseaseVersionResponse draftVersion = diseaseVersionService.createDraftVersion(savedDisease.getId(), null);
        if (request.getSections() != null) {
            request.getSections().forEach(section -> diseaseSectionService.createSection(draftVersion.getId(), section));
        }
        diseaseDraftRepository.save(DiseaseDraft.builder()
                .disease(savedDisease)
                .title(request.getName())
                .status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL)
                .createdBy(findCurrentUserWithRoles())
                .build());
        return diseaseMapper.toDiseaseResponse(savedDisease);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseDetailResponse getDiseaseBySlug(String slug) {
        Disease disease = diseaseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        validateDiseaseAccessBySlug(slug);
        return buildDiseaseDetail(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseDetailResponse getDiseaseCurrentVersion(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        validateDiseaseAccess(diseaseId);
        return buildDiseaseDetail(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseSummaryProjection> getApprovedDiseases(
            String keyword,
            Long categoryId,
            List<Long> symptomIds,
            Pageable pageable) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return diseaseRepository.findApprovedSummaryByFiltersWithoutSymptomIds(keyword, categoryId, pageable);
        } else {
            return diseaseRepository.findApprovedSummaryByFilters(keyword, categoryId, symptomIds, pageable);
        }
    }

    

    @Override
    @Transactional
    public void softDeleteDisease(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        validateDiseaseOwnership(diseaseId);
        disease.setDeletedAt(OffsetDateTime.now());
        diseaseRepository.save(disease);
        auditService.log(findCurrentUserWithRoles(), AuditAction.DISEASE_DELETED,
                "Disease", diseaseId,
                Map.of("diseaseName", disease.getName()));
    }

    @Override
    @Transactional
    public void restoreDisease(Long diseaseId) {
        // Use findByIdIgnoreDeletedAt to bypass @SQLRestriction for soft-deleted entities
        Disease disease = diseaseRepository.findByIdIgnoreDeletedAt(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        User currentUser = findCurrentUserWithRoles();
        if (!permissionService.hasPermission(PermissionCode.DISEASE_RESTORE)) {
            throw new IllegalStateException("DISEASE_RESTORE permission required");
        }

        disease.setDeletedAt(null);
        diseaseRepository.save(disease);
        auditService.log(currentUser, AuditAction.DISEASE_RESTORED,
                "Disease", diseaseId,
                Map.of("diseaseName", disease.getName()));
    }

    @Override
    @Transactional
    public void assignCategory(Long diseaseId, Long categoryId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        disease.setCategory(resolveCategory(categoryId));
        diseaseRepository.save(disease);
    }

    @Override
    @Transactional
    public void removeCategory(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        disease.setCategory(null);
        diseaseRepository.save(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseSummaryProjection> searchDiseases(DiseaseSearchRequest request, Pageable pageable) {
        return getApprovedDiseases(request.getKeyword(), request.getCategoryId(), request.getSymptomIds(), pageable);
    }

    @Override
    @Transactional
    public DiseaseVersionResponse cloneCurrentVersion(Long diseaseId) {
        return diseaseVersionService.cloneApprovedVersion(diseaseId);
    }

    // ===================================
    // HELPER METHODS
    // ===================================

    private boolean existsByName(String name) {
        return diseaseRepository.existsByName(name);
    }

    private boolean existsBySlug(String slug) {
        return diseaseRepository.findBySlug(slug).isPresent();
    }

    private void validateDiseaseAccess(Long diseaseId) {
        if (!diseaseRepository.existsById(diseaseId)) {
            throw new ResourceNotFoundException("Disease not found");
        }
    }

    private void validateDiseaseAccessBySlug(String slug) {
        if (!diseaseRepository.findBySlug(slug).isPresent()) {
            throw new ResourceNotFoundException("Disease not found");
        }
    }


    /**
     * Ki宄勫儾 tra user hi宄勫檳 t宀奉摨 c璐?quy宄勪苟 qu宀奉柎 l濯?disease hay kh涔坣g.
     *
     * Permission Rules:
     * - REVIEWER v鑴?ADMIN 鑶界摙宄勵柀 bypass ownership check
     * - Contributor ch宄?鑶界摙宄勵柀 qu宀奉柎 l濯?disease c宄勵湩 ch閾唍h m鐭沶h
     *
     * Ownership 鑶界摙宄勵柀 x璋ヽ 鑶藉硠濯檋 b宀风湏g c璋ヽh:
     * - User 鑶借尗 t宄勭幁g t宀奉摰 DiseaseVersion c宄勵湩 disease 鑶借锤
     *
     * @param diseaseId id disease c宀奉湸 ki宄勫儾 tra quy宄勪苟
     *
     * @throws ResourceNotFoundException n宀风赴 disease kh涔坣g t宄勬悏 t宀奉摨
     * @throws IllegalStateException n宀风赴 user kh涔坣g ph宀奉柉 owner
     */
    private void validateDiseaseOwnership(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        User currentUser = findCurrentUserWithRoles();
        // Check if user has DISEASE_MANAGE permission (can bypass ownership)
        if (permissionService.hasPermission(PermissionCode.DISEASE_MANAGE)) {
            return;
        }

        boolean owner = diseaseVersionRepository.existsByDiseaseIdAndCreatedByIdAndDeletedAtIsNull(
                diseaseId,
                currentUser.getId()
        );

        if (!owner) {
            throw new IllegalStateException("Current user is not owner of this disease");
        }
    }

    @Transactional
    private void updateCurrentVersion(Long diseaseId, Long versionId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        if (version.getDisease() == null || !version.getDisease().getId().equals(diseaseId)) {
            throw new IllegalStateException("Version does not belong to disease");
        }

        if (version.getStatus() != VersionStatus.APPROVED) {
            throw new IllegalStateException("Only approved version can be set as current");
        }

        disease.setCurrentVersion(version);
        diseaseRepository.save(disease);
    }

    /**
     * Build full disease detail response.
     *
     * Response bao g宄勬悎:
     * - Disease metadata
     * - Current version
     * - Disease sections
     *
     * 鑶肩摙宄勵柀 s宄?d宄勵櫞g cho:
     * - Public disease detail
     * - Reviewer preview
     * - Contributor preview
     *
     * @param disease disease entity
     * @return disease detail dto ho鑴縩 ch宄勫《h
     */
    private DiseaseDetailResponse buildDiseaseDetail(Disease disease) {
        DiseaseVersion currentVersion = disease.getCurrentVersion();
        DiseaseVersionResponse currentVersionDto = null;

        if (currentVersion != null) {
            currentVersionDto = diseaseMapper.toDiseaseVersionResponse(currentVersion);
        }

        return DiseaseDetailResponse.builder()
                .disease(diseaseMapper.toDiseaseResponse(disease))
                .currentVersion(currentVersionDto)
                .sections(currentVersion == null ? List.of() :
                        diseaseSectionRepository.findAllByVersionIdWithType(currentVersion.getId())
                                .stream()
                                .map(diseaseMapper::toDiseaseSectionResponse)
                                .toList())
                .build();
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private User findCurrentUserWithRoles() {
        Long userId = currentUserResolver.resolveCurrentUserId();
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
