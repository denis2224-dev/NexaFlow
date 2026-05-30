# NexaFlow Architecture

NexaFlow uses a JHipster microservices architecture.

## Components

### Gateway

The gateway app is located in `gateway/`.

Responsibilities:

- Serve the Angular frontend
- Authenticate users with JWT
- Register with Consul
- Route `/services/{serviceId}/**` traffic to registered microservices
- Own gateway-specific user/authentication tables

### User Service

The user service is located in `user-service/`.

Responsibilities:

- Manage workspaces through organizations
- Store memberships for users inside organizations
- Store invitations for future onboarding flows
- Register with Consul so the gateway can route requests to it

## Runtime Ports

| Component | Port |
| --- | --- |
| Angular frontend | `4200` |
| Gateway | `8080` |
| User Service | `8081` |
| Consul UI | `8500` |
| Gateway PostgreSQL | `5432` |
| User Service PostgreSQL | `5434` |

## Request Flow

1. A browser talks to the gateway.
2. The gateway validates JWT authentication.
3. Gateway routes microservice calls through Spring Cloud Gateway.
4. Consul provides service discovery.
5. User-service handles workspace, membership, and invitation requests.
6. Each application owns its own PostgreSQL database.

## Data Ownership

The gateway owns authentication and built-in JHipster user data.

The user service owns:

- `organization`
- `membership`
- `invitation`

Cross-service user identity is currently represented by user identifiers and login/email fields copied from the authenticated principal. This can later be hardened with a shared identity contract or gateway-propagated user claims.

## API Surface

Current custom user-service APIs:

- `POST /api/workspaces`
- `GET /api/workspaces/my`
- `POST /api/workspaces/{organizationId}/invitations`

Planned:

- `POST /api/invitations/accept`

## Monorepo Conventions

- Keep each JHipster app self-contained.
- Preserve app-local Maven, npm, Docker, Liquibase, and JHipster files.
- Put platform-wide docs and orchestration at the repository root.
- Keep generated JHipster changes separate from custom business features when possible.
