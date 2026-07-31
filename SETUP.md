# FinAI Backend Setup Guide

## Prerequisites

- **Java 21** - [Download](https://adoptium.net/)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop/)
- **Git** - [Download](https://git-scm.com/)

## Quick Start

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd backend
```

### Step 2: Start MySQL Database

#### Windows
```bash
cd scripts
start-mysql.bat
```

#### Linux/Mac
```bash
chmod +x scripts/start-mysql.sh
./scripts/start-mysql.sh
```

### Step 3: Verify Database Connection

The MySQL container should now be running with these details:
- **Host**: localhost
- **Port**: 3306
- **Database**: finai_dev
- **Username**: finai_user
- **Password**: finai_password

Test connection:
```bash
mysql -h localhost -P 3306 -u finai_user -pfinai_password finai_dev
```

### Step 4: Build the Application
```bash
# Windows
gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

### Step 5: Run the Application
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

### Step 6: Verify Application is Running

Open your browser and navigate to:
- **Health Check**: http://localhost:8080/api/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## Configuration

### Environment Variables

Copy the example environment file:
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```properties
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:mysql://localhost:3306/finai_dev
DATABASE_USERNAME=finai_user
DATABASE_PASSWORD=finai_password
```

### Spring Profiles

- **dev** - Development (default)
- **prod** - Production
- **test** - Testing (uses H2 in-memory database)

Change profile:
```bash
# Windows
gradlew.bat bootRun --args="--spring.profiles.active=prod"

# Linux/Mac
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Testing

### Run All Tests
```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

Tests use H2 in-memory database, so MySQL doesn't need to be running.

### View Test Reports
```
build/reports/tests/test/index.html
```

## Docker Commands

### Start MySQL Only
```bash
docker-compose up -d mysql
```

### Stop MySQL
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f mysql
```

### Reset Database
```bash
docker-compose down -v
docker-compose up -d mysql
```

## Troubleshooting

### Port 3306 Already in Use
If you have MySQL installed locally:
```bash
# Windows
net stop MySQL80

# Linux/Mac
sudo systemctl stop mysql
```

Or change the port in `docker-compose.yml`:
```yaml
ports:
  - "3307:3306"
```

### Connection Refused
Wait 10-15 seconds for MySQL to fully start:
```bash
docker-compose logs -f mysql
```

Look for: `ready for connections`

### Permission Denied (Linux/Mac)
```bash
chmod +x gradlew
chmod +x scripts/*.sh
```

### Cannot Connect to Docker
Ensure Docker Desktop is running:
```bash
docker ps
```

## Development Workflow

1. Start MySQL: `scripts/start-mysql.bat`
2. Run application: `gradlew bootRun`
3. Make changes
4. Application auto-reloads (Spring DevTools)
5. Test endpoints: `http://localhost:8080/swagger-ui.html`
6. Stop MySQL: `scripts/stop-mysql.bat`

## Next Steps

- [ ] Create entities in `src/main/java/com/finai/backend/entity/`
- [ ] Create repositories in `src/main/java/com/finai/backend/repository/`
- [ ] Create services in `src/main/java/com/finai/backend/service/`
- [ ] Create controllers in `src/main/java/com/finai/backend/controller/`
- [ ] Add business logic
- [ ] Write tests

## Support

For issues or questions, contact the development team.
