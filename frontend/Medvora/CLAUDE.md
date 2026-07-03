# Medvora Backend - Claude Instructions

# Core Philosophy

Medvora is a long-term AI-assisted software engineering project.

This repository is designed to:

* scale cleanly over time
* support AI-assisted development workflows
* minimize unnecessary token usage
* preserve architecture consistency
* externalize reusable project knowledge
* optimize maintainability over fast hacks

The system should behave like a professional production-grade backend architecture project.

Code quality, consistency, and reusable project intelligence are more important than rapid code generation.

---

# AI Engineering Philosophy

The project follows an AI-assisted engineering workflow.

The AI should:

* avoid re-analyzing unchanged systems
* reuse existing project knowledge
* prefer focused scoped reasoning
* minimize unnecessary context usage
* preserve architecture consistency
* generate reusable documentation
* behave like a long-term engineering collaborator

The repository itself should become a reusable project memory system.

---

# Knowledge System

The project uses a layered knowledge system.

```txt
/docs           -> reusable AI-readable markdown knowledge
/docs-html      -> human-friendly visual documentation
/diagrams       -> Mermaid + PlantUML architecture diagrams
```

---

# Knowledge System Rules

For every IMPORTANT implementation:

1. Analyze existing architecture first
2. Reuse existing patterns whenever possible
3. Avoid re-analyzing unchanged systems repeatedly
4. Update reusable markdown documentation
5. Update architecture diagrams when needed
6. Generate visual HTML summaries for large workflows when appropriate
7. Keep documentation concise, reusable, and future-proof

The project should gradually build a persistent engineering memory layer.

---

# Documentation Rules

For important implementations:

* create or update markdown documentation inside `/docs`
* reuse existing documentation before generating new architecture
* treat `/docs` as long-term project memory
* avoid duplicate architectural explanations
* prefer incremental updates over rewriting entire docs

---

# Folder Responsibilities

## `/docs`

Reusable AI-readable markdown knowledge.

### Structure

```txt
/docs
├── architecture/
├── workflows/
├── api/
├── security/
├── database/
├── ai/
├── rules/
├── maps/
└── summaries/
```

### Purpose

| Folder       | Purpose                              |
| ------------ | ------------------------------------ |
| architecture | system architecture and design       |
| workflows    | business workflow explanations       |
| api          | API standards and wrappers           |
| security     | authentication and authorization     |
| database     | entity relations and DB rules        |
| ai           | AI integration logic                 |
| rules        | coding rules and conventions         |
| maps         | component responsibility maps        |
| summaries    | compressed project/session summaries |

---

## `/docs-html`

Human-friendly visual documentation.

Used for:

* visual architecture pages
* workflow visualization
* embedded diagrams
* API tables
* architecture navigation
* onboarding summaries

HTML documentation should complement markdown documentation, not replace it.

Markdown remains the primary AI-readable source of truth.

---

## `/diagrams`

Architecture and flow diagrams.

### Supported Formats

* Mermaid
* PlantUML

### Example Diagrams

* authentication flow
* reviewer moderation workflow
* AI service sequence
* ERD
* package dependencies
* service interaction diagrams

Diagrams should help reduce unnecessary repository scanning.

---

# AI Context Optimization Rules

The AI MUST optimize token usage whenever possible.

---

## NEVER

```txt
Analyze the entire project
```

unless explicitly required.

---

## ALWAYS Prefer

```txt
Analyze only the authentication module
Analyze only the changed files
Analyze only the reviewer workflow
```

---

# Context Scoping Rules

Prefer:

* package-level analysis
* feature-level analysis
* module-level reasoning
* changed-file analysis

Avoid:

* full repository scans
* unnecessary context loading
* repeated architecture inference

---

# Architecture Summary Strategy

Before scanning large parts of the repository, the AI should:

1. Read architecture summaries
2. Read workflow summaries
3. Read coding rules
4. Read diagrams if available
5. Scan only relevant modules afterward

---

# Session Compression Rules

After major implementations:

* generate reusable summaries
* compress important architectural decisions
* update reusable documentation
* preserve important reasoning for future sessions

The goal is to reduce repeated reasoning across sessions.

---

# Semantic Project Structure

The repository should maintain strong semantic organization.

