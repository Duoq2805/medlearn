# AI Module Structure

## Package: `com.duoq.medlearn.ai`

Self-contained AI subsystem. All AI-related classes live under this package.

## Dependency Rules

- AI module MAY depend on: `Disease`, `DiseaseVersion`, `DiseaseSection`, `Symptom`, `CaseStudy`, `User`
- Disease module MUST NEVER depend on AI
- Other modules may depend on AI as needed

## Directory Tree

```
com.duoq.medlearn.ai
├── config/                    # Configuration
│   ├── AiConfig.java          # Bean wiring
│   └── AiProperties.java      # External properties
│
├── controller/                # REST endpoints
│   ├── AiAdminController.java    # Admin: prompt CRUD, testing, model discovery
│   ├── AiStreamController.java   # SSE streaming (moved from main controller/)
│   ├── AiSummaryController.java  # AI summary generation/retrieval
│   └── SymptomCheckerController.java  # Symptom matching endpoints
│
├── dto/
│   ├── request/               # Request DTOs
│   │   ├── AiChatRequest.java
│   │   ├── SummaryRequest.java
│   │   └── SymptomCheckerRequest.java
│   ├── response/              # Response DTOs
│   │   ├── AiChatResponse.java
│   │   ├── AiMessage.java
│   │   ├── AiStreamChunk.java
│   │   ├── AiStreamChunkResponse.java
│   │   ├── AiUsage.java
│   │   ├── DiseaseMatchResultResponse.java
│   │   ├── DiseaseMatchResultV2Response.java
│   │   ├── ModelInfo.java
│   │   ├── PromptTemplateResponse.java
│   │   ├── PromptTestResultResponse.java
│   │   ├── SummaryResponse.java
│   │   ├── SymptomMatchResult.java
│   │   └── VersionComparisonResponse.java
│   └── enums/                 # Shared enums
│       ├── AiRole.java
│       └── SummaryType.java
│
├── exception/                 # AI-specific exceptions
│   ├── AiConfigurationException.java
│   ├── AiContextExceededException.java
│   ├── AiInjectionDetectedException.java
│   ├── AiOutputFilteredException.java
│   ├── AiProviderException.java
│   ├── AiProviderUnavailableException.java
│   ├── AiQuotaExceededException.java
│   └── AiRateLimitException.java
│
├── gateway/                   # LLM provider abstraction
│   ├── AiGateway.java             # Interface
│   ├── AiGatewayRouter.java       # Router (multi-provider)
│   ├── AiCacheService.java        # Response cache interface
│   ├── AiCacheServiceImpl.java    # Response cache impl
│   └── NineRouterGateway.java     # NineRouter provider
│
├── mapper/                    # (reserved)
│
├── prompt/                    # Prompt engineering
│   ├── PromptBuilder.java
│   ├── PromptRenderer.java
│   ├── PromptTemplateService.java
│   ├── PromptTemplateServiceImpl.java
│   ├── PromptTester.java
│   ├── PromptValidator.java
│   ├── VariablesExtractor.java
│   └── entity/
│       ├── PromptTemplate.java    # @Entity
│       └── PromptTestResult.java  # @Entity
│
├── repository/                # Spring Data repositories
│   ├── AiSummaryRepository.java
│   ├── AiUsageLogRepository.java
│   ├── PromptTemplateRepository.java
│   └── PromptTestResultRepository.java
│
├── security/                  # AI-specific security
│   ├── AiOutputValidator.java
│   ├── AiQuotaService.java
│   ├── AiRateLimiter.java
│   └── PromptInjectionFilter.java
│
├── service/                   # AI service interfaces
│   ├── SymptomCheckerService.java
│   ├── SymptomCheckerServiceV1.java
│   ├── SymptomCheckerServiceV2.java
│   └── impl/
│       ├── SymptomCheckerServiceImpl.java
│       ├── SymptomCheckerServiceImplV1.java
│       └── SymptomCheckerServiceImplV2.java
│
├── summary/                   # AI summary feature
│   ├── AiSummaryService.java
│   ├── AiSummaryServiceImpl.java
│   └── entity/
│       └── AiSummary.java        # @Entity
│
└── usage/                     # Token/usage tracking
    ├── AiUsageService.java
    ├── AiUsageServiceImpl.java
    └── entity/
        └── AiUsageLog.java       # @Entity
```

## Moved Files

| Original Location | New Location |
|---|---|
| `controller.AiStreamController` | `ai.controller.AiStreamController` |
| `controller.SymptomCheckerController` | `ai.controller.SymptomCheckerController` |
| `service.SymptomCheckerService` | `ai.service.SymptomCheckerService` |
| `service.SymptomCheckerServiceV1` | `ai.service.SymptomCheckerServiceV1` |
| `service.SymptomCheckerServiceV2` | `ai.service.SymptomCheckerServiceV2` |
| `service.impl.SymptomCheckerServiceImpl` | `ai.service.impl.SymptomCheckerServiceImpl` |
| `service.impl.SymptomCheckerServiceImplV1` | `ai.service.impl.SymptomCheckerServiceImplV1` |
| `service.impl.SymptomCheckerServiceImplV2` | `ai.service.impl.SymptomCheckerServiceImplV2` |
| `domain.dto.ai.AiStreamChunkResponse` | `ai.dto.response.AiStreamChunkResponse` |
| `domain.dto.ai.DiseaseMatchResultDTO` | `ai.dto.response.DiseaseMatchResultResponse` |
| `domain.dto.ai.DiseaseMatchResultV2DTO` | `ai.dto.response.DiseaseMatchResultV2Response` |
| `domain.dto.ai.SymptomMatchResult` | `ai.dto.response.SymptomMatchResult` |
| `domain.dto.ai.SymptomCheckerRequest` | `ai.dto.request.SymptomCheckerRequest` |

## DTO Naming Convention

| Pattern | Example |
|---|---|
| `*Response` | `DiseaseResponse`, `SymptomResponse`, `UserResponse` |
| `*Request` | `CreateDiseaseRequest`, `LoginRequest` |
| `*Projection` | `DiseaseSummaryProjection`, `CaseStudySummaryProjection` |

All `*DTO` suffixes project-wide have been replaced according to this convention.
