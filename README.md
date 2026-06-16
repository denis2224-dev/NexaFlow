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

## Current Custom APIs

The user service currently exposes these custom workspace APIs:

```text
POST   /api/workspaces
GET    /api/workspaces/my
PATCH  /api/workspaces/{organizationId}

POST   /api/workspaces/{organizationId}/invitations
GET    /api/workspaces/{organizationId}/invitations
DELETE /api/workspaces/{organizationId}/invitations/{invitationId}

POST   /api/workspaces/invitations/accept

GET    /api/workspaces/{organizationId}/members
PATCH  /api/workspaces/{organizationId}/members/{membershipId}/role
DELETE /api/workspaces/{organizationId}/members/{membershipId}
```

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

Required local secrets:

```bash
export JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET="$(openssl rand -base64 64)"
export APPLICATION_INTERNAL_API_TOKEN="$(openssl rand -base64 32)"
export JHIPSTER_CONTROL_CENTER_PASSWORD="$(openssl rand -base64 24)"
export GRAFANA_ADMIN_PASSWORD="$(openssl rand -base64 24)"
export SERVER_SSL_KEY_STORE_PASSWORD="$(openssl rand -base64 24)"
```

Use the same JWT value for the gateway and every service. Use the same internal API token for `project-service` and `notification-service`. For Docker Compose, put these keys in a local `.env` file copied from `.env.example`; `.env` is ignored by git.

Before running a service-level compose file, export the local `.env` into the shell:

```bash
set -a
. ./.env
set +a
```

TLS keystores are local secret material and are ignored by git. If you enable the `tls` Spring profile, generate a local keystore for each service that needs TLS:

```bash
keytool -genkey -alias selfsigned -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore src/main/resources/config/tls/keystore.p12 -validity 3650
```
