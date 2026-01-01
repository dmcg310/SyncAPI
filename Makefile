.PHONY: setup start stop restart logs clean dev rebuild test

# Full Docker setup (database + app)
setup:
	@echo "Setting up complete environment..."
	@chmod +x scripts/setup.sh
	@./scripts/setup.sh

# Start all containers
start:
	@echo "Starting all containers..."
	@docker-compose up -d

# Stop all containers
stop:
	@echo "Stopping all containers..."
	@docker-compose down

# Restart all containers
restart:
	@echo "Restarting all containers..."
	@docker-compose restart

# View logs
logs:
	@docker-compose logs -f

# View app logs only
logs-app:
	@docker-compose logs -f app

# View database logs only
logs-db:
	@docker-compose logs -f postgres

# Clean everything
clean:
	@echo "Removing containers and volumes..."
	@docker-compose down -v

# Development mode (database in Docker, app locally)
dev:
	@echo "Starting database only..."
	@docker-compose up -d postgres pgadmin
	@echo "Database ready at localhost:5432"
	@echo "pgAdmin ready at http://localhost:5050"
	@echo "Run app locally: ./gradlew bootRun --args='--spring.profiles.active=dev'"

# Rebuild and restart app
rebuild:
	@echo "Rebuilding application..."
	@docker-compose up -d --build app

# Run tests
test:
	@./gradlew test

# Shell into app container
shell-app:
	@docker exec -it syncapi-app sh

# Shell into database container
shell-db:
	@docker exec -it syncapi-postgres psql -U syncapi_user -d syncapi_db

# Generate coverage report
coverage:
	@./gradlew test jacocoTestReport
	@echo "Coverage report: build/reports/jacoco/test/html/index.html"