#!/usr/bin/env bash
# Install a pinned Hurl release after verifying its SHA-256 checksum.
# Installs to $HURL_INSTALL_DIR (default: ~/.local/bin). Linux x86_64/aarch64 only.
# The binary links against the system libcurl and libxml2 (present on Ubuntu runners).
set -euo pipefail

HURL_VERSION="8.1.0"
declare -A HURL_SHA256=(
  [x86_64]="b497e7ab328100c507f1f3323ebe7cc5990d831ae4f9473b74d40b02971da4ca"
  [aarch64]="33c39409d4a24febb91d3766d3a5cb4074b7b86ad89d612f26fa35ab5e8cec79"
)
INSTALL_DIR="${HURL_INSTALL_DIR:-$HOME/.local/bin}"

if [[ "$(uname -s)" != "Linux" ]]; then
  echo "install-hurl.sh supports Linux only; install Hurl ${HURL_VERSION} manually (https://hurl.dev)." >&2
  exit 1
fi

case "$(uname -m)" in
  x86_64 | amd64) ARCH="x86_64" ;;
  aarch64 | arm64) ARCH="aarch64" ;;
  *)
    echo "Unsupported architecture: $(uname -m)" >&2
    exit 1
    ;;
esac

if [[ -x "$INSTALL_DIR/hurl" ]] && "$INSTALL_DIR/hurl" --version 2>/dev/null | head -1 | grep -Fq "hurl ${HURL_VERSION}"; then
  echo "hurl ${HURL_VERSION} already installed at $INSTALL_DIR/hurl"
  exit 0
fi

NAME="hurl-${HURL_VERSION}-${ARCH}-unknown-linux-gnu"
ARCHIVE="${NAME}.tar.gz"
URL="https://github.com/Orange-OpenSource/hurl/releases/download/${HURL_VERSION}/${ARCHIVE}"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

curl --fail --silent --show-error --location --retry 3 --output "$WORK_DIR/$ARCHIVE" "$URL"
echo "${HURL_SHA256[$ARCH]}  $WORK_DIR/$ARCHIVE" | sha256sum --check --quiet

tar -xzf "$WORK_DIR/$ARCHIVE" -C "$WORK_DIR"
mkdir -p "$INSTALL_DIR"
install -m 0755 "$WORK_DIR/$NAME/bin/hurl" "$INSTALL_DIR/hurl"

if ! VERSION_LINE="$("$INSTALL_DIR/hurl" --version 2>&1 | head -1)" || [[ "$VERSION_LINE" != hurl* ]]; then
  rm -f "$INSTALL_DIR/hurl"
  echo "The prebuilt Hurl binary cannot run here: $VERSION_LINE" >&2
  echo "It needs libcurl 4 and libxml2.so.2 (Ubuntu 26.04 ships libxml2.so.16)." >&2
  echo "scripts/run-api-scenarios.sh falls back to the pinned Hurl Docker image when Docker is available." >&2
  exit 1
fi
echo "Installed $VERSION_LINE to $INSTALL_DIR/hurl"
