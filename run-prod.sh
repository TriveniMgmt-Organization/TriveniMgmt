#!/bin/bash
# Backend Production Environment Runner

echo "🚀 Starting Backend Production Environment..."
echo "Using env.prod configuration"

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Copy production environment to .env
cp .env.prod .env

# Start backend services
docker-compose up --build
