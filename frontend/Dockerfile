# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .
RUN npm run build

# Run stage
FROM node:20-alpine

WORKDIR /app

RUN addgroup -S riva && adduser -S riva -G riva

COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/package-lock.json ./package-lock.json

RUN npm ci --omit=dev --silent && chown -R riva:riva /app

USER riva

EXPOSE 3000

CMD ["npm", "start"]
