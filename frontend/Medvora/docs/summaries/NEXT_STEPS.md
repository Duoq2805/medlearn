# Next Steps

## Highest Priority

1.  **Fix `DiseaseWorkflowIntegrationTest` (`UnexpectedRollbackException`):**
    *   **Root cause discovered**: The test class is annotated with `@Transactional`, which creates an outer transaction context. `AuditServiceImpl.log()` uses `@Transactional(propagation = Propagation.REQUIRES_NEW)`. When an exception occurs in the nested audit transaction, it is caught and logged, but the outer transaction is marked as rollback-only, leading to the `UnexpectedRollbackException`.
    *   **Fix approach options**:
        *   Re-examine `AuditServiceImpl.saveEntry()` to prevent the outer transaction from being marked as rollback-only.
        *   Alter the test to accept the exception or adjust transaction handling.
    *   **Goal**: `mvn clean test` passes.

2.  **Trace and Verify AuditService Transaction Behavior:**
    *   Analyze the `saveEntry()` method in `AuditServiceImpl` and its interaction with nested transactions and exception handling.
    *   Ensure the fix does not introduce other test failures.

3.  **Verify All Permission Codes:**
    *   Cross-check `PermissionCode` enum values against all check annotations used in controllers and services.

4.  **Review Remaining Service Coverage:**
    *   List services without corresponding tests.
    *   Identify high-risk areas (moderation, AI, security) for test prioritization.

5.  **Continue Backend Completion:**
    *   Implement AI features (medical concept explanations, study guides).
    *   Enhance diagram generation and visual documentation.
    *   Conduct code refactoring and optimization.
