#!/bin/bash

echo "Setting up SyncAPI development environment..."

if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Please start Docker Desktop and try again."
    exit 1
fi

echo "Building and starting Docker containers..."
docker compose up -d --build

echo "Waiting for application to start..."
echo "(This may take a minute on first run...)"

wait_for_app() {
    local max_attempts=60
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:8080 > /dev/null 2>&1; then
            return 0
        fi

        if ! docker ps | grep -q syncapi-app; then
            echo "Application container stopped unexpectedly"
            echo "View logs with: docker compose logs app"
            return 1
        fi

        sleep 2

        attempt=$((attempt + 1))

        echo -n "."
    done

    echo ""
    echo "Application didn't respond in time, but may still be starting"
    echo "Check logs with: docker compose logs -f app"
    return 1
}

if wait_for_app; then
    echo ""
    echo "Application is ready!"
else
    echo ""
    echo "Application is still starting up..."
fi

echo "Setup complete!"
