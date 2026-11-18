#!/usr/bin/env bash
# Self-test for scripts/check-openapi-breaking.sh.
# A revision that removes a required response field must fail the check;
# a revision that only adds an optional field must pass it.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FIXTURES="$SCRIPT_DIR/fixtures/openapi"
CHECK="$SCRIPT_DIR/check-openapi-breaking.sh"

if [[ ! -x "$CHECK" ]]; then
  echo "FAIL: $CHECK is missing or not executable." >&2
  exit 1
fi

failures=0

if "$CHECK" "$FIXTURES/base.yaml" "$FIXTURES/breaking-removed-required-response-field.yaml" >/dev/null 2>&1; then
  echo "FAIL: removing a required response field was not detected as breaking." >&2
  failures=$((failures + 1))
else
  echo "PASS: removing a required response field is reported as breaking."
fi

if "$CHECK" "$FIXTURES/base.yaml" "$FIXTURES/non-breaking-added-optional-field.yaml"; then
  echo "PASS: adding an optional response field is accepted."
else
  echo "FAIL: adding an optional response field was reported as breaking." >&2
  failures=$((failures + 1))
fi

if [[ "$failures" -gt 0 ]]; then
  exit 1
fi
echo "Breaking-change detection self-test passed."
