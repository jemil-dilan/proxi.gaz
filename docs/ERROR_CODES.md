# ProxiGaz Error Codes

This document defines the standard error codes used by the ProxiGaz API. All error codes follow the format `PROXIGAZ-{HTTP}-{NNN}` where:

- `{HTTP}` is the HTTP status code (e.g., 400, 401, 403, 404, 409, 429, 500)
- `{NNN}` is a sequential number within that HTTP status code group

## Error Response Format (RFC 7807)

All error responses follow the [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) format:

```json
{
  "type": "https://api.proxigaz.cm/probs/{problem-code}",
  "title": "Human-readable error type",
  "status": 404,
  "code": "PROXIGAZ-404-002",
  "correlationId": "abc123-def456",
  "detail": "Detailed error message",
  "timestamp": "2026-11-02T19:00:00Z",
  "errors": [
    {
      "field": "phone",
      "message": "phone size must be between 1 and 32"
    }
  ]
}
```

The `errors` array is only present for validation errors (400 responses).

## Error Code Registry

### 400 Bad Request

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-400-000` | Fields Extraction Code Not Supported | The requested extraction code is not supported | 400 |
| `PROXIGAZ-400-001` | Invalid Order Status Transition | Attempted to transition an order to an invalid status | 400 |
| `PROXIGAZ-400-002` | Validation Failed | Request validation failed | 400 |

### 401 Unauthorized

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-401-000` | Invalid Credentials | Authentication failed due to invalid credentials | 401 |
| `PROXIGAZ-401-001` | Token Invalid | The provided token is invalid or expired | 401 |

### 403 Forbidden

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-403-001` | Forbidden Audience | The token has the wrong audience for this endpoint | 403 |
| `PROXIGAZ-403-002` | Vendor Inactive | The vendor is inactive and cannot perform this action | 403 |

### 404 Not Found

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-404-000` | Demo Not Found | The requested demo was not found | 404 |
| `PROXIGAZ-404-001` | User Not Found | The requested user was not found | 404 |
| `PROXIGAZ-404-002` | Vendor Not Found | The requested vendor was not found | 404 |

### 409 Conflict

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-409-000` | Demo Already Exists | A demo with this identifier already exists | 409 |
| `PROXIGAZ-409-001` | Duplicate Phone | A vendor with this phone number already exists | 409 |
| `PROXIGAZ-409-002` | Duplicate Email | A user with this email already exists | 409 |

### 429 Too Many Requests

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-429-001` | Rate Limited | Too many requests in the allowed time window | 429 |

### 500 Internal Server Error

| Code | Title | Description | HTTP Status |
|------|-------|-------------|-------------|
| `PROXIGAZ-500-000` | Internal Server Error | An unexpected error occurred on the server | 500 |

## Problem Type URIs

Each error code maps to a problem type URI following the pattern:
`https://api.proxigaz.cm/probs/{problem-code}`

For example:
- `PROXIGAZ-404-002` → `https://api.proxigaz.cm/probs/002`
- `PROXIGAZ-409-001` → `https://api.proxigaz.cm/probs/001`

## Correlation ID

Every error response includes a `correlationId` that uniquely identifies this specific error occurrence. This ID is also included in the response headers (`X-Correlation-Id`) and can be used to:

1. Correlate error responses with server logs
2. Track a specific error across multiple service calls
3. Provide to support teams when reporting issues

The correlation ID is generated per-request and remains consistent for the duration of that request.

## Adding New Error Codes

When adding new error codes:

1. Add the code to the `ErrorCodes` enum in `src/main/java/cm/jemil/proxi/gaz/domain/ErrorCodes.java`
2. Add the code to this documentation file
3. Ensure the domain exception uses the correct error code
4. Verify the error code appears in the correct HTTP status code group

New error codes should follow the existing numbering scheme. When adding a new error code within an existing HTTP status code group, use the next available number.
