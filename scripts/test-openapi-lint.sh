#!/usr/bin/env bash
# Self-test for .spectral.yaml: the violation fixture must fail with every expected rule.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FIXTURE="$SCRIPT_DIR/fixtures/openapi/lint-violations.yaml"
EXPECTED_RULES=(
  operation-operationId
  proxigaz-operation-documents-client-errors
  proxigaz-required-properties-defined
  oas3-unused-component
)

set +e
OUTPUT="$("$SCRIPT_DIR/validate-openapi.sh" "$FIXTURE" 2>&1)"
STATUS=$?
set -e

if [[ "$STATUS" -eq 0 ]]; then
  echo "FAIL: the lint-violation fixture passed validation." >&2
  echo "$OUTPUT" >&2
  exit 1
fi

failures=0
for rule in "${EXPECTED_RULES[@]}"; do
  if grep -Eq "error +${rule} " <<<"$OUTPUT"; then
    echo "PASS: ${rule} reported as error."
  else
    echo "FAIL: ${rule} was not reported as error." >&2
    failures=$((failures + 1))
  fi
done

if [[ "$failures" -gt 0 ]]; then
  echo "$OUTPUT" >&2
  exit 1
fi
echo "Spectral ruleset self-test passed."
