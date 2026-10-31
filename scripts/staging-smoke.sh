#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
BASE_URL="${BASE_URL%/}"

health_response="$(curl --fail --silent --show-error "${BASE_URL}/actuator/health")"
if ! grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' <<<"${health_response}"; then
  echo "Application health endpoint did not report UP." >&2
  exit 1
fi

vendor_session_status="$(curl --silent --show-error --output /dev/null --write-out '%{http_code}' "${BASE_URL}/vendor/session")"
if [[ "${vendor_session_status}" != "401" && "${vendor_session_status}" != "403" ]]; then
  echo "Unauthenticated vendor session request returned ${vendor_session_status}; expected 401 or 403." >&2
  exit 1
fi

echo "Staging smoke checks passed: health is UP and vendor session requires authentication."
