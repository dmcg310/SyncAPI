# SyncAPI

A unified API testing and documentation platform built with Spring Boot and React.

## Overview

SyncAPI is a comprehensive platform for API testing, documentation, and collaboration. It provides teams with powerful tools to design, test, and document their APIs in a centralised workspace environment. It addresses the industry problem of **documentation drift** - where API documentation diverges from actual API behavior.

### Features
- Organise APIs into workspaces with team collaboration
- Group related API requests into folders
- Manage different environments (dev, staging, production) with variable substitution
- Test API endpoints with full HTTP method support
- Prevent concurrent modifications with user-level locking
- Auto-generate OpenAPI 3.0 specifications from your requests
- Secure JWT-based authentication system
- Multi-user workspace support with access control

## Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 4.0.1 with Java 21
- **Database**: PostgreSQL 15 with JPA/Hibernate
- **Security**: Spring Security with JWT authentication
- **API Documentation**: Auto-generated OpenAPI specifications
- **Code Quality**: Checkstyle, SpotBugs, and JaCoCo for testing coverage

### Frontend (React)
- **Framework**: React 19.2.0 with TypeScript
- **Routing**: React Router v7
- **Styling**: Tailwind CSS v4
- **Code Editor**: Monaco Editor for JSON editing
- **HTTP Client**: Axios for API communication
- **Build Tool**: Vite

## Quick Start

### Prerequisites

- **Docker** and **Docker Compose** (recommended)
- **Java 21** (for local development)
- **Node.js 18+** (for frontend development)
- **PostgreSQL 15** (if running locally without Docker)

### Using Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/syncapi.git
   cd syncapi
   ```

2. **Set up environment variables**
   ```bash
   cp .env.template .env
   # Edit .env and add your secrets
   ```

3. **Run the setup script**
   ```bash
   make setup
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - pgAdmin: http://localhost:5050

### Local Development

#### Backend Setup

1. **Configure database connection**
   ```bash
   cp src/main/resources/application-dev.properties.template src/main/resources/application-dev.properties
   # Edit the file with your database credentials
   ```

2. **Start the database**
   ```bash
   make dev
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

#### Frontend Setup

1. **Install dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start the development server**
   ```bash
   npm run dev
   ```

## Development

### Available Make Commands

```bash
# Setup and run everything with Docker
make setup

# Start all containers
make start

# Stop all containers
make stop

# Development mode (database in Docker, app locally)
make dev

# Rebuild and restart app container
make rebuild

# View logs
make logs
make logs-app
make logs-db

# Run tests
make test

# Generate coverage report
make coverage

# Run linting
make lint

# Run full verification
make check

# Database shell
make shell-db

# Clean everything
make clean

# Install git hooks
make hooks
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Code Quality

The project uses multiple tools to ensure code quality:

- **Checkstyle**: Code style verification
- **SpotBugs**: Static analysis for bug detection
- **JaCoCo**: Code coverage reporting

```bash
# Run all checks
make check

# Run linting only
make lint
```

## Environment Variables

Create a `.env` file based on `.env.template`:

```env
# Database
POSTGRES_DB=syncapi_db
POSTGRES_USER=syncapi_user
POSTGRES_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_jwt_secret_key_at_least_256_bits
JWT_EXPIRATION=86400000

# pgAdmin
PGADMIN_DEFAULT_EMAIL=admin@syncapi.com
PGADMIN_DEFAULT_PASSWORD=admin
```