Features should remain isolated and understandable.

Prefer:

* modular structure
* clear package naming
* small focused services
* clear responsibilities
* feature-oriented organization

Avoid:

* massive god classes
* huge services
* unclear package responsibilities
* duplicated logic

---

# Patch Editing Rules

Prefer patch-style modifications.

DO:

```txt
Modify only the validation logic
Update only the reviewer approval flow
Refactor only the token generation method
```

AVOID:

```txt
Rewrite the entire file
```

unless necessary.

---

# Task Chaining Rules

Complex features should be implemented incrementally.

Preferred flow:

1. Architecture design
2. Entity design
3. DTO design
4. Repository implementation
5. Service implementation
6. Controller implementation
7. Validation/security
8. Documentation update
9. Diagram update

Avoid implementing massive features in a single generation step.

---

# Reusable Prompting Philosophy

The AI should reuse:

* project standards
* existing architecture
* reusable workflows
* existing DTO patterns
* existing security patterns
* documentation memory

Avoid inventing new patterns unnecessarily.

---

# Project Overview

Medvora is an AI-powered medical learning platform for university students.

The system focuses on:

* disease learning
* medical document management
* reviewer moderation workflow
* AI-assisted study support
* scalable backend architecture

Main goals:

* clean architecture
* maintainable codebase
* scalable backend
* reusable AI-assisted workflows
* secure API design
* professional portfolio-quality engineering

---

# Tech Stack

## Backend

* Java 21+
* Spring Boot
* Spring Security
* Spring Data JPA
* Maven
* PostgreSQL
* JWT Authentication
* Lombok
* MapStruct

---

# Development Style

The project follows:

* RESTful API design
* DTO pattern
* layered architecture
* service-oriented business logic
* reusable architecture standards
* AI-assisted maintainable development

---

# Roles

There are only 3 roles in the system.

## User

* Learn medical content
* Read diseases and medical documents
* Use AI learning support
* Save/bookmark content

## Reviewer

* Review disease content
* Approve/reject disease versions
* Moderate uploaded learning resources
* Moderate AI-assisted learning content

## Admin

* Full system management
* Manage users/reviewers
* Manage moderation system
* View analytics and reports

---

# Architecture Rules

The project MUST follow layered architecture.

```txt
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

---

# Required Layers

* Controller layer
* Service layer
* Repository layer
* DTO layer
* Entity layer
* Mapper layer

---

# Architecture Rules

## MUST

* Keep controllers thin
* Place business logic inside services
* Use constructor injection only
* Use DTOs for API communication
* Use MapStruct for mapping
* Use reusable patterns
* Preserve architecture consistency

## MUST NOT

* Put business logic inside controllers
* Access repositories directly from controllers
* Return entities directly from APIs
* Create unnecessary abstractions
* Overengineer the project

---

# Package Structure

Base package:

```txt
com.duoq.medlearn
```

```txt
com.duoq.medlearn/
├── config/
├── controller/
├── domain/
│   ├── entity/
│   └── enums/
├── dto/
│   ├── request/
│   └── response/
├── exception/
├── mapper/
├── repository/
├── security/
├── service/
│   └── impl/
├── util/
└── ai/
```

---

# Coding Standards

## Naming Rules

### Classes

* PascalCase

### Variables

* camelCase

### Constants

* UPPER_SNAKE_CASE

### URL Paths

* kebab-case

Examples:

```txt
/api/auth/login
/api/admin/users
/api/disease-versions
```

---

# Entity Rules

## Requirements

* `GenerationType.IDENTITY`
* `OffsetDateTime`
* `@CreationTimestamp`
* `@UpdateTimestamp`
* soft delete (`deletedAt`)
* optimistic locking (`@Version`)
* `@Enumerated(EnumType.STRING)`
* `@Builder.Default` for defaults

---

# Controller Rules

Controllers MUST:

* remain thin
* validate requests
* return `ResponseEntity<ApiResponse<T>>`
* delegate logic to services
* use constructor injection

---

# Service Rules

Services MUST:

* contain business logic
* use interface + implementation pattern
* use `@Transactional` on write operations
* throw custom exceptions
* reuse existing services when possible

---

# DTO Rules

## Request DTOs

* use Jakarta Validation
* use descriptive validation messages

## Response DTOs

* use builder pattern
* avoid exposing entities directly

---

# Mapper Rules

Use MapStruct.

Requirements:

* one mapper per domain entity
* shared `MapStructConfig`
* reusable mapping logic

---

# Repository Rules

Repositories MUST:

* extend `JpaRepository`
* avoid unnecessary queries
* avoid N+1 problems
* use pagination where appropriate
* use lazy loading carefully

---

# Exception Rules

All custom exceptions:

* extend `RuntimeException`
* handled globally
* return standardized `ApiResponse`

Validation errors should be descriptive and readable.

---

# API Standards

All API responses MUST use:

```java
ApiResponse<T>
```

---

# Response Rules

* Always use `ResponseEntity<ApiResponse<T>>`
* Use proper HTTP status codes
* Return meaningful error messages
* Use predictable JSON structures
* Use pagination where appropriate

---

# Security Rules

The project uses:

* JWT authentication
* stateless sessions
* role-based authorization
* Spring Security
* method-level security

---

# Security Requirements

## NEVER

* expose sensitive data
* hardcode secrets
* bypass authentication
* weaken authentication flow without confirmation

## ALWAYS

* use secure validation
* preserve JWT flow
* use config-based secrets
* follow role-based authorization

---

# Disease Management Rules

Disease is the core entity of the system.

Core concepts:

* Disease
* DiseaseVersion
* reviewer moderation workflow
* approval system

---

# Disease Workflow

```txt
Disease Created/Edited
        ↓
