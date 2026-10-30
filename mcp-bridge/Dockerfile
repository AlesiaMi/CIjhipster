FROM node:22-alpine

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci --include=dev --no-audit --no-fund

COPY . .

ENV CIJHIPSTER_MCP_TRANSPORT=http

EXPOSE 8765

USER node

CMD ["./node_modules/.bin/tsx", "http-server.ts"]
