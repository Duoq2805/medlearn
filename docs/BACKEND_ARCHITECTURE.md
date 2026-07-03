# Backend Architecture

## Layer Diagram

```text
controller -> service interface -> service.impl -> repository -> database
              service.impl -> mapper -> dto
security/config/exception wrap application boundary
```

## Dependency Flow

- Controllers depend on service interfaces only.
- Service implementations own business rules and transactions.
- Repositories own persistence queries only.
- Mappers own Entity to DTO conversion.
- DTOs define API request/response contracts.

## Controller Responsibilities

- Bind path, query, headers, and request bodies.
- Run bean validation with `@Valid`.
- Delegate to services.
- Wrap service results in `ApiResponse` and `ResponseEntity`.

Controllers must not inject repositories, build entities, perform permission decisions beyond annotations, or map DTOs manually.

## Service Responsibilities

- Own business logic and workflow rules.
- Load and persist entities through repositories.
- Call mappers for DTO conversion.
- Define transaction boundaries.
- Throw typed application exceptions where possible.

## Repository Responsibilities

- Extend Spring Data repositories.
- Define persistence queries only.
- Avoid business decisions and manual DTO mapping.

## MapStruct Conventions

- Use `@Mapper(config = MapStructConfig.class)`.
- Reuse existing mappers before adding new ones.
- Entity to DTO mapping belongs in mapper classes.
- Custom nested ID extraction may use MapStruct expressions or default mapper methods.

## Transaction Conventions

- Write service methods use `@Transactional`.
- Read service methods use `@Transactional(readOnly = true)`.
- Controllers do not declare transactions.

## Security Conventions

- Controllers may use `@PreAuthorize` for endpoint permission gates.
- Services may enforce business ownership/workflow permissions when endpoint annotation is insufficient.
- Avoid duplicating permission policy across layers unless needed for non-controller callers.

## Validation Conventions

- Request DTOs use Jakarta validation annotations.
- Controllers apply `@Valid`.
- Services validate cross-entity/business constraints.

## Package Conventions

- `controller`: HTTP API layer.
- `service`: service interfaces.
- `service.impl`: service implementations.
- `repository`: persistence access.
- `mapper`: MapStruct mappers.
- `domain.entity`: JPA entities.
- `domain.dto`: API DTOs.
- `config`: Spring configuration.
- `security`: authentication/authorization support.
- `exception`: typed exceptions and handlers.

## Coding Conventions

- Constructor injection with `@RequiredArgsConstructor`.
- No field injection.
- Prefer interfaces for controller to service dependencies.
- Preserve endpoint URLs, request JSON, response JSON, schema, and business behavior during refactors.
- Keep diffs small and avoid speculative abstractions.
