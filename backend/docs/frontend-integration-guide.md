# Frontend Integration Guide

**Generated:** 2026-06-21  
**Base URL:** `http://localhost:6061/api`  
**API Version:** 1.0

## Table of Contents

1. [Common Patterns](#common-patterns)
2. [Login Flow](#login-flow)
3. [Token Management](#token-management)
4. [Logout Flow](#logout-flow)
5. [Disease Creation Flow](#disease-creation-flow)
6. [Disease Approval Flow (Reviewer)](#disease-approval-flow-reviewer)
7. [Error Handling](#error-handling)
8. [Pagination](#pagination)
9. [Swagger/OpenAPI](#swaggeropenapi)
10. [All Roles & Permissions](#roles--permissions)

---

## Common Patterns

### Request Setup

```javascript
// Axios interceptor setup
const api = axios.create({
  baseURL: 'http://localhost:6061/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Unified Response Wrapper

Every endpoint returns `ApiResponse<T>`:

```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string; // ISO-8601
}

// Success response:
{
  success: true,
  message: "Operation successful",
  data: { /* T */ },
  timestamp: "2026-06-21T10:30:45.123456"
}

// Error response:
{
  success: false,
  message: "Error description",
  data: null,
  timestamp: "2026-06-21T10:30:45.123456"
}
```

### Error Interceptor

```javascript
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Attempt token refresh
      const refreshed = await attemptTokenRefresh();
      if (!refreshed) {
        // Redirect to login
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

---

## Login Flow

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Login     │     │   Backend    │     │   Store     │
│   Form      │────▶│   /login     │────▶│   Tokens    │
└─────────────┘     └──────────────┘     └──────┬──────┘
                                                │
                                        ┌───────▼───────┐
                                        │  Redirect to  │
                                        │   Dashboard   │
                                        └───────────────┘
```

### Step-by-Step

**Step 1:** User submits username + password.

```javascript
async function login(username, password) {
  const response = await axios.post('/api/auth/login', { username, password });
  return response.data; // ApiResponse<AuthResponse>
}
```

**Step 2:** Store tokens and user info.

```javascript
function onLoginSuccess(response) {
  const { accessToken, refreshToken, user } = response.data;
  
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
  localStorage.setItem('user', JSON.stringify(user));
  
  // Redirect to dashboard
  window.location.href = '/dashboard';
}
```

**Step 3:** Use access token in all subsequent API calls.

```javascript
// Axios will automatically attach via interceptor
const me = await axios.get('/api/auth/me');
```

### Full Flow Types

```typescript
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: 3600; // seconds
  user: UserDTO;
}

interface UserDTO {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  roles: string[];    // e.g., ["ADMIN", "REVIEWER", "CONTRIBUTOR"]
  isVerified: boolean;
  status: string;     // "ACTIVE" | "INACTIVE" | "PENDING" | "BANNED"
}
```

---

## Token Management

### Token Lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                        Token Lifecycle                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   /login ──────▶ accessToken (1h) ◄───/refresh ──▶ new pair │
│                  refreshToken (7d)                           │
│                                                              │
│   Access token:  Bearer <accessToken>                        │
│   Refresh token: Sent in request body to /refresh            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Refresh Token Flow

Automatically when `401 Unauthorized` is received:

```javascript
let isRefreshing = false;
let failedQueue = [];

async function attemptTokenRefresh() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return false;
  
  try {
    const response = await axios.post('/api/auth/refresh', { refreshToken });
    const { accessToken, refreshToken: newRefreshToken } = response.data.data;
    
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', newRefreshToken);
    return true;
  } catch (error) {
    return false;
  }
}

// Axios response interceptor with queue
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        });
      }
      
      originalRequest._retry = true;
      isRefreshing = true;
      
      const refreshed = await attemptTokenRefresh();
      isRefreshing = false;
      
      if (refreshed) {
        // Retry failed requests in queue
        failedQueue.forEach(({ resolve, reject }) => {
          resolve(api(originalRequest));
        });
        failedQueue = [];
        
        // Retry original request
        originalRequest.headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`;
        return api(originalRequest);
      }
      
      failedQueue.forEach(({ reject }) => reject(error));
      failedQueue = [];
      localStorage.clear();
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);
```

---

## Logout Flow

```javascript
async function logout() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  try {
    await axios.post('/api/auth/logout', {
      refreshToken: refreshToken
    });
  } finally {
    // Always clean up locally
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    
    // Redirect to login
    window.location.href = '/login';
  }
}
```

---

## Reviewer Flow

### Workflow State Machine

```
                    ┌──────────────┐
                    │    DRAFT     │◄──────────┐
                    └──────┬───────┘           │
                           │ submit            │ resubmit
                    ┌──────▼───────┐           │
              ┌────▶│ PENDING_REV  ├──────┐    │
              │     └──────┬───────┘      │    │
              │            │              │    │
              │     ┌──────▼───────┐      │    │
              │     │   APPROVED   │      │ ┌──▼──────┐
              │     └──────┬───────┘      │ │ REJECTED│
              │            │              │ └──┬──────┘
              │     ┌──────▼───────┐      │    │
              │     │   ARCHIVED   │      │    │
              │     └──────────────┘      │    │
              └───────────────────────────┘    │
                                               │
                                               └────► DRAFT
```

### Reviewer Permissions

| Action | Permission | Endpoint |
|--------|------------|----------|
| View pending review | VERSION_REVIEW | GET `/versions/pending-review` |
| Approve version | VERSION_REVIEW | POST `/versions/{id}/approve` |
| Reject version | VERSION_REVIEW | POST `/versions/{id}/reject` |
| Archive version | VERSION_WRITE | POST `/versions/{id}/archive` |

### Reviewer Flow Sequence

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────────┐
│ Reviewer │   │  Fetch   │   │  Review  │   │   Approve    │
│  Login   │──▶│ Pending  │──▶│ Sections │──▶│  /reject/   │
│          │   │ Versions │   │          │   │  /approve   │
└──────────┘   └──────────┘   └──────────┘   └──────┬───────┘
                                                      │
                                              ┌───────▼───────┐
                                              │ Set as current│
                                              │ version       │
                                              │ (auto)        │
                                              └───────────────┘
```

### Step-by-Step Reviewer Flow

**Step 1:** Login as reviewer.

**Step 2:** Fetch pending review queue:
```javascript
const pending = await api.get('/versions/pending-review', {
  params: { page: 0, size: 20 }
});
```

**Step 3:** Review version detail and sections.

**Step 4:** Approve or reject:
```javascript
async function approveVersion(versionId) {
  return await api.post(`/versions/${versionId}/approve`, {
    note: "Approved with minor comments."
  });
}

async function rejectVersion(versionId) {
  return await api.post(`/versions/${versionId}/reject`, {
    note: "Missing sections: Causes."
  });
}
```

---

## Disease Creation Flow

```
┌──────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ Creator  │   │  Create      │   │  Create      │   │  Submit for  │
│  Login   │──▶│  Disease     │──▶│  Sections    │──▶│  Review      │
│          │   │  + Draft Ver │   │  (min 3 req) │   │              │
└──────────┘   └──────────────┘   └──────────────┘   └──────┬───────┘
                                                              │
                                                     ┌────────▼────────┐
                                                     │Reviewer approves│
                                                     │ or rejects     │
                                                     └────────┬────────┘
                                                              │
                                              ┌───────────────▼─────────┐
                                              │ Creator revises if      │
                                              │ rejected, re-submits    │
                                              └─────────────────────────┘
```

### Step-by-Step Disease Creation

**Step 1:** Login as CONTRIBUTOR.

**Step 2:** Create disease:
```javascript
// Creates disease + initial DRAFT version
const disease = await api.post('/diseases', {
  name: "Asthma",
  slug: "asthma",
  categoryId: 3
});
const diseaseId = disease.data.data.id;
```

**Step 3:** Get the draft version:
```javascript
const draftVersion = await api.get(`/versions/disease/${diseaseId}/latest-draft`);
const versionId = draftVersion.data.data.id;
```

**Step 4:** Add required sections (Definition, Symptoms, Treatment):
```javascript
const sections = await api.post(`/sections/batch?versionId=${versionId}`, [
  {
    sectionTypeId: 1,  // Definition
    title: "What is Asthma?",
    content: "<p>Asthma is a chronic...</p>",
    orderIndex: 0
  },
  {
    sectionTypeId: 2,  // Symptoms
    title: "Common Symptoms",
    content: "<ul><li>Wheezing</li><li>Coughing</li></ul>",
    orderIndex: 1
  },
  {
    sectionTypeId: 3,  // Treatment
    title: "Treatment",
    content: "<p>Inhaled corticosteroids...</p>",
    orderIndex: 2
  }
]);
```

**Step 5:** Submit for review:
```javascript
const submitted = await api.post(`/versions/${versionId}/submit`);
// Status changes: DRAFT → PENDING_REVIEW
```

**Step 6:** Check review status:
```javascript
const version = await api.get(`/versions/${versionId}`);
// Status: PENDING_REVIEW
```

**Step 7 (Reviewer):** Approve or reject.

**Step 8 (Creator):** If rejected, fix sections and re-submit:
```javascript
// Update section
await api.put(`/sections/${sectionId}`, {
  content: "<p>More detailed treatment info...</p>"
});

// Re-submit
await api.post(`/versions/${versionId}/submit`);
```

---

## Admin Endpoints

### GET /api/admin/users

Get all users (paginated).

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | USER_VIEW_ALL |
| **HTTP Method** | GET |

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 20)

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": { /* Page<UserDTO> */ }
}
```

---

### GET /api/admin/users/{id}

Get a specific user by ID.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | USER_VIEW_ALL |
| **HTTP Method** | GET |

---

### PATCH /api/admin/users/{id}/deactivate

Deactivate a user.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | USER_MANAGE |
| **HTTP Method** | PATCH |

---

### PATCH /api/admin/users/{id}/activate

Activate a deactivated user.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | USER_MANAGE |
| **HTTP Method** | PATCH |

---

### PATCH /api/admin/users/{id}/roles

Assign a role to a user.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | ROLE_ASSIGN |
| **HTTP Method** | PATCH |

**Request Body:**
```json
{
  "roleName": "CONTRIBUTOR"
}
```

---

### DELETE /api/admin/users/{id}/roles

Remove a role from a user.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | ROLE_ASSIGN |
| **HTTP Method** | DELETE |

**Request Body:**
```json
{
  "roleName": "CONTRIBUTOR"
}
```

---

## Symptom Checker Endpoint

### POST /api/symptom-checker/check

Check symptoms for matching diseases.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | POST |

**Query Parameters:**
- `limit` (optional) - Maximum results to return

**Request Body:**
```json
[1, 2, 3]
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "symptom": {
        "id": 1,
        "name": "Cough",
        "slug": "cough"
      },
      "matchedDiseases": [
        {
          "diseaseId": 1,
          "diseaseName": "Bronchitis",
          "weightScore": 0.85
        }
      ]
    }
  ]
}
```

---

## Streaming Endpoints (SSE)

### GET /api/notifications/stream

Stream real-time notifications to the client using Server-Sent Events (SSE).

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

**Implementation (client-side):**
```javascript
const eventSource = new EventSource(
  `/api/notifications/stream`,
  { withCredentials: false }
);

// Set Authorization manually (since EventSource doesn't support headers by default)
// Alternative: use fetch-based SSE reader
async function connectSSE() {
  const response = await fetch('/api/notifications/stream', {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
    }
  });
  
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    
    const text = decoder.decode(value);
    // Parse SSE data
    if (text.startsWith('data:')) {
      const event = JSON.parse(text.substring(5));
      handleNotification(event);
    }
  }
}
```

### GET /api/ai/stream

Stream AI-generated responses.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Permission** | Authenticated user |
| **HTTP Method** | GET |

---

## Roles & Permissions

### Role Hierarchy

```
ADMIN ─────▶ Full system access
REVIEWER ──▶ Can review and approve/reject versions
CONTRIBUTOR▶ Can create and edit content
USER ──────▶ Basic authenticated user
```

### Permission to Role Mapping

| Permission Name | Required For | Assigned To |
|----------------|-------------|-------------|
| DISEASE_WRITE | Create/update diseases | CONTRIBUTOR, REVIEWER, ADMIN |
| DISEASE_DELETE | Delete diseases | CONTRIBUTOR, REVIEWER, ADMIN |
| DISEASE_RESTORE | Restore diseases | ADMIN |
| DISEASE_MANAGE | Assign/remove categories | REVIEWER, ADMIN |
| VERSION_WRITE | Create/edit versions and sections | CONTRIBUTOR, REVIEWER, ADMIN |
| VERSION_DELETE | Delete versions | REVIEWER, ADMIN |
| VERSION_RESTORE | Restore versions | ADMIN |
| VERSION_READ | Read draft content | CONTRIBUTOR, REVIEWER, ADMIN |
| VERSION_REVIEW | Approve/reject versions | REVIEWER, ADMIN |
| USER_VIEW_ALL | View all users | ADMIN |
| USER_MANAGE | Deactivate/activate users | ADMIN |
| ROLE_ASSIGN | Assign/remove roles | ADMIN |

---

## Error Handling

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-06-21T10:30:45.123456"
}
```

### Validation Error Format

```json
{
  "success": false,
  "message": "username: Username is required; email: Invalid email format",
  "data": null,
  "timestamp": "2026-06-21T10:30:45.123456"
}
```

### Common HTTP Error Codes & Messages

| Status Code | Message | When It Occurs |
|-------------|---------|----------------|
| **400** | (field): (validation message) | Validation failed |
| **400** | Disease name already exists | Duplicate name |
| **400** | Email already verified | Resend verification for verified email |
| **400** | Section is not editable because version is not draft | Editing non-DRAFT version |
| **409** | Invalid workflow transition: DRAFT → APPROVED | Submitting invalid transition |
| **409** | Version is already pending review | Duplicate submit |
| **401** | Full authentication is required to access this resource | No token or expired token |
| **403** | Access denied | Insufficient permissions |
| **404** | Disease not found | Resource not found |
| **500** | An unexpected error occurred | Server error |

### Frontend Error Handling

```javascript
async function handleApiCall(apiCall) {
  try {
    const response = await apiCall;
    const { success, message, data } = response.data;
    
    if (!success) {
      showError(message);
      return null;
    }
    
    return data;
  } catch (error) {
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 400:
          showWarning(data.message);
          break;
        case 401:
          // Handled by interceptor
          break;
        case 403:
          showError('You do not have permission to perform this action');
          break;
        case 404:
          showError('Resource not found');
          break;
        case 409:
          showWarning(data.message); // Workflow conflict
          break;
        default:
          showError('An unexpected error occurred');
      }
    } else {
      showError('Network error. Please check your connection.');
    }
  }
}
```

---

## Pagination

### Request Parameters

All paginated endpoints accept the following query parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-indexed) |
| `size` | integer | 20 | Items per page |
| `sort` | string | - | Sort field + direction, e.g., `createdAt,desc` |

### Response Format

```json
{
  "content": [ /* Items */ ],
  "pageable": {
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "paged": true,
    "unpaged": false
  },
  "last": false,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "size": 20,
  "number": 0,
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "numberOfElements": 20,
  "empty": false
}
```

### Paginated Endpoints

| Endpoint | Description |
|----------|-------------|
| GET `/api/diseases/approved` | Approved diseases list |
| POST `/api/diseases/search` | Search diseases with filters |
| GET `/api/admin/users` | All users (admin) |
| GET `/api/versions/pending-review` | Pending review queue |

### Frontend Pagination Helper

```javascript
function buildPageable(page = 0, size = 20, sort = null) {
  const params = { page, size };
  if (sort) params.sort = sort;
  return params;
}

// Example: Load page 2, 10 items per page, sorted by createdAt descending
api.get('/api/diseases/approved', {
  params: buildPageable(2, 10, 'createdAt,desc')
});
```

---

## Swagger/OpenAPI

The API is documented via SpringDoc OpenAPI.

**Access Swagger UI:**

```
http://localhost:6061/swagger-ui.html
```

**Access OpenAPI JSON:**

```
http://localhost:6061/v3/api-docs
```

### What Swagger Provides

- Interactive API testing
- Request/response schemas
- Authentication setup (JWT Bearer token)
- Schema definitions for all DTOs
- Status code documentation
- Parameter descriptions (if annotated)

### Configure Swagger Authorization

OpenAPI configuration is available in `OpenApiConfig.java`. To test authenticated endpoints in Swagger UI:

1. Open Swagger UI at `/swagger-ui.html`
2. Click "Authorize" button
3. Enter JWT token: `Bearer <your-access-token>`
4. All subsequent requests include the token

---

## Complete Endpoint Index

| Group | Method | Path | Auth | Permission |
|-------|--------|------|------|-----------|
| Auth | POST | `/api/auth/register` | No | - |
| Auth | GET | `/api/auth/verify` | No | - |
| Auth | POST | `/api/auth/login` | No | - |
| Auth | POST | `/api/auth/refresh` | No | - |
| Auth | POST | `/api/auth/forgot-password` | No | - |
| Auth | POST | `/api/auth/reset-password` | No | - |
| Auth | POST | `/api/auth/resend-verification` | No | - |
| Auth | GET | `/api/auth/me` | Yes | authenticated |
| Auth | POST | `/api/auth/logout` | Yes | authenticated |
| User | GET | `/api/users/me` | Yes | authenticated |
| User | PUT | `/api/users/me` | Yes | authenticated |
| Disease | POST | `/api/diseases` | Yes | DISEASE_WRITE |
| Disease | POST | `/api/diseases/draft` | Yes | DISEASE_WRITE |
| Disease | GET | `/api/diseases/{id}` | Yes | authenticated |
| Disease | GET | `/api/diseases/slug/{slug}` | Yes | authenticated |
| Disease | GET | `/api/diseases/{id}/current-version` | Yes | authenticated |
| Disease | GET | `/api/diseases/approved` | Yes | authenticated |
| Disease | POST | `/api/diseases/search` | Yes | authenticated |
| Disease | PUT | `/api/diseases/{id}` | Yes | DISEASE_WRITE |
| Disease | DELETE | `/api/diseases/{id}` | Yes | DISEASE_DELETE |
| Disease | PATCH | `/api/diseases/{id}/restore` | Yes | DISEASE_RESTORE |
| Disease | PATCH | `/api/diseases/{id}/category/{categoryId}` | Yes | DISEASE_MANAGE |
| Disease | DELETE | `/api/diseases/{id}/category` | Yes | DISEASE_MANAGE |
| Disease | POST | `/api/diseases/{id}/clone-current-version` | Yes | VERSION_WRITE |
| Version | POST | `/api/versions/draft` | Yes | VERSION_WRITE |
| Version | POST | `/api/versions/clone/{diseaseId}` | Yes | VERSION_WRITE |
| Version | GET | `/api/versions/{versionId}` | Yes | authenticated |
| Version | GET | `/api/versions/disease/{diseaseId}` | Yes | authenticated |
| Version | GET | `/api/versions/disease/{diseaseId}/current` | Yes | authenticated |
| Version | GET | `/api/versions/disease/{diseaseId}/latest-draft` | Yes | VERSION_READ |
| Version | GET | `/api/versions/pending-review` | Yes | VERSION_REVIEW |
| Version | PUT | `/api/versions/{versionId}` | Yes | VERSION_WRITE |
| Version | POST | `/api/versions/{versionId}/submit` | Yes | VERSION_WRITE |
| Version | POST | `/api/versions/{versionId}/approve` | Yes | VERSION_REVIEW |
| Version | POST | `/api/versions/{versionId}/reject` | Yes | VERSION_REVIEW |
| Version | POST | `/api/versions/{versionId}/archive` | Yes | VERSION_WRITE |
| Version | DELETE | `/api/versions/{versionId}` | Yes | VERSION_DELETE |
| Version | PATCH | `/api/versions/{versionId}/restore` | Yes | VERSION_RESTORE |
| Section | POST | `/api/sections` | Yes | VERSION_WRITE |
| Section | POST | `/api/sections/batch` | Yes | VERSION_WRITE |
| Section | GET | `/api/sections/version/{versionId}` | Yes | authenticated |
| Section | GET | `/api/sections/version/{versionId}/type/{sectionType}` | Yes | authenticated |
| Section | PUT | `/api/sections/{sectionId}` | Yes | VERSION_WRITE |
| Section | POST | `/api/sections/version/{versionId}/reorder` | Yes | VERSION_WRITE |
| Section | DELETE | `/api/sections/{sectionId}` | Yes | VERSION_WRITE |
| Section | DELETE | `/api/sections/version/{versionId}` | Yes | VERSION_DELETE |
| Section | POST | `/api/sections/version/{versionId}/validate` | Yes | VERSION_WRITE |
| Section | GET | `/api/sections/{sectionId}` | Yes | authenticated |
| Section | GET | `/api/sections/{sectionId}/markdown` | Yes | authenticated |
| Section | GET | `/api/sections/types` | Yes | authenticated |
| Section | GET | `/api/sections/templates` | Yes | authenticated |
| Symptom | POST | `/api/symptom-checker/check` | Yes | authenticated |
| Admin | GET | `/api/admin/users` | Yes | USER_VIEW_ALL |
| Admin | GET | `/api/admin/users/{id}` | Yes | USER_VIEW_ALL |
| Admin | PATCH | `/api/admin/users/{id}/deactivate` | Yes | USER_MANAGE |
| Admin | PATCH | `/api/admin/users/{id}/activate` | Yes | USER_MANAGE |
| Admin | PATCH | `/api/admin/users/{id}/roles` | Yes | ROLE_ASSIGN |
| Admin | DELETE | `/api/admin/users/{id}/roles` | Yes | ROLE_ASSIGN |
| SSE | GET | `/api/notifications/stream` | Yes | authenticated |
| SSE | GET | `/api/ai/stream` | Yes | authenticated |

---

## Best Practices

1. **Always check `success` field** in API responses before using `data`
2. **Handle refresh token rotation** - when `/refresh` returns a new refresh token, update it locally
3. **Optimistic locking**: The system uses `@Version` for concurrent write protection. If you get a 409 with a version conflict, reload the resource and retry
4. **Paginate lists**: Always use pagination for list endpoints; never request unbounded results
5. **Use batch endpoints**: When creating sections, use the `/batch` endpoint instead of N individual requests
6. **SSE reconnection**: Implement SSE reconnection logic with exponential backoff
7. **Debounce search**: Debounce search inputs by 300ms before calling `/api/diseases/search`

