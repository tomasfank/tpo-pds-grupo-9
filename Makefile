.PHONY: build up down restart logs ps clean rebuild

build:
	docker compose build

up:
	docker compose up -d

down:
	docker compose down

restart:
	docker compose restart

logs:
	docker compose logs -f

logs-back:
	docker compose logs -f backend

logs-front:
	docker compose logs -f frontend

logs-mongo:
	docker compose logs -f mongo

ps:
	docker compose ps

rebuild:
	docker compose down
	docker compose build --no-cache
	docker compose up -d

clean:
	docker compose down -v --remove-orphans
	docker image rm riva-backend riva-frontend 2>/dev/null || true

shell-back:
	docker compose exec backend sh

shell-front:
	docker compose exec frontend sh

shell-mongo:
	docker compose exec mongo mongosh -u $${MONGO_USER:-riva} -p $${MONGO_PASSWORD:-rivapass} --authenticationDatabase admin
