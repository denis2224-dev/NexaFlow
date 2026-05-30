# NexaFlow Roadmap

## Phase 1: Platform Foundation

- Maintain the gateway and user service in a single monorepo.
- Keep JHipster-generated baseline commits clean.
- Use Consul for local service discovery.
- Use separate PostgreSQL databases per service.

## Phase 2: Workspace Management

- Create workspaces through `POST /api/workspaces`.
- Create an owner membership for the authenticated user.
- List the authenticated user's workspaces through `GET /api/workspaces/my`.
- Invite users to a workspace through `POST /api/workspaces/{organizationId}/invitations`.

## Phase 3: Invitation Lifecycle

- Add `POST /api/invitations/accept`.
- Validate invitation tokens.
- Convert accepted invitations into active memberships.
- Expire stale invitations.
- Add tests for owner/admin/member permission boundaries.

## Phase 4: SaaS Readiness

- Add organization settings.
- Add membership role management.
- Add audit logging for workspace administration.
- Add billing and subscription boundaries.
- Harden cross-service identity propagation.

## Phase 5: Delivery

- Add root-level Docker Compose orchestration for the full platform.
- Add CI checks for gateway and user-service builds.
- Add deployment documentation.
- Add production secret-management guidance.
