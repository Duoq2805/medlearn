package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Category;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;
import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryProjection;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseDraftRequest;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseSearchRequest;
import com.duoq.medlearn.domain.dto.disease.UpdateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseResponse;
import com.duoq.medlearn.domain.dto.disease.DiseaseDetailResponse;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.DiseaseMapper;
import com.duoq.medlearn.repository.CategoryRepository;
import com.duoq.medlearn.repository.DiseaseRepository;
import com.duoq.medlearn.repository.DiseaseSectionRepository;
import com.duoq.medlearn.repository.DiseaseVersionRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.domain.enums.PermissionCode;
import com.duoq.medlearn.service.AuditService;
import com.duoq.medlearn.service.PermissionService;
import com.duoq.medlearn.service.DiseaseService;
import com.duoq.medlearn.service.DiseaseVersionService;
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
    private final CategoryRepository categoryRepository;
    private final CurrentUserResolver currentUserResolver;
    private final UserRepository userRepository;
    private final DiseaseVersionService diseaseVersionService;
    private final DiseaseMapper diseaseMapper;
    private final AuditService auditService;
    private final PermissionService permissionService;

    /**
     * T宀奉摰 disease m宄勬铂 v鑴?kh宄勭剾 t宀奉摰 draft version 鑶藉卜顪?ti閿歯.
     *
     * Workflow:
     * 1. Ki宄勫儾 tra tr闇塶g t閿歯 disease
     * 2. Ki宄勫儾 tra tr闇塶g slug
     * 3. Resolve category
     * 4. T宀奉摰 Disease entity
     * 5. T宀奉摰 DiseaseVersion 鑶藉卜顪?ti閿歯 v宄勬铂 tr宀奉摯g th璋﹊ DRAFT
     * 6. G璋﹏ current version
     *
     * Business Rules:
     * - Disease name ph宀奉柉 unique
     * - Disease slug ph宀奉柉 unique
     * - M宄勬 disease lu涔坣 b宀风椂 鑶藉卜顪?b宀风湏g m宄勬獩 draft version
     * - N宄勬獙 dung disease 鑶界摙宄勵柀 qu宀奉柎 l濯?th涔坣g qua version
     *
     * @param request d宄?li宄勫檽 t宀奉摰 disease
     * @return disease dto 鑶借尗 鑶界摙宄勵柀 t宀奉摰
     *
     * @throws IllegalStateException n宀风赴 t閿歯 ho宀风 slug 鑶借尗 t宄勬悏 t宀奉摨
     * @throws ResourceNotFoundException n宀风赴 category kh涔坣g t宄勬悏 t宀奉摨
     */
    @Override
    @Transactional
    public DiseaseResponse createDisease(CreateDiseaseRequest request) {
        if (existsByName(request.getName())) {
            throw new IllegalStateException("Disease name already exists");
        }
        if (existsBySlug(request.getSlug())) {
            throw new IllegalStateException("Disease slug already exists");
        }

        Category category = resolveCategory(request.getCategoryId());

        Disease disease = Disease.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .category(category)
                .build();

        disease = diseaseRepository.save(disease);

        DiseaseVersionResponse draftVersion = diseaseVersionService.createDraftVersion(
                disease.getId(),
                null
        );

        return diseaseMapper.toDiseaseResponse(disease);
    }

    /**
     * Wrapper method d闇塶g 鑶藉硠?t宀奉摰 disease draft.
     *
     * Method n鑴縴 convert CreateDiseaseDraftRequest
     * sang CreateDiseaseRequest 鑶藉硠?t璋﹊ s宄?d宄勵櫞g logic
     * c宄勵湩 createDisease().
     *
     * M宄勵櫓 鑶介搯ch:
     * - Tr璋﹏h duplicate business logic
     * - Gi宄?workflow t宀奉摰 disease th宄勬唫g nh宀奉櫤
     *
     * @param request request t宀奉摰 draft
     * @return disease dto v宄勭帨 鑶界摙宄勵柀 t宀奉摰
     */

    @Override
    @Transactional
    public DiseaseResponse createDiseaseDraft(CreateDiseaseDraftRequest request) {
        CreateDiseaseRequest createRequest = new CreateDiseaseRequest();
        createRequest.setName(request.getName());
        createRequest.setSlug(request.getSlug());
        createRequest.setCategoryId(request.getCategoryId());
        return createDisease(createRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseResponse getDiseaseById(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        return diseaseMapper.toDiseaseResponse(disease);
    }

    /**
     * L宀奉櫩 chi ti宀风腐 disease theo slug.
     *
     * Th鐡㈠硠婕琯 d闇塶g cho:
     * - Public disease detail page
     * - Disease explorer
     * - SEO-friendly URL
     *
     * Response bao g宄勬悎:
     * - Disease metadata
     * - Current version
     * - Disease sections
     *
     * @param slug slug c宄勵湩 disease
     * @return disease detail dto
     *
     * @throws ResourceNotFoundException n宀风赴 disease kh涔坣g t宄勬悏 t宀奉摨
     */
    @Override
    @Transactional(readOnly = true)
    public DiseaseDetailResponse getDiseaseBySlug(String slug) {
        Disease disease = diseaseRepository.findBySlugWithCategory(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        return buildDiseaseDetail(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseDetailResponse getDiseaseCurrentVersion(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        return buildDiseaseDetail(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseSummaryProjection> getApprovedDiseases(String keyword, Long categoryId, List<Long> symptomIds, Pageable pageable) {
        // Use separate query paths to avoid Hibernate type inference issues with null symptomIds
        if (symptomIds == null || symptomIds.isEmpty()) {
            return diseaseRepository.findApprovedSummaryByFiltersWithoutSymptomIds(keyword, categoryId, pageable);
        }
        return diseaseRepository.findApprovedSummaryByFilters(keyword, categoryId, symptomIds, pageable);
    }

    /**
     * T鐭沵 ki宀风辅 disease theo keyword ho宀风 category.
     *
     * H宄?tr宄?
     * - Search theo t閿歯 disease
     * - Filter theo category
     * - Pagination
     *
     * N宀风赴 c璐?categoryId:
     * -> 鐡 ti閿歯 filter theo category
     *
     * N宀风赴 kh涔坣g c璐?categoryId:
     * -> search theo keyword
     *
     * Ch宄?tr宀?v宄?disease ch鐡 b宄?soft delete.
     *
     * @param request d宄?li宄勫檽 search/filter
     * @param pageable th涔坣g tin pagination
     * @return danh s璋ヽh disease summary
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseSummaryProjection> searchDiseases(DiseaseSearchRequest request, Pageable pageable) {
        String keyword = request != null ? request.getKeyword() : null;
        Long categoryId = request != null ? request.getCategoryId() : null;
        List<Long> symptomIds = request != null ? request.getSymptomIds() : null;

        return diseaseRepository.findSummaryByApprovedFilters(keyword, categoryId, symptomIds, pageable);
    }

    /**
     * C宀风捀 nh宀风捊 metadata c宄勵湩 disease.
     *
     * Ch宄?c宀风捀 nh宀风捊:
     * - name
     * - slug
     * - category
     *
     * Kh涔坣g c宀风捀 nh宀风捊 content disease tr宄勭渹 ti宀风斧.
     * Content ph宀奉柉 鑶界摙宄勵柀 ch宄勫《h s宄勭挦 th涔坣g qua
     * DiseaseVersion workflow.
     *
     * Permission Rules:
     * - Owner 鑶界摙宄勵柀 ph鑼卲 update
     * - REVIEWER/ADMIN bypass ownership check
     *
     * Validation Rules:
     * - Name ph宀奉柉 unique
     * - Slug ph宀奉柉 unique
     *
     * @param diseaseId id disease c宀奉湸 update
     * @param request d宄?li宄勫檽 update
     * @return disease dto sau khi c宀风捀 nh宀风捊
     *
     * @throws ResourceNotFoundException n宀风赴 disease kh涔坣g t宄勬悏 t宀奉摨
     * @throws IllegalStateException n宀风赴 kh涔坣g c璐?quy宄勪苟 update
     */
    @Override
    @Transactional
    public DiseaseResponse updateDiseaseMetadata(Long diseaseId, UpdateDiseaseRequest request) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        validateDiseaseOwnership(diseaseId);

        if (request.getName() != null && !request.getName().equalsIgnoreCase(disease.getName())) {
            if (diseaseRepository.existsByNameAndIdNot(request.getName(), diseaseId)) {
                throw new IllegalStateException("Disease name already exists");
            }
            disease.setName(request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().equalsIgnoreCase(disease.getSlug())) {
            diseaseRepository.findBySlug(request.getSlug())
                    .filter(existing -> !existing.getId().equals(diseaseId))
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Disease slug already exists");
                    });
            disease.setSlug(request.getSlug());
        }

        if (request.getCategoryId() != null) {
            disease.setCategory(resolveCategory(request.getCategoryId()));
        }

        return diseaseMapper.toDiseaseResponse(diseaseRepository.save(disease));
    }

    /**
     * Clone approved version hi宄勫檳 t宀奉摨 th鑴縩h draft m宄勬铂.
     *
     * Workflow:
     * Approved Version
     *      閳?
     * Clone Current Version
     *      閳?
     * Create Draft
     *      閳?
     * Contributor ch宄勫《h s宄勭挦 draft
     *
     * M宄勵櫓 鑶介搯ch:
     * - 鑶煎卜顤?b宀奉柕 approved content immutable
     * - L鐡 to鑴縩 b宄?version history
     * - H宄?tr宄?moderation workflow
     *
     * Business Rules:
     * - Kh涔坣g 鑶界摙宄勵柀 edit approved version tr宄勭渹 ti宀风斧
     * - M宄勫常 thay 鑶藉硠鏄?ph宀奉柉 t宀奉摰 version m宄勬铂
     *
     * @param diseaseId id disease c宀奉湸 clone version
     * @return draft version m宄勬铂 鑶界摙宄勵柀 t宀奉摰
     */
    @Override
    public DiseaseVersionResponse cloneCurrentVersion(Long diseaseId) {
        return diseaseVersionService.cloneApprovedVersion(diseaseId);
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