DiseaseVersion Generated
        ↓
Reviewer Moderation
        ↓
Approve / Reject
        ↓
Approved Version Goes Live
```

---

# AI Feature Rules

AI features should:

* assist learning
* explain medical concepts
* generate study support
* avoid unsafe medical advice
* avoid pretending to replace doctors

---

# Frontend Communication Rules

Backend APIs should:

* remain frontend-friendly
* use predictable JSON
* avoid excessive nesting
* support pagination
* provide reusable response structures

---

# Performance Rules

## ALWAYS

* avoid duplicated logic
* optimize heavy queries
* paginate large datasets
* avoid loading unnecessary relationships
* prefer focused queries

---

# Git & Refactoring Rules

Before major refactors:

* analyze impact first
* preserve compatibility when possible
* avoid unnecessary renaming
* update docs if architecture changes
* update diagrams if workflows change

---

# AI Workflow Strategy

## Preferred Development Flow

```txt
Architecture
    ↓
Implementation
    ↓
Documentation
    ↓
Diagram Update
    ↓
Session Compression
```

---

# AI Model Usage Philosophy

## Architecture Tasks

Prefer:

* Claude reasoning
* deep analysis
* consistency

## Implementation Tasks

Prefer:

* focused coding models
* reusable patterns
* patch-style editing

## Repetitive Tasks

Prefer:

* lightweight cheap worker models
* summaries
* DTO generation
* markdown generation

---

# Important Development Rules

## NEVER

* delete database tables without confirmation
* rename core entities without confirmation
* break existing APIs without confirmation
* modify authentication flow without confirmation
* overengineer the system
* generate unnecessary abstractions

---

# ALWAYS

* prioritize maintainability
* preserve architecture consistency
* reuse existing patterns
* update project knowledge
* keep code readable
* prefer focused modifications
* review existing code before generating new code

---

# Project Goal

Medvora should become:

* a professional academic medical platform
* a scalable long-term system
* a portfolio-quality backend architecture project
* a reusable AI-assisted engineering ecosystem

The repository itself should evolve into a structured engineering knowledge system.

---

# Communication Rules

Communicate in Vietnamese.

Keep explanations:

* practical
* maintainable
* engineering-focused
* architecture-aware
* token-efficient


# AI Memory & Documentation Workflow

The repository uses markdown documentation as persistent reusable AI memory.

Documentation exists to:
- reduce repeated repository analysis
- preserve architectural reasoning
- compress implementation knowledge
- improve future AI sessions
- reduce unnecessary token usage
- maintain long-term consistency

The AI should treat documentation as reusable engineering intelligence, not traditional documentation.

---

# Documentation Structure

```txt
/docs
├── architecture/
├── workflows/
├── summaries/
└── rules/

