# Sprint 1 scenario (PXG-063): admin onboards a vendor and manages its session.
#
# Variables (set by scripts/run-api-scenarios.sh):
#   base_url, admin_email, admin_password  - target environment and admin account
#   run_id, vendor_name                     - unique per run, so the scenario repeats on shared data
#   vendor_local_number                     - 9 digits after +237 (Hurl would read "+237..." as a number)
#
# Still to come when their tasks land (spec order: ... -> set brand + availability -> public nearby -> quote):
#   TODO PXG-064/065: set a brand and its availability for the vendor
#   TODO PXG-066:     find the vendor through the public nearby search
#   TODO PXG-067:     request a quote


# 1. Admin login
POST {{base_url}}/admin/auth/login
Content-Type: application/json
{
  "email": "{{admin_email}}",
  "password": "{{admin_password}}"
}
HTTP 200
[Captures]
admin_token: jsonpath "$.accessToken"
[Asserts]
jsonpath "$.tokenType" == "Bearer"
jsonpath "$.expiresIn" > 0


# 2. The token identifies an admin
GET {{base_url}}/admin/auth/me
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.email" == "{{admin_email}}"
jsonpath "$.roles" contains "admin"


# 3. Create a vendor; the one-time session token is never cached
POST {{base_url}}/admin/vendors
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "name": "{{vendor_name}}",
  "phone": "+237{{vendor_local_number}}",
  "latitude": 4.0511,
  "longitude": 9.7679,
  "addressText": "Scenario {{run_id}}, Akwa, Douala",
  "isStation": false,
  "deliveryFeeType": "FIXED",
  "deliveryFeeAmount": 500
}
HTTP 201
[Captures]
vendor_id: jsonpath "$.vendorId"
first_token: jsonpath "$.sessionToken"
[Asserts]
header "Cache-Control" contains "no-store"
jsonpath "$.sessionToken" isString


# 4. The admin sees the vendor and its active session
GET {{base_url}}/admin/vendors/{{vendor_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.name" == "{{vendor_name}}"
jsonpath "$.isActive" == true
jsonpath "$.hasActiveSession" == true


# 5. The vendor list finds it by name
GET {{base_url}}/admin/vendors
Authorization: Bearer {{admin_token}}
[Query]
name: {{run_id}}
HTTP 200
[Asserts]
jsonpath "$.totalElements" == 1
jsonpath "$.content[0].vendorId" == "{{vendor_id}}"


# 6. The vendor signs in with its one-time token
GET {{base_url}}/vendor/session
Authorization: Bearer {{first_token}}
HTTP 200
[Asserts]
jsonpath "$.vendorId" == "{{vendor_id}}"
jsonpath "$.name" == "{{vendor_name}}"
jsonpath "$.isActive" == true


# 7. The admin updates the profile
PUT {{base_url}}/admin/vendors/{{vendor_id}}
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "name": "{{vendor_name}} Hub",
  "phone": "+237{{vendor_local_number}}",
  "latitude": 4.0511,
  "longitude": 9.7679,
  "addressText": "Scenario {{run_id}}, Akwa, Douala",
  "isStation": true,
  "deliveryFeeType": "FIXED",
  "deliveryFeeAmount": 750
}
HTTP 204


# 8. A deactivated vendor is refused, then works again once reactivated
PUT {{base_url}}/admin/vendors/{{vendor_id}}/status
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{ "isActive": false }
HTTP 204

GET {{base_url}}/vendor/session
Authorization: Bearer {{first_token}}
HTTP 403
[Asserts]
header "Content-Type" contains "application/problem+json"
jsonpath "$.code" == "PROXIGAZ-403-002"

PUT {{base_url}}/admin/vendors/{{vendor_id}}/status
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{ "isActive": true }
HTTP 204

GET {{base_url}}/vendor/session
Authorization: Bearer {{first_token}}
HTTP 200
[Asserts]
jsonpath "$.name" == "{{vendor_name}} Hub"


# 9. Rotation issues a new token and invalidates the old one
POST {{base_url}}/admin/vendors/{{vendor_id}}/session
Authorization: Bearer {{admin_token}}
HTTP 201
[Captures]
second_token: jsonpath "$.sessionToken"
[Asserts]
header "Cache-Control" contains "no-store"
jsonpath "$.sessionToken" != "{{first_token}}"

GET {{base_url}}/vendor/session
Authorization: Bearer {{first_token}}
HTTP 401
[Asserts]
jsonpath "$.code" == "PROXIGAZ-401-001"

GET {{base_url}}/vendor/session
Authorization: Bearer {{second_token}}
HTTP 200


# 10. The audit log records every admin action, newest first, without secrets
GET {{base_url}}/admin/audit-log
Authorization: Bearer {{admin_token}}
[Query]
targetType: VENDOR
targetId: {{vendor_id}}
HTTP 200
[Asserts]
jsonpath "$.totalElements" == 5
jsonpath "$.content[0].action" == "VENDOR_SESSION_ROTATED"
jsonpath "$.content[1].action" == "VENDOR_ACTIVATED"
jsonpath "$.content[2].action" == "VENDOR_DEACTIVATED"
jsonpath "$.content[3].action" == "VENDOR_UPDATED"
jsonpath "$.content[4].action" == "VENDOR_CREATED"
jsonpath "$.content[3].metadata.changedFields" contains "name"
jsonpath "$.content[3].metadata.changedFields" contains "isStation"
jsonpath "$.content[3].metadata.changedFields" contains "deliveryFeeAmount"
jsonpath "$.content[0].adminEmail" == "{{admin_email}}"
body not contains "{{first_token}}"
body not contains "{{second_token}}"


# 11. Revocation ends the vendor session
DELETE {{base_url}}/admin/vendors/{{vendor_id}}/session
Authorization: Bearer {{admin_token}}
HTTP 204

GET {{base_url}}/vendor/session
Authorization: Bearer {{second_token}}
HTTP 401
[Asserts]
jsonpath "$.code" == "PROXIGAZ-401-001"


# 12. Admin endpoints reject anonymous calls with a traceable problem
GET {{base_url}}/admin/vendors
HTTP 401
[Asserts]
header "Content-Type" contains "application/problem+json"
header "X-Correlation-Id" exists
jsonpath "$.correlationId" isString
