# Startup Blockers Audit Report

**Generated**: 2026-06-08  
**Status**: Application CANNOT start without resolving these blockers

---

## Executive Summary

**Current State**: Application startup **BLOCKED** due to missing environment variables.

**Blocker Count**: 6 required environment variables with no default values.

**Impact**:
- `mvn clean test`: **FAILS** (ApplicationContext cannot load)
- `spring-boot:run`: **FAILS** (JwtService bean creation fails)

---

## Critical Blockers (6 Required Environment Variables)

### 1. JWT_SECRET
**File**: `src/main/resources/application.yml` (Line 66)  
**Configuration**:
```yaml
jwt:
  secret: ${JWT_SECRET}
```
**Evidence**: No default value provided. Startup fails with:
```
Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```
**Used By**: `JwtService.java` (Line 22)
```java
@Value("${jwt.secret}")
private String secret;
```

### 2. SPRING_DATASOURCE_PASSWORD
**File**: `src/main/resources/application.yml` (Line 11)  
**Configuration**:
```yaml
spring:
  datasource:
    password: ${SPRING_DATASOURCE_PASSWORD}
```
**Evidence**: No default value. Required for database connection.

### 3. SPRING_MAIL_USERNAME
**File**: `src/main/resources/application.yml` (Line 34)  
**Configuration**:
```yaml
spring:
  mail:
    username: ${SPRING_MAIL_USERNAME}
```
**Evidence**: No default value. Required for email functionality (registration, password reset).

### 4. SPRING_MAIL_PASSWORD
**File**: `src/main/resources/application.yml` (Line 35)  
**Configuration**:
```yaml
spring:
  mail:
    password: ${SPRING_MAIL_PASSWORD}
```
**Evidence**: No default value. Required for email authentication.

### 5. SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID
**File**: `src/main/resources/application.yml` (Line 49)  
**Configuration**:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID}
```
**Evidence**: No default value. Required for Google OAuth2 login.

### 6. SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET
**File**: `src/main/resources/application.yml` (Line 50)  
**Configuration**:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-secret: ${SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET}
```
**Evidence**: No default value. Required for Google OAuth2 authentication.

---

## Optional Environment Variables (7 with defaults)

These have default values and do NOT block startup:

1. `SPRING_DATASOURCE_URL` → default: `jdbc:postgresql://localhost:5432/medlearn_db`
2. `SPRING_DATASOURCE_USERNAME` → default: `medlearn`
3. `SPRING_MAIL_HOST` → default: `smtp.gmail.com`
4. `SPRING_MAIL_PORT` → default: `587`
5. `JWT_EXPIRATION` → default: `86400000`
6. `JWT_REFRESH_EXPIRATION` → default: `604800000`
7. `JWT_PASSWORD_RESET_EXPIRY_HOURS` → default: `1`

---

## Test Configuration Blocker

**File**: `src/test/resources/application-test.yml`  
**Status**: **DOES NOT EXIST**

**Evidence**:
```bash
$ cat src/test/resources/*.yml
No test configs found
```

**Impact**: Tests attempt to load `application.yml`, which references all 6 required environment variables. Without `application-test.yml`, tests cannot run unless all environment variables are provided.

**Test Failure Evidence** (from `mvn clean test`):
```
java.lang.IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```

---

## Minimal Configuration Requirements

### For `mvn clean test` to succeed:

**Option A**: Create `src/test/resources/application-test.yml` with test-specific values:
```yaml
jwt:
  secret: test-secret-key-for-testing-only-min-256-bits
  
spring:
  datasource:
    password: test
  mail:
    username: test@example.com
    password: test
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: test-client-id
            client-secret: test-client-secret
```

**Option B**: Set all 6 required environment variables before running tests.

### For `spring-boot:run` to succeed:

**Requirement**: All 6 required environment variables must be set.

**Current Method**: `.env` file exists at `backend/.env` (gitignored) with values, but Spring Boot does NOT load `.env` files by default.

**Actual Requirement**: Environment variables must be:
- Exported in shell session, OR
- Passed via `-D` flags, OR
- Loaded by external tool (e.g., `dotenv`, IDE configuration)

---

## Startup Sequence Evidence

**From** `mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"`:

1. Spring Boot starts loading ApplicationContext
2. Attempts to create `JwtService` bean
3. `@Value("${jwt.secret}")` tries to resolve `${JWT_SECRET}`
4. Environment variable not found
5. **CRASH**: `IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET'`
6. ApplicationContext fails to load
7. Application exits with failure

**Time to failure**: ~2 seconds (before any controllers or security config initialized)

---

## Configuration Files Present

```
src/main/resources/application.yml         (1913 bytes, modified 2026-06-07 23:46)
src/main/resources/application-dev.yml     (369 bytes, modified 2026-06-08 00:09)
src/main/resources/application-docker.yml  (785 bytes, modified 2026-06-07 23:46)
src/main/resources/application-prod.yml    (308 bytes, modified 2026-06-08 00:09)
src/test/resources/                        (MISSING: application-test.yml)
```

---

## Resolution Requirements Summary

| Blocker | Resolution Method | Priority |
|---------|-------------------|----------|
| JWT_SECRET | Set env var or create application-test.yml | **CRITICAL** |
| SPRING_DATASOURCE_PASSWORD | Set env var or create application-test.yml | **CRITICAL** |
| SPRING_MAIL_USERNAME | Set env var or create application-test.yml | **CRITICAL** |
| SPRING_MAIL_PASSWORD | Set env var or create application-test.yml | **CRITICAL** |
| GOOGLE_CLIENT_ID | Set env var or create application-test.yml | **CRITICAL** |
| GOOGLE_CLIENT_SECRET | Set env var or create application-test.yml | **CRITICAL** |
| application-test.yml | Create file with test configuration | **HIGH** |

**Note**: All 6 environment variables must be resolved simultaneously for successful startup.

---

**End of Report**
