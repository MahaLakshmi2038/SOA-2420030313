# EventSync AI — deployable baseline

EventSync AI is a React/Vite frontend and Spring Boot REST API for event coordination, volunteer profiles, tasks, assignments, notifications, and explainable rule-based volunteer matching. It supports role-based workspaces for `ADMIN`, `ORGANIZER`, `TEAM_LEAD`, and `VOLUNTEER`.

> **Readiness statement:** This repository is a production-oriented deployment baseline, not a claim of certified or fully validated industrial readiness. Run the security, integration, load, backup/restore, and operational checks in `docs/PRODUCTION_READINESS.md` before exposing it to real users or sensitive data. No software is limitation-free.

## Quick local development (Mac)

Requirements: Java 17+, Maven 3.9+, Node.js 20+, npm.

Terminal 1:
```bash
cd backend
mvn spring-boot:run
```
The default `dev` profile uses a local H2 file database and creates a demo administrator:
- Email: `admin@eventsync.local`
- Password: `Admin123!`

**Development only. Never use the demo credentials on a public server.**

Terminal 2:
```bash
cd frontend
npm install
npm run dev
```
Open <http://localhost:5173>. The Vite dev server proxies `/api` requests to port 8080.

## Containerized PostgreSQL deployment baseline

Requirements: Docker Desktop with Docker Compose v2.

1. Copy `.env.example` to `.env`.
2. Generate unique secret values. Example on macOS: `openssl rand -base64 48` for the database password and `openssl rand -base64 64` for `EVENTSYNC_JWT_SECRET`. Set a unique `EVENTSYNC_ADMIN_PASSWORD` of at least 14 characters. Set the actual initial admin email.
3. Keep `.env` private and never commit it.
4. Run from the project root:
```bash
docker compose up --build -d
docker compose ps
docker compose logs -f backend
```
5. Open <http://localhost:8088>. Health check: <http://localhost:8088/actuator/health>.
6. Sign in with the initial administrator and change the password immediately using the authenticated `POST /api/auth/password` endpoint. The endpoint requires the current password and a new password of 14–128 characters; it invalidates existing tokens. Sign in again with the new password.

The production profile uses PostgreSQL, Flyway schema migration, Hibernate schema validation, a non-root backend container, internal database networking, health checks, and a reverse proxy. PostgreSQL is not exposed on a host port by default. Back up the `postgres_data` volume using a tested backup procedure before upgrades.

## Role behavior

- `ADMIN`: manage team accounts and coordinate all events.
- `ORGANIZER`: manage event operations and assignments.
- `TEAM_LEAD`: review event/task/volunteer information and update permitted work items.
- `VOLUNTEER`: view their own profile and assignments and mark their own assigned work complete.
- Public registration only creates volunteer accounts. Privileged roles are created by an administrator.

## AI assignment behavior

The assignment recommender is an explainable, deterministic rule-based score using required skills, experience, and current workload. It is **not a trained machine-learning model** and should be reviewed by an organizer before high-impact assignments.

## Known boundaries

- The app uses signed bearer tokens stored in browser local storage; a production identity/security review should consider an HttpOnly Secure SameSite cookie or an external identity provider, token revocation, MFA, and XSS controls.
- The dashboards poll the API every 15 seconds for cross-user updates; this is near-real-time polling, not push delivery. External email/SMS integration, multi-region failover, and formal SLA support are not included.
- Production use requires HTTPS termination, managed secret storage, monitoring/alerting, tested backups, retention/privacy policies, dependency scanning, automated security tests, and load tests sized to the deployment.

See `docs/PRODUCTION_READINESS.md` for the deployment checklist.

### Change a password (API)

After signing in, use the returned bearer token:
```bash
curl -X POST http://localhost:8088/api/auth/password \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  -d '{"currentPassword":"YOUR_CURRENT_PASSWORD","newPassword":"YOUR_NEW_UNIQUE_PASSWORD_14_CHARS_OR_MORE"}'
```
This invalidates previously issued access tokens for that account. Sign in again after a successful password change.
