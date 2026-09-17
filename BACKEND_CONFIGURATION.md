# Restaurant ERP — Backend Configuration

## 1. Purpose

This document defines the backend configuration architecture for the Restaurant ERP SaaS application.

The backend is built with:

```text
Java
Spring Boot
Spring Security
Spring Data MongoDB
MongoDB
Maven
JWT
SSE
WebSocket
Swagger / OpenAPI
```

The application follows a modular monolith architecture with tenant-aware data isolation.

---

# 2. Backend Configuration Overview

```text
Spring Boot Application
        │
        ├── Application Configuration
        │
        ├── MongoDB Configuration
        │
        ├── UUID Configuration
        │
        ├── Security Configuration
        │
        ├── JWT Configuration
        │
        ├── WebSocket Configuration
        │
        ├── SSE Configuration
        │
        ├── Swagger Configuration
        │
        ├── Rate Limiting
        │
        ├── Scheduled Tasks
        │
        └── Application Initialization
```

Configuration classes should remain focused on infrastructure concerns rather than business logic.

---

# 3. Main Configuration Package

Recommended structure:

```text
config/
│
├── ApplicationInitializer.java
├── JwtConfig.java
├── MongoConfig.java
├── MongoUuidConfig.java
├── SecurityConfig.java
├── SwaggerConfig.java
├── WebSocketConfig.java
├── RateLimiter.java
└── RefreshTokenCleanupScheduler.java
```

Additional infrastructure configuration can be added here when required.

---

# 4. Application Entry Point

The main Spring Boot application class is responsible for starting the application.

Conceptually:

```java
@SpringBootApplication
@EnableMongoAuditing
public class RestaurantErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantErpApplication.class, args);
    }
}
```

The application entry point should remain small.

Do not place business logic inside the main application class.

---

# 5. Application Name

The application name is:

```text
restaurant-erp
```

Recommended:

```yaml
spring:
  application:
    name: restaurant-erp
```

The application name is useful for:

- Logging
- Monitoring
- Service identification
- Spring Boot metadata
- Deployment environments

---

# 6. Server Configuration

The backend currently runs on:

```text
Port: 8080
```

Configuration:

```yaml
server:
  port: 8080
```

Development API:

```text
http://localhost:8080
```

Production should use the deployed backend domain rather than localhost.

---

# 7. MongoDB Configuration

The application uses MongoDB as its primary database.

Development configuration:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/restaurant_db
```

Conceptually:

```text
Spring Boot
     │
     ▼
Spring Data MongoDB
     │
     ▼
MongoDB
     │
     ▼
restaurant_db
```

---

# 8. Database Architecture

The application uses:

```text
Shared Database
      +
Shared Collections
      +
tenantId
```

Example:

```text
restaurant_db
│
├── users
├── organizations
├── branches
├── roles
├── permissions
├── role_permissions
├── subscription_plans
├── menu_items
├── categories
├── tables
├── reservations
├── orders
├── inventory
├── purchases
├── expenses
└── ...
```

Tenant-owned documents should contain a tenant identifier.

---

# 9. MongoDB Connection

For development:

```text
mongodb://localhost:27017/restaurant_db
```

For production, the URI should be supplied through an environment variable.

Example:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

Example environment value:

```text
MONGODB_URI=mongodb://username:password@host:27017/restaurant_db
```

Production database credentials must never be committed to Git.

---

# 10. MongoDB Configuration Class

`MongoConfig` is responsible for MongoDB-specific application configuration.

Potential responsibilities:

- MongoDB client configuration
- MongoTemplate customization
- Mapping configuration
- Database-specific converters
- MongoDB infrastructure beans

It should not contain business-specific queries.

---

# 11. UUID Configuration

The application uses Java `UUID` identifiers.

MongoDB UUID representation must use the standard representation.

The project includes:

```text
MongoUuidConfig
```

The purpose is to prevent UUID codec/representation mismatches.

Conceptually:

```text
Java UUID
    │
    ▼
MongoDB UUIDRepresentation.STANDARD
    │
    ▼
MongoDB
```

This configuration is important for consistent UUID storage and retrieval.

---

# 12. UUID Strategy

The project uses UUID identifiers instead of MongoDB-generated ObjectIds for application entities.

Conceptually:

```text
BaseEntity
    │
    └── UUID id
