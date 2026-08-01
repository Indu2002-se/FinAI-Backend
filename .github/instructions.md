You are a senior Spring Boot software engineer.

Follow the project's copilot-instructions.md exactly.

Project:
FinAI Backend

Current Status:

Completed:

- Spring Boot Foundation
- MySQL
- Docker
- Swagger
- Health Check API
- Authentication Module (JWT)
- User & Role
- Security Configuration
- User Onboarding Wizard Module

Do NOT regenerate existing code.

======================================================

TASK

Implement the User Onboarding Wizard module only.

Generate ONLY:

1. WizardProfile entity

Fields:

- id
- user (OneToOne with User)
- monthlyIncome
- monthlyExpense
- savingsGoal
- financialKnowledgeLevel
- employmentStatus
- preferredCurrency
- createdAt
- updatedAt

2. DTOs

- WizardRequest
- WizardResponse

3. Repository

- WizardProfileRepository

Methods:

- findByUserId(Long userId)
- existsByUserId(Long userId)

4. Service

WizardService

Methods:

- saveWizard()
- getWizard()
- updateWizard()

5. Controller

Endpoints:

POST /api/v1/wizard

GET /api/v1/wizard

PUT /api/v1/wizard

6. Validation

Use Jakarta Validation.

7. Swagger documentation

======================================================

Rules

- Use DTOs only.
- Never expose entities.
- Controller → Service → Repository architecture.
- Associate wizard data with the authenticated user.
- Do not implement Income, Expense, Budget, Reports, Child Profile or AI features.
- Generate production-ready code only.
- Follow SOLID principles.