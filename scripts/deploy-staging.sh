#!/usr/bin/env bash
set -Eeuo pipefail

IMAGE_REF="${1:?Usage: deploy-staging.sh ghcr.io/owner/repository@sha256:digest}"
APP_DIR="${PROXIGAZ_STAGING_DIR:-/opt/proxi-gaz/staging}"
STAGING_ENV_FILE="${STAGING_ENV_FILE:-/etc/proxi-gaz/staging.env}"
COMPOSE_FILE="${APP_DIR}/compose.staging.yaml"
IMAGE_ENV_FILE="${APP_DIR}/image.env"
SMOKE_SCRIPT="${APP_DIR}/staging-smoke.sh"

if [[ ! "${IMAGE_REF}" =~ ^ghcr\.io/[a-z0-9_.-]+/[a-z0-9_.-]+@sha256:[a-f0-9]{64}$ ]]; then
  echo "Refusing non-immutable or malformed image reference: ${IMAGE_REF}" >&2
  exit 2
fi

for required_file in "${STAGING_ENV_FILE}" "${COMPOSE_FILE}" "${SMOKE_SCRIPT}"; do
  if [[ ! -r "${required_file}" ]]; then
    echo "Required deployment file is missing or unreadable: ${required_file}" >&2
    exit 2
  fi
done

if [[ "$(stat -c '%a' "${STAGING_ENV_FILE}")" != "600" ]]; then
  echo "Staging environment file must have mode 0600: ${STAGING_ENV_FILE}" >&2
  exit 2
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose v2 is required on the staging host." >&2
  exit 2
fi

export STAGING_ENV_FILE

compose() {
  docker compose \
    --project-name proxigaz-staging \
    --env-file "${IMAGE_ENV_FILE}" \
    --file "${COMPOSE_FILE}" \
    "$@"
}

previous_image_ref=""
if [[ -f "${IMAGE_ENV_FILE}" ]]; then
  previous_image_ref="$(sed -n 's/^IMAGE_REF=//p' "${IMAGE_ENV_FILE}" | head -n 1)"
fi

write_image_ref() {
  local value="$1"
  local tmp_file="${IMAGE_ENV_FILE}.tmp"
  printf 'IMAGE_REF=%s\n' "${value}" >"${tmp_file}"
  chmod 640 "${tmp_file}"
  mv "${tmp_file}" "${IMAGE_ENV_FILE}"
}

rollback() {
  local reason="$1"
  echo "${reason}; attempting application-image rollback." >&2
  if [[ -n "${previous_image_ref}" ]]; then
    write_image_ref "${previous_image_ref}"
    compose pull app && compose up -d --no-deps app
  else
    compose down --remove-orphans
  fi
  exit 1
}

write_image_ref "${IMAGE_REF}"
if ! compose pull app; then
  rollback "Could not pull the new application image"
fi
if ! compose up -d --no-deps app; then
  rollback "New application container failed to start"
fi

for attempt in $(seq 1 30); do
  if "${SMOKE_SCRIPT}" "http://127.0.0.1:8080" 2>/dev/null; then
    echo "Deployed ${IMAGE_REF} successfully."
    exit 0
  fi
  sleep 5
done

compose logs --tail=100 app >&2 || true
rollback "New application container did not pass staging smoke checks"
