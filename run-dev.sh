#!/bin/bash
# Backend Development Environment Runner

echo "🚀 Starting Backend Development Environment..."
echo "Using env.dev configuration"

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Copy dev environment to .env
cp .env.dev .env

# Start backend services
docker-compose up --build
