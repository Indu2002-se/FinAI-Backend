#!/bin/bash
# Start MySQL container for FinAI Backend

echo "Starting MySQL container for FinAI Backend..."
docker-compose up -d mysql

echo "Waiting for MySQL to be ready..."
sleep 10

echo "MySQL is ready!"
echo "Connection details:"
echo "  Host: localhost"
echo "  Port: 3306"
echo "  Database: finai_dev"
echo "  Username: finai_user"
echo "  Password: finai_password"
echo ""
echo "You can now run the Spring Boot application."
