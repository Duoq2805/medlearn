$path = 'src/main/java/com/duoq/medlearn/knowledge/disease/service/impl/DiseaseServiceImpl.java'
$content = Get-Content -Path $path
$packageIndex = ($content | Select-String '^package').LineNumber - 1
$annotationIndex = ($content | Select-String '^@RequiredArgsConstructor').LineNumber - 1
$newImports = @(
    'import com.duoq.medlearn.knowledge.disease.entity.Category;',
    'import com.duoq.medlearn.knowledge.disease.entity.Disease;',
    'import com.duoq.medlearn.knowledge.disease.entity.DiseaseVersion;',
    'import com.duoq.medlearn.knowledge.disease.entity.User;',
    'import com.duoq.medlearn.knowledge.disease.enums.AuditAction;',
    'import com.duoq.medlearn.knowledge.disease.enums.VersionStatus;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseSummaryProjection;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.CreateDiseaseDraftRequest;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.CreateDiseaseRequest;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseSearchRequest;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.UpdateDiseaseRequest;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseResponse;',
    'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseDetailResponse;',
    'import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;',
    'import com.duoq.medlearn.exception.ResourceNotFoundException;',
    'import com.duoq.medlearn.knowledge.disease.mapper.DiseaseMapper;',
    'import com.duoq.medlearn.knowledge.disease.repository.CategoryRepository;',
    'import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;',
    'import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;',
    'import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;',
    'import com.duoq.medlearn.auth.repository.UserRepository;',
    'import com.duoq.medlearn.common.security.CurrentUserResolver;',
    'import com.duoq.medlearn.auth.enums.PermissionCode;',
    'import com.duoq.medlearn.audit.service.AuditService;',
    'import com.duoq.medlearn.auth.service.PermissionService;',
    'import com.duoq.medlearn.knowledge.disease.service.DiseaseService;',
    'import com.duoq.medlearn.knowledge.version.service.DiseaseVersionService;',
    'import lombok.RequiredArgsConstructor;',
    'import org.springframework.data.domain.Page;',
    'import org.springframework.data.domain.Pageable;',
    'import org.springframework.stereotype.Service;',
    'import org.springframework.transaction.annotation.Transactional;'
)
$newContent = $content[0..$packageIndex]
$newContent += ''  # empty line after package
$newContent += $newImports
$newContent += ''  # empty line after imports
$newContent += $content[$annotationIndex..($content.Length - 1)]
Set-Content -Path $path -Value $newContent -Encoding UTF8
