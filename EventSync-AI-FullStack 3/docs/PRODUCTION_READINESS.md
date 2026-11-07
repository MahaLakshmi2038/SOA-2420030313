# Production readiness and limitations checklist

This repository provides a hardened deployment baseline, not a blanket guarantee of industrial readiness. Complete and record each check in the actual target environment.

## Included in this baseline
- PostgreSQL service with persistent named volume and internal-only database networking.
- Flyway versioned initial schema for the production profile; Hibernate schema validation rather than automatic schema changes.
- Environment-provided database credentials, initial administrator credentials, and signing secret in the production profile.
- Development-only demo administrator bootstrap; production bootstrap is configured from environment variables and requires a 14+ character password.
- Role checks on protected API routes; public registration can only create a volunteer.
- Password hashing with BCrypt; signed expiring bearer tokens; request authorization defaults to deny.
- Backend and frontend container definitions, health checks, non-root backend user, Nginx reverse proxy, basic security headers.
- Deterministic rule-based assignment scoring using skills, experience, and workload.

## Required before industrial use
- [ ] Terminate HTTPS with a managed certificate; set secure proxy headers and trusted proxy configuration.
- [ ] Replace local `.env` with a secrets manager; rotate credentials and the token signing key; document key rotation.
- [ ] Consider HttpOnly Secure SameSite cookies or OIDC/OAuth2; implement token revocation, MFA for privileged users, and account recovery.
- [ ] Review every endpoint and object-level authorization rule with security tests (including cross-user and cross-event access).
- [ ] Add rate limiting, login throttling, audit logs, account lockout policy, and abuse monitoring.
- [ ] Run SAST, dependency/CVE scans, container scans, DAST, penetration testing, and a privacy review.
- [ ] Add automated unit/integration/API contract tests; enforce them in CI; test migrations on a copy of production data.
- [ ] Load-test the expected concurrency and data size; optimize N+1 queries and add pagination for list endpoints.
- [ ] Set up database backups and perform a restore drill; define RPO/RTO, retention, disaster recovery, and rollback plans.
- [ ] Configure centralized logs, metrics, tracing, alerts, uptime checks, and an on-call process.
- [ ] Configure external email/SMS/push providers if required and test delivery/retry/dead-letter behavior.
- [ ] Implement authenticated real-time push (SSE/WebSocket) if sub-15-second instant cross-client updates are required; current dashboards poll every 15 seconds.
- [ ] Validate accessibility, browser compatibility, privacy notices, consent, data retention/deletion, and relevant local legal requirements.
- [ ] Review AI/rule recommendations for fairness and suitability; provide organizer override and clear explanations.

## Operational warning
The default development profile intentionally has demo credentials and an H2 database. Do not expose it publicly. For the containerized deployment, use `SPRING_PROFILES_ACTIVE=prod`, provide strong unique secrets, and expose only the frontend through a TLS-enabled reverse proxy.
