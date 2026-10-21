# Security Policy

## Supported Versions

ProxiGaz is pre-pilot. Only the current development line receives security fixes.

| Version | Supported |
|---------|-----------|
| `main` / `0.0.0-SNAPSHOT` (pre-release) | :white_check_mark: |
| Any published release tag (once cut) | Documented per release |
| Older / abandoned branches | :x: |

## Reporting a Vulnerability

Do **not** open a public GitHub issue for security vulnerabilities.

1. Prefer [GitHub private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability) on this repository if enabled.
2. Otherwise contact the repository maintainers privately (account that owns this repo).

You can expect:

- Acknowledgement when the report is received (typically within a few business days).
- An initial assessment of impact and whether a fix will be pursued.
- A coordinated disclosure timeline once a fix is available; please do not publish exploit details until then.

Accepted reports will be fixed on `main` (and on supported release tags when they exist). Declined reports will include a short rationale (e.g. out of scope, duplicate, or not considered a vulnerability).

## Project security expectations (pilot)

- No secrets in source control; use environment variables (see `.env.example` and `docs/RUNTIME.md`).
- Default JWT / DB credentials in config are for **local development only**.
- Personal data (phone, location) must be handled under applicable Cameroon data-protection rules before pilot launch.
