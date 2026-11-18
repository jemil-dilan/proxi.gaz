#!/usr/bin/env python3
"""Prepare the versioned OpenAPI contract published with a release.

Copies the source contract, sets ``info.version`` to the release SemVer and,
optionally, appends a staging entry to ``servers``. The source file is never
modified, so the repository keeps its development version marker.

Standard library only: GitHub runners and developer machines need no extra
packages. Edits are line-based and limited to the top-level ``info`` and
``servers`` blocks; the result is linted by ``scripts/validate-openapi.sh``.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

# https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
SEMVER = re.compile(
    r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)"
    r"(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)"
    r"(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
    r"(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"
)
HTTPS_URL = re.compile(r"^https://[^\s/\"]+(/[^\s\"]*)?$")
INFO_VERSION = re.compile(r"^  version:\s*")


class ReleaseContractError(ValueError):
    """The contract or the release inputs cannot produce a valid release contract."""


def _top_level_block(lines: list[str], key: str) -> tuple[int, int]:
    """Return (start, end) line indexes of a top-level ``key:`` block; end is exclusive."""
    header = f"{key}:"
    for start, line in enumerate(lines):
        if line.rstrip() == header:
            end = start + 1
            while end < len(lines) and (not lines[end].strip() or lines[end][0] in " #"):
                end += 1
            # Do not swallow trailing blank/comment lines that separate blocks.
            while end > start + 1 and not lines[end - 1].strip():
                end -= 1
            return start, end
    raise ReleaseContractError(f"The contract has no top-level '{key}' block.")


def _set_version(lines: list[str], version: str) -> None:
    start, end = _top_level_block(lines, "info")
    for index in range(start + 1, end):
        if INFO_VERSION.match(lines[index]):
            lines[index] = f'  version: "{version}"'
            return
    raise ReleaseContractError("The contract has no 'info.version' to replace.")


def _add_staging_server(lines: list[str], staging_url: str) -> None:
    if not HTTPS_URL.match(staging_url):
        raise ReleaseContractError(
            f"Invalid staging URL: {staging_url!r}. Expected an https:// URL."
        )
    start, end = _top_level_block(lines, "servers")
    entry = f'  - url: "{staging_url}"'
    if any(line.rstrip() == entry for line in lines[start + 1 : end]):
        return
    lines[end:end] = [entry, '    description: "Staging"']


def prepare(contract: str, version: str, staging_url: str | None = None) -> str:
    """Return ``contract`` with the release version and optional staging server."""
    if not SEMVER.match(version):
        raise ReleaseContractError(
            f"Invalid version: {version!r}. Expected SemVer without a leading 'v'."
        )
    trailing_newline = contract.endswith("\n")
    lines = contract.splitlines()
    _set_version(lines, version)
    if staging_url:
        _add_staging_server(lines, staging_url)
    result = "\n".join(lines)
    return result + "\n" if trailing_newline else result


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--source", required=True, type=Path, help="Source OpenAPI YAML")
    parser.add_argument("--output", required=True, type=Path, help="Release OpenAPI YAML to write")
    parser.add_argument("--version", required=True, help="Release SemVer without leading 'v'")
    parser.add_argument("--staging-url", help="Optional https:// staging API base URL")
    args = parser.parse_args(argv)

    try:
        prepared = prepare(args.source.read_text(encoding="utf-8"), args.version, args.staging_url)
    except (OSError, ReleaseContractError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(prepared, encoding="utf-8")
    print(f"Wrote {args.output} (info.version={args.version})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
