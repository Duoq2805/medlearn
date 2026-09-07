$path = 'src/main/java/com/duoq/medlearn/knowledge/disease/service/impl/DiseaseServiceImpl.java'
$content = Get-Content -Path $path -Raw

# Replace package
$content = $content -replace '^package com\.duoq\.medlearn\.service\.impl;', 'package com.duoq.medlearn.knowledge.disease.service.impl;'

# Replace imports
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.entity\.Category;', 'import com.duoq.medlearn.knowledge.disease.entity.Category;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.entity\.Disease;', 'import com.duoq.medlearn.knowledge.disease.entity.Disease;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.entity\.DiseaseVersion;', 'import com.duoq.medlearn.knowledge.disease.entity.DiseaseVersion;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.entity\.User;', 'import com.duoq.medlearn.knowledge.disease.entity.User;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.enums\.AuditAction;', 'import com.duoq.medlearn.knowledge.disease.enums.AuditAction;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.enums\.VersionStatus;', 'import com.duoq.medlearn.knowledge.disease.enums.VersionStatus;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.DiseaseSummaryProjection;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseSummaryProjection;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.CreateDiseaseDraftRequest;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.CreateDiseaseDraftRequest;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.CreateDiseaseRequest;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.CreateDiseaseRequest;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.DiseaseSearchRequest;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseSearchRequest;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.UpdateDiseaseRequest;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.UpdateDiseaseRequest;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.DiseaseResponse;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseResponse;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.disease\.DiseaseDetailResponse;', 'import com.duoq.medlearn.knowledge.disease.dto.disease.DiseaseDetailResponse;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.dto\.version\.DiseaseVersionResponse;', 'import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;'
$content = $content -replace 'import com\.duoq\.medlearn\.exception\.ResourceNotFoundException;', 'import com.duoq.medlearn.exception.ResourceNotFoundException;'
$content = $content -replace 'import com\.duoq\.medlearn\.mapper\.DiseaseMapper;', 'import com.duoq.medlearn.knowledge.disease.mapper.DiseaseMapper;'
$content = $content -replace 'import com\.duoq\.medlearn\.repository\.CategoryRepository;', 'import com.duoq.medlearn.knowledge.disease.repository.CategoryRepository;'
$content = $content -replace 'import com\.duoq\.medlearn\.repository\.DiseaseRepository;', 'import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;'
$content = $content -replace 'import com\.duoq\.medlearn\.repository\.DiseaseSectionRepository;', 'import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;'
$content = $content -replace 'import com\.duoq\.medlearn\.repository\.DiseaseVersionRepository;', 'import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;'
$content = $content -replace 'import com\.duoq\.medlearn\.repository\.UserRepository;', 'import com.duoq.medlearn.auth.repository.UserRepository;'
$content = $content -replace 'import com\.duoq\.medlearn\.security\.CurrentUserResolver;', 'import com.duoq.medlearn.common.security.CurrentUserResolver;'
$content = $content -replace 'import com\.duoq\.medlearn\.domain\.enums\.PermissionCode;', 'import com.duoq.medlearn.auth.enums.PermissionCode;'
$content = $content -replace 'import com\.duoq\.medlearn\.service\.AuditService;', 'import com.duoq.medlearn.audit.service.AuditService;'
$content = $content -replace 'import com\.duoq\.medlearn\.service\.PermissionService;', 'import com.duoq.medlearn.auth.service.PermissionService;'
$content = $content -replace 'import com\.duoq\.medlearn\.service\.DiseaseService;', 'import com.duoq.medlearn.knowledge.disease.service.DiseaseService;'
$content = $content -replace 'import com\.duoq\.medlearn\.service\.DiseaseVersionService;', 'import com.duoq.medlearn.knowledge.version.service.DiseaseVersionService;'

# Write back
[System.IO.File]::WriteAllText($path, $content, [System.Text.Encoding]::UTF8)
