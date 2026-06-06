# Backend Summary

Medvora backend follows layered architecture.

Flow:
Controller -> Service -> Repository -> Database

Controllers remain thin.
Business logic belongs inside services.
Repositories handle database access only.

Main modules:
- Authentication
- Disease Management
- Disease Versioning
- Reviewer Workflow
- AI Learning Support

Project standards:
- DTO pattern
- ApiResponse wrapper
- JWT authentication
- soft delete
- optimistic locking (@Version on entities)
- pessimistic locking (approval/rollback operations)
- MapStruct mapping
- ResponseEntity<ApiResponse<T>>

Main backend goals:
- maintainable architecture
- reusable patterns
- scalable structure
- token-efficient AI-assisted development

Important rules:
- never expose entities directly
- never place business logic in controllers
- use constructor injection only
- preserve architecture consistency
- prefer reusable services
- prefer focused modifications
- pessimistic locking for high-value operations (approval, rollback)
- optimistic locking for general entity updates
- deep-clone patterns for immutable version snapshots

Database philosophy:
- all important entities support soft delete
- enums use EnumType.STRING
- pagination required for large datasets
- avoid unnecessary eager loading

AI workflow philosophy:
- summaries are reusable AI memory
- avoid re-analyzing unchanged systems
- reuse documentation before scanning repository