# FinAI Backend - Mobile App Integration Guide

මෙම backend එක FinAI mobile app එක සමග integrate වෙන විදියට configure කර ඇත.

## 🚀 Quick Start

### Prerequisites
- **Java 21 JDK** (not just JRE) - [Download here](https://adoptium.net/) or install with:
  ```bash
  sudo apt install openjdk-21-jdk
  ```
- MySQL database running on localhost:3306
- Gradle (included via wrapper)

**Important:** Make sure you have the full JDK installed, not just the JRE. You can verify with:
```bash
javac -version  # Should show: javac 21.x.x
```

### Database Setup

1. MySQL database එකක් create කරන්න:
```sql
CREATE DATABASE finai_dev;
```

2. `src/main/resources/application-dev.properties` file එකේ database credentials update කරන්න:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### Run the Application

```bash
# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

Backend එක `http://localhost:8080` port එකේ run වෙයි.

## 📱 Mobile App Integration

### API Endpoints

Mobile app එක පහත endpoints use කරයි:

#### Authentication Endpoints

**1. Register User**
```
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "Password123",
  "firstName": "John",
  "lastName": "Doe"
}

Response (201 Created):
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "profileComplete": false,
      "enabled": true,
      "emailVerified": false
    }
  }
}
```

**2. Login User**
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "Password123"
}

Response (200 OK):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "profileComplete": false,
      "enabled": true,
      "emailVerified": false
    }
  }
}
```

**3. Logout User**
```
POST /api/auth/logout
Authorization: Bearer {token}

Response (200 OK):
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

**4. Get Current User**
```
GET /api/auth/me
Authorization: Bearer {token}

Response (200 OK):
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "profileComplete": false,
    "enabled": true,
    "emailVerified": false
  }
}
```

## 🔐 JWT Authentication

- JWT tokens හදාගන්නේ login/register වෙද්දී
- Token expiration: 24 hours (86400000 milliseconds)
- Authorization header එක use කරන්න: `Bearer {token}`

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    profile_image VARCHAR(255),
    provider VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    profile_complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 🔧 Configuration

### Server Port
Backend එක port `8080` එකේ run වෙයි mobile app එකේ base URL (`http://localhost:8080/api`) එක match වෙන විදියට.

### CORS Configuration
Frontend/mobile apps සඳහා CORS enabled කර ඇත. පහත origins allow කර ඇත:
- http://localhost:3000
- http://localhost:4200
- http://localhost:5173

Additional origins add කරන්න නම් `application-dev.properties` file එකේ update කරන්න:
```properties
cors.allowed-origins=http://localhost:3000,http://localhost:4200
```

### Security Configuration
- `/api/auth/**` endpoints public (authentication අවශ්‍ය නැහැ)
- අනිත් හැම endpoint එකකම JWT authentication අවශ්‍යයි
- CSRF disabled (REST API එකක් නිසා)
- Stateless session management

## 📚 API Documentation

Backend එක run වෙද්දී Swagger UI එක මගින් API documentation බලන්න පුළුවන්:

```
http://localhost:8080/swagger-ui.html
```

## 🧪 Testing with Postman

1. Backend එක start කරන්න
2. Postman import කරන්න පුළුවන් `POSTMAN_TESTING_GUIDE.txt` file එක බලලා
3. Register endpoint එක call කරලා user එකක් හදන්න
4. Login endpoint එක call කරලා token එකක් ගන්න
5. Token එක use කරලා protected endpoints access කරන්න

## 🐛 Common Issues

### Java Compiler Not Found
```
Error: Toolchain installation does not provide the required capabilities: [JAVA_COMPILER]
```
**Solution:** JDK (Java Development Kit) install කරන්න, JRE එක පමණක් නෙමෙයි:
```bash
sudo apt install openjdk-21-jdk
```

Verify කරන්න:
```bash
javac -version
```

### Database Connection Error
```
Error: Could not create connection to database server
```
**Solution:** MySQL service එක running ද බලන්න සහ credentials correct ද verify කරන්න.

### Port Already in Use
```
Error: Port 8080 is already in use
```
**Solution:** Port එක වෙනස් කරන්න `application.properties` file එකේ:
```properties
server.port=8081
```
Mobile app එකේ base URL එකත් update කරන්න ඕන.

### JWT Token Expired
```
Error: JWT token is expired
```
**Solution:** නැවත login වෙලා fresh token එකක් ගන්න.

## 📝 Changes Made for Mobile App Integration

1. ✅ API path prefix `/api/v1/auth` → `/api/auth` change කරලා
2. ✅ `profileComplete` field එක User entity එකට add කරලා
3. ✅ Logout endpoint එක implement කරලා
4. ✅ Server port `8082` → `8080` change කරලා
5. ✅ SecurityConfig එකේ `/api/auth/**` public කරලා
6. ✅ UserResponse DTO එකේ `profileComplete` field එක add කරලා
7. ✅ Database migration script එකක් add කරලා

## 🚀 Next Steps

1. Backend එක start කරන්න
2. Mobile app එක run කරන්න
3. Registration/Login test කරන්න
4. API calls monitor කරන්න Swagger UI එක හරහා

## 📞 Support

Issues තිබ්බොත් backend logs බලන්න:
```bash
tail -f logs/finai-backend.log
```

හෝ console output එක බලන්න debug mode එකේ.
