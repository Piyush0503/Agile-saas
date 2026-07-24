.PHONY: dev-up dev-down db-reset db-seed backend frontend

dev-up:
	docker-compose up -d

dev-down:
	docker-compose down

db-reset:
	docker-compose down -v
	docker-compose up -d postgres
	@echo "Waiting for postgres to initialize..."
	@sleep 5
	@echo "Database reset complete."

db-seed:
	@echo "Running seed scripts..."
	docker exec -i agileflow-postgres psql -U postgres -d agileflow_dev < ./seed.sql

backend:
	cd agileflow-backend && mvn clean spring-boot:run -pl agileflow-api

frontend:
	cd agileflow-frontend && npm run dev
