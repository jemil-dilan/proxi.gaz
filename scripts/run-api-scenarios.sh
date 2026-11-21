#!/usr/bin/env bash
# Run the API scenario collection (api-scenarios/sprint-*/*.hurl) against one environment.
#
# Usage: scripts/run-api-scenarios.sh [--reset-local] <local|staging>
#
#   --reset-local  rebuild the Jib image and restart the local Compose stack on a fresh database
#                  (DELETES the local Compose database volume), then wait for /actuator/health.
#
# Environment overrides (take precedence over api-scenarios/env/<env>.env):
#   PROXIGAZ_SCENARIO_BASE_URL, PROXIGAZ_SCENARIO_ADMIN_EMAIL, PROXIGAZ_SCENARIO_ADMIN_PASSWORD
#
# Each run uses a unique vendor name and phone, so the scenario can be repeated on shared data.
# Writes a JUnit report to build/api-scenarios/junit.xml. Uses a local hurl (scripts/install-hurl.sh)
# or, if none runs, the pinned Hurl Docker image.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESET_LOCAL=false
ENV_NAME=""

for arg in "$@"; do
  case "$arg" in
    --reset-local) RESET_LOCAL=true ;;
    -h | --help)
      sed -n '2,/^set -euo/p' "${BASH_SOURCE[0]}" | sed '$d' | sed 's/^# \{0,1\}//'
      exit 0
      ;;
    -*)
      echo "Unknown option: $arg" >&2
      exit 2
      ;;
    *) ENV_NAME="$arg" ;;
  esac
done

if [[ -z "$ENV_NAME" ]]; then
  echo "Usage: $0 [--reset-local] <local|staging>" >&2
  exit 2
fi

ENV_FILE="$ROOT/api-scenarios/env/${ENV_NAME}.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Unknown environment '$ENV_NAME': $ENV_FILE not found." >&2
  exit 2
fi

# Hurl: a working local binary of the pinned version, else the pinned official image (keep both
# in sync with scripts/install-hurl.sh).
HURL_VERSION="8.1.0"
HURL_IMAGE="ghcr.io/orange-opensource/hurl:${HURL_VERSION}@sha256:f8aed33658ba793ed53c5a5b2e57cadd9ec9322c8b56c550652c1f4cbaa241c5"
if command -v hurl >/dev/null 2>&1 && hurl --version 2>/dev/null | head -1 | grep -Fq "hurl ${HURL_VERSION} "; then
  HURL=(hurl)
elif command -v docker >/dev/null 2>&1; then
  # Host network so http://localhost reaches the local stack; the repository is mounted at the
  # same path so scenario and report paths stay valid inside the container. The admin password
  # is passed by name (--env), so its value never appears in the process list.
  HURL=(docker run --rm --network host --user "$(id -u):$(id -g)"
    --env HURL_SECRET_admin_password
    --volume "$ROOT:$ROOT" --workdir "$ROOT" "$HURL_IMAGE")
  echo "Using the Hurl ${HURL_VERSION} Docker image (no local hurl ${HURL_VERSION} binary that runs)."
else
  echo "hurl ${HURL_VERSION} is not available: run scripts/install-hurl.sh, or install Docker to use the Hurl image." >&2
  exit 2
fi

env_value() {
  # Last "key=value" for $1 in the env file, without surrounding whitespace.
  sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" "$ENV_FILE" | tail -n 1 | sed 's/[[:space:]]*$//'
}

BASE_URL="${PROXIGAZ_SCENARIO_BASE_URL:-$(env_value base_url)}"
ADMIN_EMAIL="${PROXIGAZ_SCENARIO_ADMIN_EMAIL:-$(env_value admin_email)}"
ADMIN_PASSWORD="${PROXIGAZ_SCENARIO_ADMIN_PASSWORD:-$(env_value admin_password)}"

for required in BASE_URL ADMIN_EMAIL ADMIN_PASSWORD; do
  if [[ -z "${!required}" ]]; then
    echo "$required is not set for '$ENV_NAME' (env file or PROXIGAZ_SCENARIO_* variable)." >&2
    exit 2
  fi
done

wait_for_health() {
  local url="$1/actuator/health" deadline=$((SECONDS + ${PROXIGAZ_SCENARIO_HEALTH_TIMEOUT:-180}))
  echo "Waiting for $url ..."
  until curl --silent --fail --output /dev/null "$url"; do
    if ((SECONDS >= deadline)); then
      echo "$url did not become healthy in time." >&2
      return 1
    fi
    sleep 3
  done
  echo "Healthy."
}

if [[ "$RESET_LOCAL" == true ]]; then
  if [[ "$ENV_NAME" != "local" ]]; then
    echo "--reset-local only applies to the local environment." >&2
    exit 2
  fi
  (
    cd "$ROOT"
    docker compose down --volumes --remove-orphans
    ./gradlew --quiet jibDockerBuild -Djib.to.image=proxigaz:local
    docker compose up --detach
  )
fi

REPORT_DIR="$ROOT/build/api-scenarios"
mkdir -p "$REPORT_DIR"
# Hurl appends to an existing JUnit file; keep the report to this run only.
rm -f "$REPORT_DIR/junit.xml"

wait_for_health "$BASE_URL"

# Fixed width (10-digit seconds + 3 digits) so one run id is never a prefix of another.
RUN_ID="run$(date +%s)$(printf '%03d' $((RANDOM % 1000)))"
# Local part of a +237 mobile number (6 + 8 digits); the scenario adds "+237".
VENDOR_LOCAL_NUMBER="6$(printf '%08d' $(((RANDOM * 32768 + RANDOM) % 100000000)))"
echo "Running API scenarios against $BASE_URL (run $RUN_ID)"
shopt -s nullglob
SCENARIOS=("$ROOT"/api-scenarios/sprint-*/*.hurl)
if ((${#SCENARIOS[@]} == 0)); then
  echo "No scenarios found under api-scenarios/." >&2
  exit 2
fi

# Hurl reads HURL_SECRET_<name> as a secret variable and redacts it in its output; passing it
# through the environment keeps the password out of the command line.
export HURL_SECRET_admin_password="$ADMIN_PASSWORD"

"${HURL[@]}" --test --jobs 1 \
  --variable "base_url=$BASE_URL" \
  --variable "admin_email=$ADMIN_EMAIL" \
  --variable "run_id=$RUN_ID" \
  --variable "vendor_name=Scenario Vendor $RUN_ID" \
  --variable "vendor_local_number=$VENDOR_LOCAL_NUMBER" \
  --report-junit "$REPORT_DIR/junit.xml" \
  "${SCENARIOS[@]}"