```

Benefits:

- Application-level identifiers
- Easier distributed generation
- No dependency on MongoDB ObjectId semantics
- Consistent IDs across modules

---

# 13. MongoDB Auditing

The application uses auditing for timestamps.

The base entity contains concepts such as:

```text
createdAt
updatedAt
```

Spring Data Mongo auditing can populate these automatically.

Example:

```java
@CreatedDate
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;
```

The application should enable Mongo auditing.

---

# 14. Base Entity

The common persistence model is:

```text
BaseEntity
│
├── UUID id
├── boolean isActive
├── createdAt
└── updatedAt
```

This provides consistent behavior across business modules.

Typical entities extending the base entity include:

```text
Organization
Branch
Floor
RestaurantTable
MenuItem
Order
Expense
...
```

---

# 15. Soft Delete Configuration

The project uses:

```text
isActive
```

for soft deletion.

States:

```text
true  = active
false = inactive
```

Lifecycle:

```text
Create
  ↓
Active
  ↓
Deactivate
  ↓
Inactive
  ↓
Reactivate
```

Repositories and custom queries must take this field into account when appropriate.

---

# 16. Environment Profiles

The application should support environment-specific configuration.

Recommended:

```text
application.yml
application-dev.yml
application-test.yml
application-prod.yml
```

Conceptually:

```text
application.yml
       │
       ├── common configuration
       │
       ├── dev
       ├── test
       └── prod
```

Activate a profile using:

```yaml
spring:
  profiles:
    active: dev
```

Production profile should be supplied through deployment configuration rather than hardcoded.

---

# 17. Development Configuration

Development may use:

```text
MongoDB localhost
Port 8080
Frontend localhost:5173
Swagger enabled
Debug logging as required
```

Example:

```yaml
server:
  port: 8080

spring:
  application:
    name: restaurant-erp

  data:
    mongodb:
      uri: mongodb://localhost:27017/restaurant_db
```

---

# 18. Production Configuration

Production should use environment variables.

Example:

```yaml
server:
  port: ${PORT:8080}

spring:
  application:
    name: restaurant-erp

  data:
    mongodb:
      uri: ${MONGODB_URI}
```

Security configuration should also come from environment variables.

---

# 19. JWT Configuration

JWT settings belong in:

```text
JwtConfig
```

Typical values:

```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION}
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION}
```

Responsibilities:

```text
JwtConfig
   │
   ├── Secret
   ├── Access token lifetime
   └── Refresh token lifetime
```

JWT secrets must never be hardcoded into source code.

---

# 20. Security Configuration

`SecurityConfig` configures:

- Authentication
- Authorization
- JWT filter
- Public endpoints
- Protected endpoints
- Stateless sessions
- Password encoder
- CORS
- Method-level authorization

Conceptually:

```text
SecurityConfig
      │
      ├── JWT
      ├── Authentication
      ├── Authorization
      ├── CORS
      └── Session Policy
```

---

# 21. Stateless Security

The backend uses JWT-based stateless authentication.

Conceptually:

```text
Request
  │
  ▼
JWT
  │
  ▼
Authentication
  │
  ▼
SecurityContext
```

The application should not depend on an HTTP server session for normal API authentication.

Recommended:

```text
SessionCreationPolicy.STATELESS
```

---

# 22. JWT Filter Registration

The JWT authentication filter should execute before Spring's standard username/password authentication filter.

Conceptually:

```text
Security Filter Chain
        │
        ▼
JwtAuthFilter
        │
        ▼
Authentication
        │
        ▼
Controller
```

The filter should only perform authentication-related processing.

---

# 23. Password Encoder

A BCrypt encoder should be registered as a bean.

Conceptually:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Services should inject:

```text
PasswordEncoder
```

instead of creating encoders repeatedly.

---

# 24. CORS Configuration

Development frontend:

```text
http://localhost:5173
```

The backend should allow the configured frontend origin.

Example conceptual configuration:

```text
Allowed Origins:
    http://localhost:5173
```

Production should allow only trusted frontend domains.

Avoid unrestricted production CORS.

---

# 25. CSRF Configuration

The CSRF strategy depends on how authentication tokens are transported.

For stateless bearer-token APIs:

```text
Authorization: Bearer <token>
```

the CSRF requirements differ from cookie-based authentication.

If authentication uses cookies, appropriate CSRF protection must be implemented.

The frontend and backend token strategy must remain consistent.

---

# 26. WebSocket Configuration

The application supports WebSocket for realtime features.

Configuration belongs in:

```text
WebSocketConfig
```

Conceptually:

```text
React
  │
  ▼
WebSocket
  │
  ▼
Spring Boot
  │
  ▼
Application Events
```

Potential use cases:

```text
Kitchen
Orders
Notifications
POS events
Operational status
```

---

# 27. Server-Sent Events Configuration

SSE is used where server-to-client event streaming is appropriate.

Conceptually:

```text
Spring Boot
    │
    ▼
