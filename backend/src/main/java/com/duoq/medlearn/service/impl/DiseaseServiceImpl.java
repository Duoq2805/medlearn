package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Category;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;
import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseDraftRequest;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseSearchRequest;
import com.duoq.medlearn.domain.dto.disease.UpdateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseDTO;
import com.duoq.medlearn.domain.dto.disease.DiseaseDetailDTO;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
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
     * Tạo disease mới và khởi tạo draft version đầu tiên.
     *
     * Workflow:
     * 1. Kiểm tra trùng tên disease
     * 2. Kiểm tra trùng slug
     * 3. Resolve category
     * 4. Tạo Disease entity
     * 5. Tạo DiseaseVersion đầu tiên với trạng thái DRAFT
     * 6. Gán current version
     *
     * Business Rules:
     * - Disease name phải unique
     * - Disease slug phải unique
     * - Mỗi disease luôn bắt đầu bằng một draft version
     * - Nội dung disease được quản lý thông qua version
     *
     * @param request dữ liệu tạo disease
     * @return disease dto đã được tạo
     *
     * @throws IllegalStateException nếu tên hoặc slug đã tồn tại
     * @throws ResourceNotFoundException nếu category không tồn tại
     */
    @Override
    @Transactional
    public DiseaseDTO createDisease(CreateDiseaseRequest request) {
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

        DiseaseVersionDTO draftVersion = diseaseVersionService.createDraftVersion(
                disease.getId(),
                null
        );

        return diseaseMapper.toDiseaseDTO(disease);
    }

    /**
     * Wrapper method dùng để tạo disease draft.
     *
     * Method này convert CreateDiseaseDraftRequest
     * sang CreateDiseaseRequest để tái sử dụng logic
     * của createDisease().
     *
     * Mục đích:
     * - Tránh duplicate business logic
     * - Giữ workflow tạo disease thống nhất
     *
     * @param request request tạo draft
     * @return disease dto vừa được tạo
     */

    @Override
    @Transactional
    public DiseaseDTO createDiseaseDraft(CreateDiseaseDraftRequest request) {
        CreateDiseaseRequest createRequest = new CreateDiseaseRequest();
        createRequest.setName(request.getName());
        createRequest.setSlug(request.getSlug());
        createRequest.setCategoryId(request.getCategoryId());
        return createDisease(createRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseDTO getDiseaseById(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        return diseaseMapper.toDiseaseDTO(disease);
    }

    /**
     * Lấy chi tiết disease theo slug.
     *
     * Thường dùng cho:
     * - Public disease detail page
     * - Disease explorer
     * - SEO-friendly URL
     *
     * Response bao gồm:
     * - Disease metadata
     * - Current version
     * - Disease sections
     *
     * @param slug slug của disease
     * @return disease detail dto
     *
     * @throws ResourceNotFoundException nếu disease không tồn tại
     */
    @Override
    @Transactional(readOnly = true)
    public DiseaseDetailDTO getDiseaseBySlug(String slug) {
        Disease disease = diseaseRepository.findBySlugWithCategory(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        return buildDiseaseDetail(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseDetailDTO getDiseaseCurrentVersion(Long diseaseId) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        return buildDiseaseDetail(disease);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseSummaryDTO> getApprovedDiseases(String keyword, Long categoryId, List<Long> symptomIds, Pageable pageable) {
        // Use separate query paths to avoid Hibernate type inference issues with null symptomIds
        if (symptomIds == null || symptomIds.isEmpty()) {
            return diseaseRepository.findApprovedSummaryByFiltersWithoutSymptomIds(keyword, categoryId, pageable);
        }
        return diseaseRepository.findApprovedSummaryByFilters(keyword, categoryId, symptomIds, pageable);
    }

    /**
     * Tìm kiếm disease theo keyword hoặc category.
     *
     * Hỗ trợ:
     * - Search theo tên disease
     * - Filter theo category
     * - Pagination
     *
     * Nếu có categoryId:
     * -> ưu tiên filter theo category
     *
     * Nếu không có categoryId:
     * -> search theo keyword
     *
     * Chỉ trả về disease chưa bị soft delete.
     *
     * @param request dữ liệu search/filter
     * @param pageable thông tin pagination
     * @return danh sách disease summary
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseSummaryDTO> searchDiseases(DiseaseSearchRequest request, Pageable pageable) {
        String keyword = request != null ? request.getKeyword() : null;
        Long categoryId = request != null ? request.getCategoryId() : null;
        List<Long> symptomIds = request != null ? request.getSymptomIds() : null;

        return diseaseRepository.findSummaryByApprovedFilters(keyword, categoryId, symptomIds, pageable);
    }

    /**
     * Cập nhật metadata của disease.
     *
     * Chỉ cập nhật:
     * - name
     * - slug
     * - category
     *
     * Không cập nhật content disease trực tiếp.
     * Content phải được chỉnh sửa thông qua
     * DiseaseVersion workflow.
     *
     * Permission Rules:
     * - Owner được phép update
     * - REVIEWER/ADMIN bypass ownership check
     *
     * Validation Rules:
     * - Name phải unique
     * - Slug phải unique
     *
     * @param diseaseId id disease cần update
     * @param request dữ liệu update
     * @return disease dto sau khi cập nhật
     *
     * @throws ResourceNotFoundException nếu disease không tồn tại
     * @throws IllegalStateException nếu không có quyền update
     */
    @Override
    @Transactional
    public DiseaseDTO updateDiseaseMetadata(Long diseaseId, UpdateDiseaseRequest request) {
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

        return diseaseMapper.toDiseaseDTO(diseaseRepository.save(disease));
    }

    /**
     * Clone approved version hiện tại thành draft mới.
     *
     * Workflow:
     * Approved Version
     *      ↓
     * Clone Current Version
     *      ↓
     * Create Draft
     *      ↓
     * Contributor chỉnh sửa draft
     *
     * Mục đích:
     * - Đảm bảo approved content immutable
     * - Lưu toàn bộ version history
     * - Hỗ trợ moderation workflow
     *
     * Business Rules:
     * - Không được edit approved version trực tiếp
     * - Mọi thay đổi phải tạo version mới
     *
     * @param diseaseId id disease cần clone version
     * @return draft version mới được tạo
     */
    @Override
    public DiseaseVersionDTO cloneCurrentVersion(Long diseaseId) {
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
     * Kiểm tra user hiện tại có quyền quản lý disease hay không.
     *
     * Permission Rules:
     * - REVIEWER và ADMIN được bypass ownership check
     * - Contributor chỉ được quản lý disease của chính mình
     *
     * Ownership được xác định bằng cách:
     * - User đã từng tạo DiseaseVersion của disease đó
     *
     * @param diseaseId id disease cần kiểm tra quyền
     *
     * @throws ResourceNotFoundException nếu disease không tồn tại
     * @throws IllegalStateException nếu user không phải owner
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
     * Response bao gồm:
     * - Disease metadata
     * - Current version
     * - Disease sections
     *
     * Được sử dụng cho:
     * - Public disease detail
     * - Reviewer preview
     * - Contributor preview
     *
     * @param disease disease entity
     * @return disease detail dto hoàn chỉnh
     */
    private DiseaseDetailDTO buildDiseaseDetail(Disease disease) {
        DiseaseVersion currentVersion = disease.getCurrentVersion();
        DiseaseVersionDTO currentVersionDto = null;

        if (currentVersion != null) {
            currentVersionDto = diseaseMapper.toDiseaseVersionDTO(currentVersion);
        }

        return DiseaseDetailDTO.builder()
                .disease(diseaseMapper.toDiseaseDTO(disease))
                .currentVersion(currentVersionDto)
                .sections(currentVersion == null ? List.of() :
                        diseaseSectionRepository.findAllByVersionIdWithType(currentVersion.getId())
                                .stream()
                                .map(diseaseMapper::toDiseaseSectionDTO)
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