/diagrams
/docs-html
```

---

# Folder Responsibilities

## `/docs/architecture`

Contains compressed reusable architecture knowledge.

Examples:
- backend architecture
- authentication architecture
- database architecture
- API architecture
- security structure
- package responsibilities

Purpose:
- reduce repeated architecture inference
- help future sessions understand the system quickly
- preserve important engineering decisions

---

## `/docs/workflows`

Contains reusable business workflow knowledge.

Examples:
- reviewer moderation workflow
- disease versioning flow
- AI learning workflow
- approval lifecycle
- upload flow

Purpose:
- preserve business logic reasoning
- reduce repeated workflow analysis
- maintain workflow consistency

---

## `/docs/summaries`

Contains compressed reusable session memory.

This is the PRIMARY AI memory layer.

After important implementations:
- create or update summaries
- compress implementation reasoning
- preserve reusable decisions
- preserve reusable patterns
- preserve future considerations

DO NOT store trivial implementation details.

Focus on:
- architecture decisions
- reusable patterns
- business logic reasoning
- important service interactions
- future maintainability

Summaries should help future sessions avoid scanning large parts of the repository.

---

## `/docs/rules`

Contains reusable coding conventions and standards.

Examples:
- DTO rules
- naming conventions
- API response rules
- validation rules
- transaction rules

Avoid duplicating rules already defined inside CLAUDE.md.

---

## `/diagrams`

Contains Mermaid and PlantUML diagrams.

Generate or update diagrams ONLY when:
- workflows changed significantly
- architecture changed significantly
- authentication flow changed
- entity relationships changed

Avoid generating unnecessary diagrams for trivial CRUD features.

---

## `/docs-html`

Contains optional human-friendly visual summaries.

Generate HTML documentation ONLY for:
- large workflows
- authentication overview
- architecture overview
- AI system overview

Avoid excessive HTML generation.

Markdown remains the primary AI-readable memory format.

---

# Automatic Documentation Behavior

Before implementation:
1. Read relevant summaries
2. Read relevant architecture docs
3. Read related workflow docs
4. Scan only relevant modules afterward

Avoid scanning the entire repository unless absolutely necessary.

---

# Required Documentation Updates

After IMPORTANT implementations:
- update summaries
- update workflows if business logic changed
- update architecture docs if system structure changed
- update diagrams if architecture/workflow changed significantly

Documentation updates should happen automatically without requiring explicit user prompts.

---

# Important Documentation Rules

## ALWAYS
- keep documentation concise
- keep documentation reusable
- compress reasoning efficiently
- preserve future maintainability
- reuse existing docs before generating new docs
- prefer incremental updates
- prefer semantic organization
- prioritize token efficiency

---

## NEVER
- generate verbose academic documentation
- duplicate architectural explanations
- store trivial implementation details
- rewrite entire documentation unnecessarily
- generate excessive diagrams
- create unnecessary documentation files

---

# AI Context Optimization

The AI MUST optimize context usage.

Prefer:
- feature-level reasoning
- module-level reasoning
- changed-file analysis
- reusable summaries
- focused repository scanning

Avoid:
- full repository analysis
- repeated architecture inference
- unnecessary context loading

---

# Session Compression Workflow

After large implementation sessions:
1. compress implementation knowledge
2. update reusable summaries
3. preserve architectural reasoning
4. preserve reusable workflows
5. preserve future considerations

The repository itself should gradually evolve into a reusable engineering memory system.

---

# Persistent Engineering Memory Philosophy

The goal is NOT:
- storing AI outputs

The goal IS:
- preserving reusable engineering intelligence

The repository should become progressively smarter and more reusable across future AI sessions.

# Continuation & Recovery Rules

When an implementation becomes large, complex, or incomplete:

- generate a reusable continuation summary
- preserve implementation progress
- preserve important reasoning
- preserve unfinished tasks
- preserve architecture decisions
- preserve next implementation steps

If context becomes too large or generation becomes unstable:
- generate a continuation prompt
- summarize current implementation state
- summarize pending tasks
- summarize important constraints

Continuation prompts should help future sessions continue work without re-analyzing the entire repository.