# Rules

- Never add sensitive information to this project (API keys, tokens, passwords, connection strings, secret keys, private keys, personal information...). Do not hardcode these values in source code, committed config files, or anywhere else in the repo. Sensitive values must be supplied via runtime configuration/environment variables kept outside the repo.
- Before `git add`/`git commit`, review the diff (`git status`, `git diff --cached`) to make sure no secret has slipped in, including in test resources, sample logs, or attachments (screenshots, DB exports...).
- Do not commit key/cert/keystore files (`.pem`, `.key`, `.p12`, `.pfx`, `.jks`), `.env` files, or personal settings files (`settings.xml` containing Maven repo credentials...). These patterns should be added to `.gitignore`.
- If sensitive information is found to have been committed (even in old commits), report it to the user immediately and ask how to proceed (rotate/revoke the exposed value, rewrite history...) — do not run `git filter-repo`, force-push, or rewrite history on your own without confirmation.
- Every push and pull request is scanned by the `gitleaks` GitHub Action (`.github/workflows/gitleaks.yml`, config in `.gitleaks.toml`). Known false positives (variable/method names, not real secrets) are allowlisted there — do not loosen the allowlist to hide an actual secret finding.
