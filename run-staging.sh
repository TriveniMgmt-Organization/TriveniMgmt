#!/bin/bash
# Backend Staging Environment Runner

echo "🚀 Starting Backend Staging Environment..."
echo "Using env.staging configuration"

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Copy staging environment to .env
cp .env.staging .env

# Start backend services
docker-compose up --build