SseEmitter
    │
    ▼
React EventSource
    │
    ▼
UI State
```

Example endpoint:

```text
GET /api/branch/stream
```

SSE event services should manage active emitters and publish module-specific events.

---

# 28. SSE vs WebSocket

Use SSE when:

```text
Server → Client
```

updates are sufficient.

Examples:

```text
CRUD update notifications
Permission changes
Branch changes
Table status updates
```

Use WebSocket when:

```text
Client ↔ Server
```

interactive realtime communication is required.

Examples:

```text
Kitchen
Live POS
Operational collaboration
Interactive notifications
```

The two technologies can coexist.

---

# 29. Swagger / OpenAPI

API documentation is configured through:

```text
SwaggerConfig
```

Swagger should document:

```text
Authentication
Organizations
Branches
Menu
Tables
Reservations
Orders
Inventory
Purchasing
Expenses
Reports
```

Authentication endpoints should clearly document:

```text
Request body
Response body
HTTP status codes
Authentication requirements
```

---

# 30. API Documentation Security

Swagger/OpenAPI should not expose secrets.

Do not document:

```text
JWT secrets
Database credentials
Internal keys
Passwords
Private infrastructure details
```

Production Swagger access can be restricted depending on deployment requirements.

---

# 31. Application Initializer

The application contains:

```text
ApplicationInitializer
```

This component can initialize required platform data.

Potential initialization:

```text
Default roles
Default permissions
Default subscription plans
Required system configuration
```

Initialization must be idempotent.

That means running the application multiple times should not create duplicate default records.

---

# 32. Seed Data Principle

System seed data should be distinguishable from tenant-created data.

Example:

```text
SYSTEM
  │
  ├── Roles
  ├── Permissions
  └── Subscription Plans
```

versus:

```text
TENANT
  │
  ├── Employees
  ├── Menu
  ├── Tables
  └── Orders
```

Initialization should never accidentally overwrite customer data.

---

# 33. Subscription Initialization

Default subscription plans may be initialized during application startup.

Example:

```text
Free
Starter
Professional
Enterprise
```

The actual plan names and limits are business decisions.

Initializer responsibilities:

```text
Check plan exists
     │
     ├── Yes → Do nothing
     │
     └── No  → Create
```

---

# 34. Role Initialization

Default roles may be initialized:

```text
SUPER_ADMIN
RESTAURANT_OWNER
BRANCH_MANAGER
CASHIER
WAITER
KITCHEN_STAFF
INVENTORY_MANAGER
CUSTOMER
```

The initializer should avoid duplicating roles.

---

# 35. Permission Initialization

Permissions can follow:

```text
MODULE_ACTION
```

Examples:

```text
ORGANIZATION_CREATE
ORGANIZATION_VIEW
ORGANIZATION_UPDATE
ORGANIZATION_DELETE

BRANCH_CREATE
BRANCH_VIEW
BRANCH_UPDATE
BRANCH_DELETE

ORDER_VIEW
ORDER_CREATE
ORDER_UPDATE
```

The permission initializer should create missing system permissions without destroying existing assignments.

---

# 36. Rate Limiter

Authentication-sensitive endpoints should have rate limiting.

Potential targets:

```text
/api/auth/login
/api/auth/register
/api/auth/refresh
/api/auth/forgot-password
```

Conceptually:

```text
Request
   │
   ▼
RateLimiter
   │
 ┌─┴─────────┐
 │           │
Allowed    Limited
 │           │
 ▼           ▼
Auth       Reject
```

Rate limits should be configurable.

---

# 37. Refresh Token Cleanup Scheduler

The backend contains:

```text
RefreshTokenCleanupScheduler
```

Its responsibility is to periodically clean expired refresh tokens.

```text
Scheduler
   │
   ▼
Find Expired Refresh Tokens
   │
   ▼
Delete / Revoke
```

This keeps the authentication collection manageable.

---

# 38. Scheduler Configuration

Scheduled tasks should have configurable intervals.

Conceptually:

```yaml
app:
  scheduler:
    refresh-token-cleanup: ...
```

Avoid hardcoding operational intervals when they may differ between environments.

---

# 39. Logging Configuration

Logging should provide enough information for debugging without exposing sensitive data.

Useful categories:

```text
Authentication
Authorization
Database
HTTP
Application
Scheduled Tasks
Realtime
```

Never log:

```text
Passwords
JWT secrets
Full access tokens
Full refresh tokens
Database credentials
Payment secrets
```

---

# 40. Error Handling Configuration

The application should provide centralized exception handling.

Conceptually:

```text
Controller
    │
    ▼
