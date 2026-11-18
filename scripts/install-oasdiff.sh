#!/usr/bin/env bash
# Install a pinned oasdiff release after verifying its SHA-256 checksum.
# Installs to $OASDIFF_INSTALL_DIR (default: ~/.local/bin). Linux amd64/arm64 only.
set -euo pipefail

OASDIFF_VERSION="1.35.0"
declare -A OASDIFF_SHA256=(
  [amd64]="a8023002fcb2eaa7f5758148a9303cb63d33b939886df312268bc0fc7b939140"
  [arm64]="1f8b5aa033cd0240ae6c53460268bcd6e578526666a4487a781ece8cef39fbdd"
)
INSTALL_DIR="${OASDIFF_INSTALL_DIR:-$HOME/.local/bin}"

if [[ "$(uname -s)" != "Linux" ]]; then
  echo "install-oasdiff.sh supports Linux only; install oasdiff ${OASDIFF_VERSION} manually." >&2
  exit 1
fi

case "$(uname -m)" in
  x86_64 | amd64) ARCH="amd64" ;;
  aarch64 | arm64) ARCH="arm64" ;;
  *)
    echo "Unsupported architecture: $(uname -m)" >&2
    exit 1
    ;;
esac

if [[ -x "$INSTALL_DIR/oasdiff" ]] && "$INSTALL_DIR/oasdiff" --version 2>/dev/null | grep -Fq "$OASDIFF_VERSION"; then
  echo "oasdiff ${OASDIFF_VERSION} already installed at $INSTALL_DIR/oasdiff"
  exit 0
fi

ARCHIVE="oasdiff_${OASDIFF_VERSION}_linux_${ARCH}.tar.gz"
URL="https://github.com/oasdiff/oasdiff/releases/download/v${OASDIFF_VERSION}/${ARCHIVE}"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

curl --fail --silent --show-error --location --retry 3 --output "$WORK_DIR/$ARCHIVE" "$URL"
echo "${OASDIFF_SHA256[$ARCH]}  $WORK_DIR/$ARCHIVE" | sha256sum --check --quiet

tar -xzf "$WORK_DIR/$ARCHIVE" -C "$WORK_DIR" oasdiff
mkdir -p "$INSTALL_DIR"
install -m 0755 "$WORK_DIR/oasdiff" "$INSTALL_DIR/oasdiff"
echo "Installed $("$INSTALL_DIR/oasdiff" --version) to $INSTALL_DIR/oasdiff"
