# Zest India IT - Product REST API Assignment

Production-style Java 17 + Spring Boot REST API implementing Product CRUD, Items, JWT authentication, refresh-token rotation, RBAC, validation, pagination, Swagger/OpenAPI, tests and Docker.

## Tech Stack

- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA / Hibernate
- MySQL 8.4
- Spring Security
- JWT + refresh token rotation
- Jakarta Validation
- JUnit 5 + Mockito + Spring Boot Test
- H2 for tests
- Swagger/OpenAPI
- Docker + Docker Compose
- Async audit logging

## Architecture

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
MySQL

Security:
HTTP Request → JWT Filter → SecurityContext → Controller authorization

Layers:
auth/       Authentication and refresh-token rotation
product/    Product + Item domain and CRUD
user/       User and role persistence
security/   JWT and Spring Security
common/     Standardized API errors
config/     OpenAPI, CORS, async executor, seed data
```

## Run locally

### 1. Start MySQL

Create a MySQL database:

```sql
CREATE DATABASE zest_product_db;
```

Default local connection:

```text
host=localhost
port=3306
database=zest_product_db
username=root
password=root
```

Or use Docker:

```bash
docker compose up --build
```

### 2. Run with Maven

```bash
mvn clean test
mvn spring-boot:run
```

API:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Health:

```text
http://localhost:8080/actuator/health
```

## Default users

The application seeds:

```text
ADMIN
email: admin@example.com
password: Admin@123

USER
email: user@example.com
password: User@123
```

Change these before production.

## Authentication

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "Vimal",
  "email": "vimal@example.com",
  "password": "Password@123"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin@123"
}
```

Copy `accessToken` and use:

```http
Authorization: Bearer <accessToken>
```

### Refresh

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "<refresh-token>"
}
```

Refresh tokens are stored only as SHA-256 hashes and are rotated on refresh. The previous token is revoked to prevent replay.

### Logout

```http
POST /api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "<refresh-token>"
}
```

## Product APIs

### Get products

```http
GET /api/v1/products?page=0&size=10&sort=createdOn,desc
Authorization: Bearer <access-token>
```

### Get one product

```http
GET /api/v1/products/1
Authorization: Bearer <access-token>
```

### Create

```http
POST /api/v1/products
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "productName": "Laptop",
  "items": [
    { "quantity": 10 },
    { "quantity": 5 }
  ]
}
```

### Update

```http
PUT /api/v1/products/1
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "productName": "Updated Laptop",
  "items": [
    { "quantity": 20 }
  ]
}
```

### Delete

Admin only:

```http
DELETE /api/v1/products/1
Authorization: Bearer <admin-access-token>
```

### Get items

```http
GET /api/v1/products/1/items
Authorization: Bearer <access-token>
```

## Standard error response

Example validation error:

```json
{
  "timestamp": "2026-09-02T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "validationErrors": {
    "productName": "must not be blank"
  }
}
```

## Database/indexing strategy

Indexes are included for:

- `app_user.email` - unique lookup for login/JWT
- `refresh_token.token_hash` - unique refresh token lookup
- `refresh_token.user_id` - user token lookup
- `product.created_on` - pagination/sorting
- `product.created_by` - audit/filtering
- `item.product_id` - product-item lookup

## HTTPS / CORS

CORS is restricted to common local frontend origins. In production, replace those origins with the actual frontend domain.

HTTPS should be terminated at a reverse proxy/load balancer (Nginx, cloud load balancer, API gateway, etc.) and HTTP should redirect to HTTPS there.

## Testing

Run:

```bash
mvn clean test
```

Included:

- Service unit tests with Mockito
- Controller tests with MockMvc
- Spring Boot context/integration test
- H2 test database

## Production recommendations

Before production:

1. Move JWT secret and database credentials to a secrets manager/environment.
2. Replace `ddl-auto=update` with Flyway/Liquibase migrations.
3. Configure HTTPS at the gateway/load balancer.
4. Restrict CORS to the real frontend domain.
5. Add rate limiting on login/refresh endpoints.
6. Add structured audit persistence instead of log-only audit events.
7. Add pagination limits and filtering/search if required.
8. Add observability with centralized logs, metrics and tracing.
