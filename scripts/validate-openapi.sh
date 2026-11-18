#!/usr/bin/env bash
# Lint an OpenAPI contract with the project's Spectral ruleset.
# Usage: scripts/validate-openapi.sh [contract.yaml]   (default: specs/openapi/main.yaml)
# Fails on rule violations of severity "error"; warnings are reported only.
set -euo pipefail

SPECTRAL_VERSION="6.15.0"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONTRACT="${1:-$ROOT/specs/openapi/main.yaml}"

if [[ ! -f "$CONTRACT" ]]; then
  echo "Contract not found: $CONTRACT" >&2
  exit 1
fi

npx --yes "@stoplight/spectral-cli@${SPECTRAL_VERSION}" lint "$CONTRACT" \
  --ruleset "$ROOT/.spectral.yaml" \
  --fail-severity error
