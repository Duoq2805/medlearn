# Coding Rules

Architecture:
- controllers remain thin
- services contain business logic
- repositories handle persistence only
- use layered architecture consistently

DTO rules:
- never expose entities directly
- use DTOs for all API communication
- use Jakarta Validation for request DTOs
- use builder pattern for response DTOs

API rules:
- use ApiResponse wrapper
- use ResponseEntity<ApiResponse<T>>
- use proper HTTP status codes
- keep JSON structures predictable

Security rules:
- preserve JWT flow
- use role-based authorization
- never hardcode secrets
- validate permissions properly

Database rules:
- prefer soft delete
- use optimistic locking
- avoid N+1 queries
- paginate large datasets

Code quality rules:
- prefer reusable patterns
- avoid duplicated logic
- avoid unnecessary abstractions
- prefer focused modifications
- preserve maintainability

AI workflow rules:
- reuse existing documentation first
- avoid scanning entire repository
- update summaries after major implementations
- compress reusable engineering reasoning