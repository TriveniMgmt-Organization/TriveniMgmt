#!/bin/bash
# Local Development Runner
# This script starts the Docker database and runs the app with Gradle

echo "🚀 Starting Local Development Environment..."
echo "Starting Docker database..."

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Start only the database container
docker-compose up -d db

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 10

# Check if database is ready
until docker-compose exec -T db pg_isready -U proha -d triveni_mgmt_db; do
  echo "⏳ Database is not ready yet, waiting..."
  sleep 2
done

echo "✅ Database is ready!"
echo "🚀 Starting application with Gradle..."

# Run the application with local profile
./gradlew bootRun --args='--spring.profiles.active=local'

