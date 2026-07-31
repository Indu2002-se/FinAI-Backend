

do not create any md files
You are a senior Spring Boot architect.

Follow the project's copilot-instructions.md exactly.

Project:
FinAI Backend

Current Status:

Completed:

- Spring Boot Foundation
- MySQL Configuration
- Docker
- Swagger
- Health Check API
- User Entity
- Role Entity
- RoleType Enum
- UserRepository
- RoleRepository

Do NOT regenerate existing code.

======================================================

TASK

Implement the complete Authentication module.

Generate ONLY production-ready code.

Implement the following:

1. DTOs

Request DTOs

- LoginRequest
- RegisterRequest

Response DTOs

- AuthenticationResponse
- UserResponse

======================================================

2. Service Layer

AuthenticationService interface

AuthenticationServiceImpl

Methods:

register()

login()

getCurrentUser()

======================================================

3. Security Configuration

Spring Security Configuration

SecurityFilterChain

PasswordEncoder

AuthenticationManager

Cors Configuration

Stateless Session

Disable CSRF

======================================================

4. JWT

Implement

JwtService

Methods

generateToken()

extractUsername()

isTokenValid()

extractExpiration()

extractClaims()

Generate secure JWT tokens.

======================================================

5. JWT Filter

JwtAuthenticationFilter

Validate JWT

Authenticate user

Populate SecurityContext

======================================================

6. UserDetails

CustomUserDetailsService

Load user by email.

======================================================

7. Controllers

AuthenticationController

Endpoints

POST /api/v1/auth/register

POST /api/v1/auth/login

GET /api/v1/auth/me

======================================================

8. Validation

Use Jakarta Validation.

Validate:

Email

Password

First Name

Last Name

======================================================

9. Exception Handling

AuthenticationException

GlobalExceptionHandler

Meaningful error responses.

======================================================

10. Password Encryption

Use BCryptPasswordEncoder.

Never store plain passwords.

======================================================

11. Swagger

Document all authentication endpoints.

======================================================

12. Architecture Rules

Controller

↓

Service

↓

Repository

Never place business logic inside controllers.

Never expose entities directly.

Always use DTOs.

======================================================

13. Do NOT implement

Google Authentication

Refresh Tokens

Forgot Password

Email Verification

Role Management APIs

Child APIs

Wizard APIs

Financial APIs

AI APIs

======================================================

Generate every class separately.

Generate production-ready enterprise code.

Follow SOLID principles.

Use constructor injection.

Use Lombok.

Follow Spring Boot best practices.

Do not modify existing User and Role entities.