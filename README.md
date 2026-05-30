# NexaFlow Platform

NexaFlow is a JHipster-based microservices SaaS platform.

This monorepo contains the gateway, frontend, and backend microservices that make up the platform.

## Applications

| App | Path | Type | Port |
| --- | --- | --- | --- |
| Gateway | `gateway/` | JHipster gateway, Angular frontend, JWT auth, Consul, PostgreSQL | `8080` |
| User Service | `user-service/` | JHipster microservice, JWT auth, Consul, PostgreSQL, Feign client | `8081` |
| Angular dev server | `gateway/` | Frontend development server | `4200` |
| Consul UI | Docker | Service discovery and configuration | `8500` |
| Gateway PostgreSQL | Docker | Gateway database | `5432` |
| User Service PostgreSQL | Docker | User-service database | `5434` |

## Repository Layout

```text
nexaflow-platform/
├── gateway/
├── user-service/
├── docs/
│   ├── architecture.md
│   └── roadmap.md
├── README.md
└── .gitignore
```

## Current APIs

The user service currently exposes:

- `POST /api/workspaces`
- `GET /api/workspaces/my`
- `POST /api/workspaces/{organizationId}/invitations`

`POST /api/workspaces` creates an organization and an `OWNER` membership for the authenticated user.

Planned:

- `POST /api/invitations/accept`

## Local Development

Start infrastructure for each app from its folder:

```bash
cd gateway
docker compose -f src/main/docker/services.yml up -d
```

```bash
cd user-service
docker compose -f src/main/docker/services.yml up -d
```

Run the gateway backend:

```bash
cd gateway
./mvnw
```

Run the gateway frontend:

```bash
cd gateway
./npmw run start
```

Run the user service:

```bash
cd user-service
./mvnw
```

## Git History

The monorepo history is intentionally organized so the first commits represent the fresh JHipster-generated applications before local configuration and custom product work.

Do not commit secrets. Use local environment variables or ignored `.env` files for machine-specific configuration.