Exception
    │
    ▼
Global Exception Handler
    │
    ▼
ApiResponse
```

Errors should be categorized appropriately:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
422 Business/Validation Error where used
500 Internal Server Error
```

Internal implementation details should not be exposed to clients.

---

# 41. Validation Configuration

API input validation should be enabled using Jakarta Validation where appropriate.

Typical annotations:

```text
@NotNull
@NotBlank
@Email
@Size
@Min
@Max
@Positive
```

Validation belongs at the API boundary.

Business rules belong in services.

---

# 42. MongoDB Index Configuration

Frequently queried fields should have appropriate indexes.

Important tenant-related indexes may include:

```text
tenantId
tenantId + isActive
tenantId + name
tenantId + code
tenantId + createdAt
```

Authentication indexes:

```text
email
tenantId + email
refreshToken
expiryDate
```

Actual indexes should be based on real query patterns.

---

# 43. Search Configuration

Dynamic search should use:

```text
SearchCriteria
+
CustomRepository
+
MongoTemplate
```

Flow:

```text
Controller
   ↓
Handler
   ↓
Service
   ↓
Custom Repository
   ↓
MongoTemplate
   ↓
MongoDB
```

This is preferable to putting complex dynamic queries directly in controllers or services.

---

# 44. Pagination

Large result sets should use pagination.

Typical parameters:

```text
page
size
sort
direction
```

Example:

```text
GET /api/branch/search?page=0&size=20
```

Avoid returning unbounded collections from production endpoints.

---

# 45. API Base Path

The application can use:

```text
/api
```

as the common REST prefix.

Examples:

```text
/api/auth
/api/organization
/api/branch
/api/menu
/api/table
/api/order
/api/inventory
```

The exact endpoint naming should remain consistent across modules.

---

# 46. File Upload Configuration

Modules such as:

```text
Organization
Expense
Order
```

may require file attachments.

File upload configuration should define:

```text
Maximum file size
Allowed MIME types
Storage strategy
Filename strategy
Security validation
```

Uploaded files should not automatically be trusted based only on filename or extension.

---

# 47. Storage Architecture

The application can separate file metadata from file storage.

Conceptually:

```text
Business Entity
      │
      ▼
Attachment Metadata
      │
      ▼
Object/File Storage
```

Production may use object storage instead of storing large binary files directly in MongoDB.

---

# 48. External Service Configuration

Future integrations may require:

```text
Email
SMS
Payment Gateway
Cloud Storage
Maps
Notifications
```

Credentials should follow:

```text
Environment Variables
        or
Secure Secret Manager
```

Never hardcode third-party credentials.

---

# 49. Transaction Strategy

MongoDB transaction support should be used only where the workflow requires atomic multi-document operations and the deployment supports it.

Examples may include:

```text
Order payment + order state
Inventory movement + stock update
Purchase receiving + inventory update
Accounting entry workflows
```

Not every CRUD operation requires a transaction.

---

# 50. Service-to-Service Calls

Inside the modular monolith, modules should communicate through service interfaces and well-defined application boundaries.

Prefer:

```text
OrderService
   ↓
InventoryService
```

over directly manipulating another module's repositories.

Avoid:

```text
OrderService
   ↓
InventoryRepository
```

unless the architecture explicitly requires that repository-level integration.

---

# 51. Dependency Direction

Preferred direction:

```text
Controller
    ↓
Handler
    ↓
Service
    ↓
Repository
    ↓
MongoDB
```

Supporting:

```text
Service
   ↓
Transformer
   ↓
Model / Entity
```

Avoid reverse dependencies such as:

```text
Repository → Controller
Domain → Service
Domain → HTTP
```

---

# 52. Configuration and Secrets Rule

Configuration may be committed.

Secrets must not.

Safe examples:

```text
server.port
spring.application.name
API paths
feature flags
non-sensitive defaults
```

Unsafe examples:

```text
JWT_SECRET
MONGODB_PASSWORD
SMTP_PASSWORD
PAYMENT_SECRET
OAUTH_SECRET
```

Use:

```text
.env
Environment Variables
Secret Manager
Deployment Secrets
```

and ensure secret files are excluded from Git.

---

# 53. Git Configuration

The repository should exclude sensitive/local files.

Typical `.gitignore` entries:

```text
.env
.env.*
target/
.idea/
*.iml
node_modules/
logs/
```

Do not commit production credentials.

---

# 54. Development Tools

The backend development workflow uses:

```text
IntelliJ IDEA
Maven
Java
Spring Boot
MongoDB
MongoDB GUI tools
Bruno
Git
GitHub
```

Bruno is used for API testing.

