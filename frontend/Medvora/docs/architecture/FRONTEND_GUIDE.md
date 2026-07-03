# Frontend Guide

## Login Flow

1. POST `/api/auth/login` with `usernameOrEmail` and `password`.
2. Store `accessToken` in memory and `refreshToken` in secure storage.
3. Send `Authorization: Bearer <accessToken>` for protected APIs.
4. Call GET `/api/auth/me` after login to hydrate current user state.

## Refresh Token Flow

1. On `401`, POST `/api/auth/refresh` with `{ "refreshToken": "..." }`.
2. Replace both tokens from returned `AuthResponse`.
3. Retry original request once.
4. If refresh fails, clear auth state and redirect to login.

## Logout Flow

1. POST `/api/auth/logout`.
2. Include `Authorization` header when access token exists.
3. Include optional `{ refreshToken }` body when refresh token exists.
4. Clear local auth state after success.

## Pagination

Use `page`, `size`, `sort` for paginated endpoints.

Example:

`GET /api/diseases?page=0&size=20&sort=updatedAt,desc`

Read list data from:

- `data.content`
- `data.page`
- `data.size`
- `data.totalElements`
- `data.totalPages`
- `data.first`
- `data.last`

## Error Handling

All non-SSE errors return:

```json
{
  "timestamp": "2026-07-02T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation message",
  "path": "/api/example"
}
```

Frontend should render `message` and branch on `status`.

## Disease / Document Flow

Current backend domain uses diseases, versions, and sections instead of generic documents.

- create disease: POST `/api/diseases`
- create draft: POST `/api/diseases/draft`
- create version: POST `/api/versions/draft?diseaseId=`
- update version: PUT `/api/versions/{versionId}`
- manage sections: `/api/sections/*`
- submit review: POST `/api/versions/{versionId}/submit`
- approve/reject: POST `/api/versions/{versionId}/approve|reject`

Suggested UI shape:

- disease list page -> `/api/diseases`
- disease detail page -> `/api/diseases/slug/{slug}`
- editor page -> `/api/versions/disease/{diseaseId}/latest-draft` + `/api/sections/version/{versionId}`

## Upload Flow

No file upload REST endpoint exists in current backend.

Frontend should not assume upload capability yet.
If upload is added later, keep success payload aligned with:

- `id`
- `filename`
- `url`
- `size`
- `contentType`

## Flashcard Flow

No flashcard endpoint exists in current backend.
Frontend should hide or mock this feature until backend endpoints are added.

## Quiz Flow

No quiz endpoint exists in current backend.
Frontend should hide or mock this feature until backend endpoints are added.

## Case Study Flow

- list cases: GET `/api/cases`
- get detail: GET `/api/cases/{id}` or `/api/cases/slug/{slug}`
- submit diagnosis: POST `/api/cases/{id}/diagnose`

## AI Flow

- basic checker: POST `/api/symptom-checker/check`
- advanced analyze: POST `/api/symptom-checker/analyze`
- AI stream: GET `/api/ai/stream` as `EventSource`
- notification stream: GET `/api/notifications/stream` as `EventSource`

## SSE Notes

SSE endpoints do not return `ApiResponse`.
Use browser `EventSource` or equivalent streaming client.
Reconnect on network drop.

## Recommended Frontend API Wrapper

- attach bearer token automatically
- on `401`, try refresh once
- normalize error payload from `message`
- unwrap success payload from `response.data.data`
