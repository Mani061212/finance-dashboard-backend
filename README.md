# Finance Dashboard Backend API

Role-based financial records management REST API built with Java 21, Spring Boot 3.x, PostgreSQL, JWT authentication, and Flyway migrations.

## Tech Stack

| Layer          | Technology                                  |
|----------------|---------------------------------------------|
| Language       | Java 21                                     |
| Framework      | Spring Boot 4.x                             |
| Security       | Spring Security + JWT (JJWT 0.12.6)         |
| Database       | PostgreSQL                                  |
| Migrations     | Flyway                                      |
| ORM            | Spring Data JPA / Hibernate                 |
| Validation     | Jakarta Bean Validation                     |
| Rate Limiting  | Bucket4j                                    |
| API Docs       | SpringDoc OpenAPI (Swagger UI)              |
| Tests          | JUnit 5 + Mockito + Spring MockMvc (H2)     |
| Build          | Maven 3.9                                   |


## Project Structure

```text
src/main/java/com/finance/dashboard
├── controller
├── service
├── repository
├── security
├── dto
├── entity
├── exception
└── config
```

## Architecture Overview

The project follows layered architecture:

- Controller layer → request handling, validation, response mapping  
- Service layer → business logic and access control  
- Repository layer → persistence and query abstraction  
- Security layer → JWT authentication + role enforcement  
- DTO layer → request/response separation from entities  

This keeps responsibilities isolated and improves maintainability.


## Roles and Permissions

| Role     | Transactions                               | Dashboard                      | Users       |
|----------|--------------------------------------------|--------------------------------|-------------|
| VIEWER   | None (use GET /dashboard/recent)           | GET /dashboard/recent only     | None        |
| ANALYST  | Create / Read / Update / Delete (own only) | Full: summary, trends, recent  | None        |
| ADMIN    | Full access (all users' data)              | Full: summary, trends, recent  | Full access |

> **Design decision:** The assignment states a Viewer "can only view dashboard data."  
> This is implemented as access to `GET /dashboard/recent` (the 10 most recent transactions).  
> Full transaction listing and analytics are restricted to ANALYST+ to give the role model meaningful depth.

## Quick Start

### Prerequisites
- Java 21  
- PostgreSQL  
- Maven 3.9  

### 1. Create the database
```sql
CREATE DATABASE financedb;
```

### 2. Set environment variables

The names below must match `application.yml` exactly:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="yourpassword"
$env:JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

### 3. Run
```bash
mvn spring-boot:run
```

- API: `http://localhost:8080`  
- Swagger UI: `http://localhost:8080/swagger-ui.html`  

### 4. Default credentials (seeded on first startup)

| Email | Password | Role |
|---|---|---|
| admin@finance.com | Admin@123 | ADMIN |
| analyst@finance.com | Analyst@123 | ANALYST |

## API Reference

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register — default role: VIEWER |
| POST | /api/auth/login | Login → access + refresh tokens |
| POST | /api/auth/refresh | New token pair (old refresh rotated) |
| POST | /api/auth/logout | Revoke refresh token |

### Transactions (ANALYST, ADMIN)

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/transactions | ANALYST, ADMIN |
| GET | /api/transactions | ANALYST (own), ADMIN (all) |
| GET | /api/transactions/{id} | ANALYST (own), ADMIN (all) |
| PUT | /api/transactions/{id} | Owner or ADMIN |
| DELETE | /api/transactions/{id} | Owner or ADMIN (soft delete) |

**Query params:** `type`, `category`, `search`, `from`, `to`, `page`, `size`, `sortBy`, `direction`

### Dashboard

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | /api/dashboard/summary | ANALYST, ADMIN | Income, expenses, net, categories |
| GET | /api/dashboard/trends/monthly | ANALYST, ADMIN | Monthly breakdown (?year=) |
| GET | /api/dashboard/trends/weekly | ANALYST, ADMIN | Weekly breakdown (?weeks=12) |
| GET | /api/dashboard/recent | **All roles** | 10 most recent transactions |

### Users (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/users | List all (paginated) |
| GET | /api/users/{id} | Get by ID |
| PATCH | /api/users/{id}/role | Change role |
| PATCH | /api/users/{id}/status | Activate / deactivate |
| DELETE | /api/users/{id} | Permanently delete |

## Design Decisions

**RBAC model:** VIEWERs access `GET /dashboard/recent` only.

**Dashboard scope:** Summary and trend aggregations are global and restricted to trusted roles.

**Soft delete vs hard delete:** Transactions preserve history; users are operational records.

**Admin safeguards:** Prevent deleting/deactivating the only ADMIN.

**DataSeeder:** Password hashes are generated using the active PasswordEncoder bean.

**JWT format:** Secret uses HMAC-safe length requirements.

**Rate limiting:** 100 requests/min per IP using Bucket4j.

**JPA Specification:** Single-query filtering with EntityGraph avoids N+1.

## Tradeoffs

**PostgreSQL over lightweight storage:**  
Chosen for relational consistency and aggregate query support.

**JWT over session-based authentication:**  
Stateless API design with simpler horizontal scaling.

**Soft delete for transactions, hard delete for users:**  
Balances auditability with operational simplicity.

**In-memory rate limiting:**  
Simple locally, Redis needed for distributed deployments.

**Role-scoped aggregation:**  
Analytics intentionally restricted to trusted roles.

**Controller tests over full integration tests:**  
Fast feedback with lower infrastructure overhead.

## Assumptions

- One transaction belongs to one user  
- ANALYST can only manage own transactions  
- ADMIN can access all records  
- Dashboard summary excludes soft-deleted transactions  
- Refresh token rotation is enabled  

## Validation Examples

- amount > 0  
- category not blank  
- type must be INCOME or EXPENSE  
- email must be valid format  
- password minimum length enforced  

Invalid input returns structured 400 responses.

## Error Response Example

```json
{
  "timestamp": "2026-04-05T20:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Amount must be greater than zero"
}
```