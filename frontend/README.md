# NexaFlow Frontend

Standalone Angular frontend for NexaFlow.

## Development

Run the gateway backend on port `8080`, then start the Angular dev server:

```bash
npm install
npm run start
```

The dev server proxies `/api`, `/management`, `/v3/api-docs`, and `/services` to the gateway. Override the target when needed:

```bash
BACKEND_HOST=localhost BACKEND_PORT=8080 npm run start
```

## Production Build

```bash
npm run build
```

The compiled app is written to `dist/nexa-flow/`.

## Docker

```bash
docker build -t nexaflow-frontend .
docker run --rm -p 4200:80 -e GATEWAY_UPSTREAM=http://host.docker.internal:8080 nexaflow-frontend
```

In Compose, set `GATEWAY_UPSTREAM=http://gateway:8080` and put the frontend and gateway services on the same network.
