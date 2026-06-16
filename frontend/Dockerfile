FROM node:24-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
ARG APP_VERSION=unknown
ENV APP_VERSION=${APP_VERSION}
RUN npm run build

FROM nginx:1.29-alpine

ENV GATEWAY_UPSTREAM=http://gateway:8080

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/nexa-flow/ /usr/share/nginx/html/

EXPOSE 80
