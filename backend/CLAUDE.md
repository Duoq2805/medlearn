# Medvora Backend - Claude Instructions

## Project Overview

Medvora is an AI-powered medical learning platform for university students.
The system focuses on disease learning, medical document management, AI-assisted study, and reviewer moderation workflow.

Main goals:
- Clean architecture
- Maintainable codebase
- Scalable backend
- Secure API design
- Reviewer-based moderation flow

---

# Tech Stack

## Backend
- Java 21+
- Spring Boot
- Spring Security
- Spring Data JPA
- Maven
- PostgreSQL
- JWT Authentication
- Lombok

## Development Style
- RESTful API
- DTO pattern
- Layered architecture
- Service-oriented business logic

---

# Roles

There are only 3 roles in the system:

## User
- Learn medical content
- Read diseases and documents
- Use AI learning features
- Save/bookmark content

## Reviewer
- Review submitted/edited medical content
- Approve or reject disease versions
- Moderate AI-generated or uploaded learning resources

## Admin
- Full system management
- Manage users and reviewers
- Manage moderation system
- View analytics and reports

---

# Architecture Rules

The project MUST follow layered architecture:

```txt
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

## Required Layers
- Controller layer
- Service layer
- Repository layer
- DTO layer
- Entity layer

## Rules
- Business logic MUST stay inside services
- Controllers should remain thin
- Never place business logic inside controllers
- Never access repositories directly from controllers
- Use constructor injection only
- Use DTOs for API communication
- Avoid returning entities directly

---

# Coding Standards

## Naming

### Classes
- PascalCase
- Example:
    - UserController
    - DiseaseService
    - ReviewRepository

### Variables
- camelCase

### Constants
- UPPER_SNAKE_CASE

---

# API Standards

## Response Rules
- Use ResponseEntity
- Use proper HTTP status codes
- Return meaningful error messages

## Validation
- Use Jakarta Validation
- Validate all request DTOs

## Security
- JWT authentication required
- Role-based authorization
- Never expose sensitive data
- Never hardcode secrets

---

# Database Rules

## SQL Server
- Use proper relationships
- Use lazy loading when appropriate
- Avoid N+1 query problems

## Entity Rules
- Use createdAt and updatedAt
- Use soft delete when necessary
- Avoid bidirectional relationships unless needed

---

# Disease Management Rules

Disease is the core entity of the system.

## Core Concepts
- Disease
- DiseaseVersion
- Moderation workflow
- Reviewer approval system

## Workflow
1. Disease content created or edited
2. New version generated
3. Reviewer reviews content
4. Reviewer approves or rejects
5. Approved version becomes live

---

# AI Feature Rules

AI features should:
- Assist learning
- Explain medical concepts
- Generate study support
- Never provide unsafe medical advice
- Never pretend to replace doctors

---

# Frontend Communication Rules

Backend APIs should:
- Be frontend-friendly
- Return predictable JSON structures
- Use pagination where needed
- Avoid excessive nested responses

---

# Important Development Rules

## NEVER
- Delete database tables without confirmation
- Rename core entities without confirmation
- Modify authentication flow without confirmation
- Break existing APIs without confirmation
- Generate unnecessary abstractions
- Overengineer the project

## ALWAYS
- Prefer clean and maintainable code
- Reuse services when possible
- Keep code readable
- Add comments only when necessary
- Follow existing architecture patterns
- Review existing code before generating new code

---

# Performance Rules

- Avoid loading unnecessary relationships
- Use pagination for large datasets
- Optimize heavy queries
- Avoid duplicated logic

---

# Git & Refactoring Rules

Before major refactors:
- Analyze impact first
- Preserve API compatibility when possible
- Avoid unnecessary file renaming

---

# Project Goal

This project is intended to become:
- A professional academic medical platform
- A scalable long-term system
- A portfolio-quality backend architecture project

Code quality and maintainability are more important than fast hacks.