#!/usr/bin/env bash
# Fail when <revision> breaks clients of <base> (e.g. a required response field is removed).
# Usage: scripts/check-openapi-breaking.sh <base.yaml> <revision.yaml>
# Requires oasdiff on PATH (see scripts/install-oasdiff.sh).
set -euo pipefail

if [[ "$#" -ne 2 ]]; then
  echo "Usage: $0 <base.yaml> <revision.yaml>" >&2
  exit 2
fi

BASE="$1"
REVISION="$2"

for file in "$BASE" "$REVISION"; do
  if [[ ! -f "$file" ]]; then
    echo "Contract not found: $file" >&2
    exit 2
  fi
done

if ! command -v oasdiff >/dev/null 2>&1; then
  echo "oasdiff is not installed; run scripts/install-oasdiff.sh" >&2
  exit 2
fi

if oasdiff breaking "$BASE" "$REVISION" --fail-on ERR --format text; then
  echo "No breaking API changes detected."
else
  echo "Breaking API changes detected (see above)." >&2
  exit 1
fi