---

# 55. Local Development Environment

Expected local setup:

```text
Windows
Java
Maven
MongoDB
IntelliJ IDEA
Node.js
React/Vite frontend
```

Backend:

```text
localhost:8080
```

Frontend development server:

```text
localhost:5173
```

MongoDB:

```text
localhost:27017
```

Database:

```text
restaurant_db
```

---

# 56. Backend Startup Sequence

Conceptually:

```text
Start Application
       │
       ▼
Load Environment
       │
       ▼
Load Spring Configuration
       │
       ▼
Connect MongoDB
       │
       ▼
Configure UUID
       │
       ▼
Configure Security
       │
       ▼
Configure WebSocket / SSE
       │
       ▼
Initialize System Data
       │
       ▼
Start Scheduled Tasks
       │
       ▼
Start HTTP Server :8080
```

---

# 57. Production Startup Sequence

```text
Deployment
    │
    ▼
Environment Variables
    │
    ▼
Spring Boot Startup
    │
    ▼
MongoDB Connection
    │
    ▼
Security Initialization
    │
    ▼
System Data Initialization
    │
    ▼
Realtime Infrastructure
    │
    ▼
Application Ready
```

---

# 58. Health and Monitoring

Production should expose appropriate health information.

Useful monitoring areas:

```text
Application availability
MongoDB connection
Memory
CPU
HTTP errors
Authentication failures
Realtime connections
Scheduled task failures
Database latency
```

Spring Boot Actuator can be introduced when operational monitoring is required.

Sensitive actuator endpoints must not be publicly exposed.

---

# 59. Production Deployment Architecture

Conceptually:

```text
                 Internet
                    │
                    ▼
             Reverse Proxy
                    │
                    ▼
             Spring Boot API
                    │
             ┌──────┴───────┐
             │              │
             ▼              ▼
         MongoDB       External Services
```

Frontend:

```text
Browser
   │
   ▼
React Application
   │
   ▼
Spring Boot API
```

---

# 60. Configuration Checklist

Before development:

```text
[ ] Java configured
[ ] Maven configured
[ ] MongoDB running
[ ] restaurant_db available
[ ] application configuration loaded
[ ] UUID STANDARD configured
[ ] Mongo auditing enabled
[ ] JWT secret configured
[ ] JWT expiration configured
[ ] SecurityConfig configured
[ ] JWT filter registered
[ ] CORS configured
[ ] WebSocket configured
[ ] SSE endpoints configured
[ ] Swagger configured
[ ] Rate limiter configured
[ ] Refresh-token scheduler configured
[ ] Application initializer configured
```

---

# 61. Production Checklist

Before production:

```text
[ ] Production MongoDB URI configured
[ ] Database credentials stored securely
[ ] JWT secret stored securely
[ ] Access-token lifetime configured
[ ] Refresh-token lifetime configured
[ ] CORS restricted
[ ] Swagger access reviewed
[ ] Debug logging disabled/reduced
[ ] Rate limiting enabled
[ ] HTTPS enabled
[ ] Secure token transport configured
[ ] MongoDB indexes reviewed
[ ] Health monitoring configured
[ ] Backups configured
[ ] Error responses sanitized
[ ] Secrets excluded from Git
```

---

# 62. Final Backend Configuration Architecture

```text
                        Spring Boot
                            │
          ┌─────────────────┼──────────────────┐
          │                 │                  │
          ▼                 ▼                  ▼
     Application         Security           Database
     Configuration       Configuration      Configuration
          │                 │                  │
          │                 ├── JWT            ├── MongoDB
          │                 ├── BCrypt         ├── UUID
          │                 ├── CORS           └── Auditing
          │                 ├── RBAC
          │                 └── Rate Limit
          │
          ├─────────────────────────────────────┐
          │                                     │
          ▼                                     ▼
     Realtime                              Infrastructure
          │                                     │
          ├── SSE                               ├── Initializer
          └── WebSocket                         ├── Scheduler
                                                └── Swagger
          │
          └──────────────────┬──────────────────┘
                             ▼
                    Restaurant ERP Modules
                             │
                             ▼
                          MongoDB
```

---

# 63. Core Configuration Principle

The backend configuration layer should configure infrastructure, not business workflows.

The architectural separation is:

```text
CONFIG
  ↓
Infrastructure

CONTROLLER
  ↓
HTTP

HANDLER
  ↓
Application orchestration

SERVICE
  ↓
Business logic

REPOSITORY
  ↓
Database access

MONGODB
  ↓
Persistence
```

The configuration layer provides the secure and consistent runtime environment in which all Restaurant ERP modules operate.
