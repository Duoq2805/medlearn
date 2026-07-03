# MedLearn Frontend Contract

**Single source of truth for all frontend developers and AI coding agents.**

Non-negotiable rules. Violations cause integration bugs. Read before writing code.

---

## 1. Backend Authority

| Rule | Explanation |
|------|-------------|
| Backend is source of truth | Never invent endpoints, DTOs, enums, or business logic. |
| No guessing | If an endpoint isn't documented, ask. Don't guess the path. |
| No inventing fields | Request DTOs must match backend exactly. Extra fields are ignored. |
| Implementation wins | If docs and running backend differ, the running backend wins. |
| Fix docs, not code | When unsure, update backend documentation. Never adapt frontend to guesses. |

---

## 2. Base URL

```
VITE_API_URL=http://localhost:6060/api
```

- Must use environment variable.
- Never hardcode `http://localhost:6060` in components, services, or tests.

---

## 3. Authentication Rules

### Anonymous users may call:

- All public GET endpoints
- POST /api/auth/register, /login, /forgot-password, /reset-password, /resend-verification
- GET /api/auth/verify-email/{token}
- POST /api/auth/refresh
- POST /api/symptom-checker/{check,analyze}
- POST /api/diseases/search
- POST /api/cases/{id}/diagnose

### ⚠️ FORBIDDEN for anonymous users:

```
GET /api/auth/me
```

Never call `/api/auth/me` unless an access token exists in localStorage.

### Recommended bootstrap:

```
App mount
  ↓
accessToken in localStorage?
  ├─ YES → GET /api/auth/me → set user state
  └─ NO  → stay anonymous, never call /auth/me
```

### Token refresh flow:

```
401 response
  ↓
POST /api/auth/refresh { refreshToken }
  ├─ success → retry original request with new token
  └─ failure → clear tokens, redirect to /login
```

### Logout:

```
POST /api/auth/logout (Authorization header carries token)
  ↓
Clear localStorage tokens
  ↓
Redirect to /login
```

No request body required for logout. Backend reads token from header.

---

## 4. Response Format

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-07-02T12:00:00"
}
```

- Always check `success` before reading `data`.
- Display `message` to user when useful.

---

## 5. Pagination

Use Spring Pageable query params exactly:

```
?page=0&size=20&sort=name,asc
```

- `page` is 0‑based.
- Never invent custom page/pageSize param names.
- Response wraps content in `data.content`.

---

## 6. Error Handling

| Code | Cause | Frontend action |
|------|-------|-----------------|
| 401 | No token or expired | `→` try refresh `→` if fail, logout |
| 403 | Authenticated, no permission | Show "You do not have permission." |
| 404 | Resource not found | Show not-found state |
| 409 | Conflict (duplicate name/slug) | Show backend error message |
| 422 | Validation error | Show field-level errors |
| 500 | Server error | Show "Something went wrong" |

---

## 7. DTO Rules

- Frontend TypeScript types **must mirror backend DTOs exactly**.
- Never add extra fields.
- Never omit fields that backend returns.
- When backend DTO changes, update frontend types **immediately** — stale types cause silent bugs.

---

## 8. Enum Rules

- Never hardcode enum values like `"APPROVED"` or `"MEDIUM"` in frontend logic.
- Always consume enum values from backend responses.
- If frontend needs a display label, map it on the frontend side.

---

## 9. API Client Rules

One centralized API client (`src/api/client.ts`):

```typescript
const apiClient = axios.create({ baseURL: VITE_API_URL });
apiClient.interceptors.request.use(/* attach token */);
apiClient.interceptors.response.use(/* handle 401 refresh */);
```

- Never call `fetch()` directly.
- Never create duplicate axios instances.
- All auth logic (attach token, refresh, 401 handling) lives in the interceptors.

---

## 10. State Management Rules

- Use React Query for server state.
- Use mutations (`useMutation`) for all writes.
- Invalidate relevant queries after successful mutations.
- Keep local state only for UI concerns (form input, modals, toggles).
- Avoid duplicating server data in local state.

---

## 11. Forms

- Use React Hook Form for validation.
- Validate on frontend for UX.
- Backend is the **final validator** — frontend validation is convenience, not authority.

---

## 12. File Structure

```
src/
  api/         # API client + endpoint modules
  hooks/       # React Query hooks
  types/       # TypeScript interfaces (mirror backend DTOs)
  features/    # Feature-based UI components
  components/  # Shared UI components
  pages/       # Route pages
```

- Never place API calls inside UI components.
- API calls belong in `src/api/`.
- Data fetching belongs in `src/hooks/`.

---

## 13. Frontend Responsibilities

### Frontend owns:
- Rendering UI
- Loading states (skeletons, spinners)
- Error states (error boundaries, retry)
- Optimistic updates (where appropriate)
- Input validation UX (required fields, format hints)

### Frontend does NOT own:
- Authorization logic
- Business rules ("can user edit this?")
- Permission checking
- Moderation workflows
- Data persistence logic

All of the above belong in the backend.

---

## 14. AI Agent Rules

Every AI coding agent MUST:

1. Read this contract **first** before making any changes.
2. Read `API_DOCUMENTATION.md` to find endpoints — never invent them.
3. Never hardcode URLs, DTOs, or enums.
4. Never bypass authentication flow.
5. Never duplicate backend business logic in frontend.
6. Always prefer consistency over cleverness.

---

## 15. PR Checklist

Before every pull request, verify:

- [ ] Uses only documented endpoints
- [ ] Uses `ApiResponse` wrapper pattern
- [ ] Uses centralized API client
- [ ] No hardcoded URLs
- [ ] TypeScript types match backend DTOs
- [ ] Handles loading state
- [ ] Handles error state
- [ ] Handles 401 refresh flow
- [ ] Handles 403 permission errors
- [ ] React Query cache invalidated correctly after mutations

---

## 16. Guiding Principle

```
Frontend displays data.
Backend owns data.
```

If business logic starts appearing in React components, it probably belongs in the backend.

**The backend API is the single source of truth. Never fight it.**
