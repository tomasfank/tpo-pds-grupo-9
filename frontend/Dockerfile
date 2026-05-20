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

COPY --from=builder /app/build ./build
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json

USER riva

EXPOSE 3000

CMD ["npm", "start"]
