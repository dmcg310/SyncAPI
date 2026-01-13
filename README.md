## Quick Reference

### Application Profiles

- **dev** - Local development (app on host, DB in Docker)
- **docker** - Full Docker (app + DB in containers)

### Pre-commit Hooks

```bash
# Will install pre-commit hooks
make hooks
```

### Common Tasks

```bash
# Full Docker setup
make setup

# Development mode (hot reload)
make dev
./gradlew bootRun --args='--spring.profiles.active=dev'

# Rebuild after code changes
make rebuild

# View logs
make logs-app
make logs-db

# Database shell
make shell-db

# Run tests
make test

# Coverage report
make coverage

# Lint code
make lint
```
