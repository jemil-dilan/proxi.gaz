"""Unit tests for scripts/prepare_openapi_release.py (stdlib only).

Run from the repository root:
    python3 -m unittest scripts.tests.test_prepare_openapi_release -v
"""

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from scripts.prepare_openapi_release import ReleaseContractError, prepare

REPO_ROOT = Path(__file__).resolve().parents[2]
MAIN_CONTRACT = REPO_ROOT / "specs" / "openapi" / "main.yaml"
SCRIPT = REPO_ROOT / "scripts" / "prepare_openapi_release.py"

CONTRACT = """\
---
openapi: "3.0.3"
info:
  title: "PROXIGAZ"
  description: "REST API endpoints for PROXIGAZ"
  version: "0.1.0-dev"
servers:
  - url: "https://api.proxigaz.cm"
    description: "Production"
  - url: "http://localhost:8080"
    description: "Local development"
paths:
  /things:
    get:
      operationId: "getThings"
      parameters:
        - name: "version"
          in: "query"
          schema:
            type: "string"
      responses:
        "200":
          description: "OK"
components:
  schemas:
    ThingDTO:
      type: "object"
      properties:
        version:
          type: "string"
"""


class PrepareVersionTest(unittest.TestCase):
    def test_sets_info_version_to_release_version(self):
        result = prepare(CONTRACT, "0.1.0")

        self.assertIn('  version: "0.1.0"\n', result)
        self.assertNotIn("0.1.0-dev", result)

    def test_accepts_prerelease_version(self):
        result = prepare(CONTRACT, "0.2.0-alpha.1")

        self.assertIn('  version: "0.2.0-alpha.1"\n', result)

    def test_only_info_version_changes(self):
        result = prepare(CONTRACT, "0.1.0")

        changed = [
            (before, after)
            for before, after in zip(CONTRACT.splitlines(), result.splitlines())
            if before != after
        ]
        self.assertEqual([('  version: "0.1.0-dev"', '  version: "0.1.0"')], changed)

    def test_rejects_invalid_semver(self):
        for bad in ["", "v0.1.0", "0.1", "01.0.0", "0.1.0-", "0.1.0 beta", "latest"]:
            with self.subTest(version=bad):
                with self.assertRaises(ReleaseContractError):
                    prepare(CONTRACT, bad)

    def test_rejects_contract_without_info_version(self):
        contract = CONTRACT.replace('  version: "0.1.0-dev"\n', "")

        with self.assertRaises(ReleaseContractError):
            prepare(contract, "0.1.0")


class PrepareStagingServerTest(unittest.TestCase):
    def test_no_staging_url_leaves_servers_untouched(self):
        result = prepare(CONTRACT, "0.1.0")

        self.assertNotIn("Staging", result)
        self.assertEqual(len(CONTRACT.splitlines()), len(result.splitlines()))

    def test_adds_staging_server_after_existing_servers(self):
        result = prepare(CONTRACT, "0.1.0", "https://staging.api.proxigaz.cm")

        expected = (
            "servers:\n"
            '  - url: "https://api.proxigaz.cm"\n'
            '    description: "Production"\n'
            '  - url: "http://localhost:8080"\n'
            '    description: "Local development"\n'
            '  - url: "https://staging.api.proxigaz.cm"\n'
            '    description: "Staging"\n'
            "paths:\n"
        )
        self.assertIn(expected, result)

    def test_does_not_duplicate_existing_staging_server(self):
        once = prepare(CONTRACT, "0.1.0", "https://staging.api.proxigaz.cm")
        twice = prepare(once, "0.1.0", "https://staging.api.proxigaz.cm")

        self.assertEqual(once, twice)
        self.assertEqual(1, twice.count("https://staging.api.proxigaz.cm"))

    def test_rejects_non_https_staging_url(self):
        for bad in ["http://staging.api.proxigaz.cm", "staging.api.proxigaz.cm", "https://", "https://a b"]:
            with self.subTest(url=bad):
                with self.assertRaises(ReleaseContractError):
                    prepare(CONTRACT, "0.1.0", bad)

    def test_rejects_contract_without_servers(self):
        start = CONTRACT.index("servers:")
        end = CONTRACT.index("paths:")
        contract = CONTRACT[:start] + CONTRACT[end:]

        with self.assertRaises(ReleaseContractError):
            prepare(contract, "0.1.0", "https://staging.api.proxigaz.cm")


class RealContractTest(unittest.TestCase):
    def test_project_contract_can_be_prepared(self):
        source = MAIN_CONTRACT.read_text(encoding="utf-8")

        result = prepare(source, "0.1.0", "https://staging.api.proxigaz.cm")

        self.assertIn('  version: "0.1.0"\n', result)
        self.assertIn('description: "Staging"', result)
        self.assertEqual(len(source.splitlines()) + 2, len(result.splitlines()))


class CommandLineTest(unittest.TestCase):
    def run_script(self, *args):
        return subprocess.run(
            [sys.executable, "-I", str(SCRIPT), *args],
            capture_output=True,
            text=True,
            check=False,
        )

    def test_writes_prepared_contract_to_output(self):
        with tempfile.TemporaryDirectory() as tmp:
            source = Path(tmp) / "main.yaml"
            output = Path(tmp) / "out" / "openapi-0.1.0.yaml"
            source.write_text(CONTRACT, encoding="utf-8")

            completed = self.run_script(
                "--source", str(source),
                "--output", str(output),
                "--version", "0.1.0",
                "--staging-url", "https://staging.api.proxigaz.cm",
            )

            self.assertEqual(0, completed.returncode, completed.stderr)
            written = output.read_text(encoding="utf-8")
            self.assertIn('  version: "0.1.0"\n', written)
            self.assertIn('description: "Staging"', written)
            self.assertEqual(CONTRACT, source.read_text(encoding="utf-8"))

    def test_invalid_version_exits_non_zero_without_output(self):
        with tempfile.TemporaryDirectory() as tmp:
            source = Path(tmp) / "main.yaml"
            output = Path(tmp) / "openapi.yaml"
            source.write_text(CONTRACT, encoding="utf-8")

            completed = self.run_script(
                "--source", str(source), "--output", str(output), "--version", "v0.1.0"
            )

            self.assertNotEqual(0, completed.returncode)
            self.assertIn("Invalid", completed.stderr)
            self.assertFalse(output.exists())


if __name__ == "__main__":
    unittest.main()
