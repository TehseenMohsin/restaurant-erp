# Restaurant ERP — Architecture

## 1. Overview

**Restaurant ERP** is a multi-tenant SaaS restaurant management, POS, and ERP platform designed for restaurants, cafés, fast-food businesses, and restaurant chains.

The system is built around:

- **Backend:** Java + Spring Boot
- **Database:** MongoDB
- **Authentication:** JWT access/refresh tokens
- **Authorization:** RBAC + permissions
- **Frontend:** React + Vite + Tailwind CSS
- **Realtime communication:** Server-Sent Events (SSE) and WebSocket
- **API testing:** Bruno
- **Build tool:** Maven
- **Architecture style:** Modular layered architecture
- **Tenant strategy:** Shared database + shared collections using `tenantId`

The architecture is intended to support multiple restaurants, branches, employees, customers, menus, orders, inventory, purchasing, reservations, accounting, and reporting from one SaaS platform.

---

# 2. High-Level Architecture

```text
                         ┌─────────────────────────┐
                         │        React App        │
                         │   Vite + Tailwind CSS   │
                         └────────────┬────────────┘
                                      │
                         HTTP / REST / SSE / WS
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │      Spring Boot API    │
                         │                         │
                         │ Controllers             │
                         │ Handlers                 │
                         │ Services                 │
                         │ Transformers             │
                         │ Repositories             │
                         └────────────┬────────────┘
                                      │
                       ┌──────────────┴──────────────┐
                       │                             │
                       ▼                             ▼
              ┌────────────────┐            ┌────────────────┐
              │ Spring Security│            │ Realtime Layer │
              │ JWT + RBAC     │            │ SSE / WebSocket│
              └────────────────┘            └────────────────┘
                       │
                       ▼
              ┌─────────────────────┐
              │      MongoDB        │
              │ Shared DB / Shared  │
              │ Collections         │
              │ tenantId isolation  │
              └─────────────────────┘
```

---

# 3. Backend Package Architecture

The backend follows a **module-first architecture**.

```text
com.devmasters.restaurant_erp
│
├── auth/
├── organization/
├── branch/
│
├── user/
├── employee/
├── customer/
├── vendor/
│
├── role/
├── permission/
├── rolepermission/
│
├── subscription/
│
├── menu/
├── recipe/
├── inventory/
├── purchase/
│
├── floor/
├── table/
├── reservation/
│
├── order/
├── kitchen/
├── delivery/
│
├── tax/
├── expense/
├── accounting/
│
├── attendance/
├── payroll/
│
├── loyalty/
├── notification/
├── audit/
├── report/
│
├── payment/
├── settings/
│
└── common/
```

Each business module should remain as independent as reasonably possible.

---

# 4. Module Internal Structure

The standard structure for a module is:

```text
module/
│
├── controller/
│   └── ModuleController.java
│
├── domain/
│   └── Module.java
│
├── model/
│   └── ModuleModel.java
│
├── repository/
│   ├── ModuleRepository.java
│   └── custom/
│       ├── ModuleCustomRepository.java
│       └── ModuleCustomRepositoryImpl.java
│
├── service/
│   └── ModuleService.java
│
├── handler/
│   └── ModuleHandler.java
│
└── transformer/
    └── ModuleTransformer.java
```

Not every module requires every package, but the project follows this structure when CRUD/search/business logic requires it.

---

# 5. Responsibility of Each Layer

## 5.1 Domain

The `domain` package contains MongoDB persistence entities.

Example:

```text
domain/
└── Organization.java
```

Responsibilities:

- Database representation
- Persistent fields
- MongoDB annotations
- Relationships/references
- Extending common base entities where applicable

The domain entity should not contain controller/API concerns.

---

## 5.2 Model

The `model` package contains API/application models.

Example:

```text
model/
└── OrganizationModel.java
```

Models are used instead of exposing MongoDB domain entities directly through the API.

Responsibilities:

- API data representation
- Request/response data where appropriate
- Nested model representation
- Hiding persistence implementation details

The project prefers a unified model approach rather than creating unnecessary `Request` and `Response` classes for every operation.

---

## 5.3 Transformer

The transformer converts between domain entities and API models.

Standard methods:

```java
toModel(...)
toEntity(...)
toModels(...)
toEntities(...)
```

Flow:

```text
Domain Entity <──── Transformer ────> API Model
```

Example:

```text
Organization
      │
      ▼
OrganizationTransformer
      │
      ▼
OrganizationModel
```

Transformers should not contain business rules.

---

## 5.4 Repository

Repositories are responsible for database access.

Example:

```text
OrganizationRepository
```

Typical responsibilities:

- CRUD operations
- Simple derived queries
- Pagination
- Database persistence

Complex MongoDB queries should use the custom repository layer.

---

## 5.5 Custom Repository

The custom repository layer is used for complex searches and dynamic queries.

```text
ModuleCustomRepository
        │
        ▼
ModuleCustomRepositoryImpl
        │
        ▼
MongoTemplate
```

Typical use cases:

- Multiple optional filters
- Search criteria
- Sorting
- Pagination
- Dynamic MongoDB queries
- Aggregation queries

---

# 6. Service Layer

The service layer contains business logic.

```text
Controller
    ↓
Handler
    ↓
Service
    ↓
Repository
```

Responsibilities include:

- Business rules
- Validation that belongs to business logic
- Create/update/delete operations
- Transaction-like workflows where applicable
- Permission/business checks
- Coordinating multiple repositories/services
- Publishing realtime events when required

Services should not depend on HTTP-specific concerns.

---

# 7. Handler Layer

Handlers sit between controllers and services.

```text
Controller
    ↓
Handler
    ↓
Service
```

The handler is responsible for application/API orchestration such as:

- Calling the correct service method
- Preparing API responses
- Coordinating model transformation
- Handling operation-specific flow
- Keeping controllers thin

The controller should primarily define the HTTP endpoint.

---

# 8. Controller Layer

Controllers expose REST APIs.

Example:

```text
/api/organization
/api/branch
/api/subscription-plan
/api/role-permission
```

Responsibilities:

- HTTP endpoints
- Path/query parameters
- Request models
- Authentication/authorization annotations
- Calling handlers
- Returning API responses

Controllers should not contain database queries or large business rules.

---

# 9. Standard Request Flow

A normal CRUD request follows:

```text
React
  │
  │ HTTP
  ▼
Controller
  │
  ▼
Handler
  │
  ▼
Service
  │
  ▼
Repository / Custom Repository
  │
  ▼
MongoDB
```

For a response:

```text
MongoDB
   │
   ▼
Repository
   │
   ▼
Service
   │
   ▼
Handler
   │
   ▼
Transformer
   │
   ▼
Model
   │
   ▼
Controller
   │
   ▼
React
```

---

# 10. Multi-Tenant Architecture

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
├── organizations
├── branches
├── users
├── employees
├── menu_items
├── orders
├── inventory
└── ...
```

Each tenant-owned document contains a tenant identifier.

Conceptually:

```json
{
  "id": "...",
  "tenantId": "...",
  "name": "...",
  "isActive": true
}
```

Tenant isolation must be enforced in application/database queries.

A request must never be allowed to read or modify another tenant's data.

---

# 11. Organization and Branch Hierarchy

The restaurant structure is:

```text
Super Admin
    │
    ▼
Restaurant Owner
    │
    ▼
Organization / Restaurant
    │
    ├── Branch
    │    ├── Floor
    │    │    └── Table
    │    ├── Employees
    │    ├── Orders
    │    └── Inventory
    │
    └── Subscription
```

An organization represents the restaurant/business account.

A branch represents an individual physical location.

---

# 12. Authentication Architecture

Authentication uses:

```text
JWT Access Token
+
JWT Refresh Token
+
Token Version
+
BCrypt Password Hashing
```

Authentication flow:

```text
Login
  │
  ▼
Credentials Validation
  │
  ▼
User Authentication
  │
  ▼
JWT Access Token
+
Refresh Token
  │
  ▼
Client
```

For protected requests:

```text
HTTP Request
     │
     ▼
JwtAuthFilter
     │
     ▼
Extract JWT
     │
     ▼
Validate Token
     │
     ▼
Load Authentication
     │
     ▼
Spring Security
     │
     ▼
Controller
```

---

# 13. Authorization Architecture

Authorization uses:

```text
User
  │
  ▼
Role
  │
  ▼
Permissions
```

Example:

```text
Restaurant Owner
       │
       ▼
      Role
       │
       ├── ORGANIZATION_CREATE
       ├── ORGANIZATION_VIEW
       ├── BRANCH_CREATE
       ├── BRANCH_VIEW
       ├── ORDER_VIEW
       └── REPORT_VIEW
```

Permissions are represented using permission identifiers such as:

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
```

Spring Security method authorization can be applied using `@PreAuthorize`.

---

# 14. Roles

The planned role hierarchy includes:

```text
Super Admin
Restaurant Owner
Branch Manager
Cashier
Waiter
Kitchen Staff
Inventory Manager
Customer
```

Roles determine which permissions a user receives.

The system should avoid hardcoding business permissions throughout controllers and services.

---

# 15. Common Base Entity

Most persistent entities use a common base entity.

Conceptually:

```text
BaseEntity
│
├── UUID id
├── boolean isActive
├── createdAt
└── updatedAt
```

This provides:

- Consistent IDs
- Soft deletion
- Creation timestamps
- Update timestamps

---

# 16. Soft Delete Architecture

The project uses soft deletion rather than immediately removing business records.

```text
isActive = true
```

means active.

```text
isActive = false
```

means deactivated/deleted.

Typical lifecycle:

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

This is especially important for business records where historical information must be retained.

---

# 17. Realtime Architecture

The application uses both **SSE** and **WebSocket** depending on the feature.

## SSE

Server-Sent Events are appropriate for server-to-client updates such as:

```text
Organization created
Organization updated
Organization deleted
Branch created
Branch updated
Permission changed
Table status changed
```

Conceptual flow:

```text
MongoDB
   ↓
Service
   ↓
SSE Event Service
   ↓
SseEmitter
   ↓
React
   ↓
UI Update
```

The frontend should update the affected data without requiring a full page reload.

## WebSocket

WebSocket is suitable for realtime interactive workflows such as:

```text
Kitchen updates
Order status
POS events
Notifications
Live operational state
```

---

# 18. Menu Architecture

The menu domain is structured around:

```text
Category
   │
   ▼
MenuItem
   │
   ├── MenuVariant
   ├── ModifierGroup
   │       └── Modifier
   │
   └── MenuItemModifierGroup
```

This allows restaurants to support:

- Categories
- Menu items
- Variants/sizes
- Add-ons
- Modifier groups
- Optional modifiers

---

# 19. Floor and Table Architecture

The physical restaurant structure is:

```text
Organization
    │
    ▼
Branch
    │
    ▼
Floor
    │
    ▼
RestaurantTable
```

Tables can have operational states such as:

```text
AVAILABLE
OCCUPIED
RESERVED
CLEANING
OUT_OF_SERVICE
```

Table-related functionality includes:

```text
Table
TableStatusHistory
TableCombination
```

Table assignment is closely related to reservation workflows and should be handled as part of the reservation/table-assignment process rather than duplicating reservation logic inside the basic table module.

---

# 20. Order Architecture

The order module is one of the central business modules.

Conceptually:

```text
Order
│
├── OrderItem
│     └── OrderItemModifier
│
├── OrderPayment
├── OrderDiscount
├── OrderTax
├── OrderStatusHistory
├── OrderDelivery
├── OrderKitchenTicket
├── OrderAttachment
├── OrderRefund
└── OrderSplit
      └── OrderSplitItem
```

Order lifecycle:

```text
Created
   ↓
Confirmed
   ↓
Preparing
   ↓
Ready
   ↓
Completed
```

Other states may exist depending on order type and operational requirements.

---

# 21. Kitchen Architecture

Kitchen functionality is connected to orders but should remain a separate module for operational workflows.

```text
Order
  │
  ▼
Kitchen Ticket
  │
  ▼
Kitchen Station
  │
  ▼
Preparation
  │
  ▼
Ready
```

Realtime updates are particularly important in kitchen workflows.

---

# 22. Inventory Architecture

Inventory should connect:

```text
Vendor
   │
   ▼
Purchase
   │
   ▼
Inventory
   │
   ├── Stock
   ├── Stock Movement
   └── Adjustments
          │
          ▼
       Reports
```

Inventory should also integrate with recipes.

---

# 23. Recipe Architecture

Recipes connect menu items to inventory ingredients.

```text
MenuItem
   │
   ▼
Recipe
   │
   ├── Ingredient
   ├── Quantity
   └── Unit
```

When an order is completed, inventory consumption can be calculated from recipe definitions where the business rules require it.

---

# 24. Purchase Architecture

Purchasing manages procurement from vendors.

Typical flow:

```text
Vendor
  ↓
Purchase Order
  ↓
Purchase Items
  ↓
Receiving
  ↓
Inventory Increase
  ↓
Accounting
```

Purchase functionality should remain separate from inventory storage while providing integration points between them.

---

# 25. Reservation Architecture

Reservation functionality connects customers, tables, and time slots.

Conceptually:

```text
Customer
   │
   ▼
Reservation
   │
   ├── Branch
   ├── Date/Time
   ├── Guest Count
   ├── Table Assignment
   └── Status
```

Table assignment belongs naturally to the reservation workflow when a reservation needs one or more specific tables.

Table combinations can be used when multiple tables are combined for larger parties.

---

# 26. Tax Architecture

Tax is designed as a reusable business component.

Tax information may be applied to:

```text
Order
OrderItem
Purchase
Expense
Accounting
```

Tax calculations should be centralized rather than duplicated in every module.

---

# 27. Payment Architecture

Payment functionality should support multiple payment methods.

Example:

```text
Cash
Card
Bank
Wallet
Online Payment
Other configured methods
```

Payment information should be separated from order business logic where possible.

---

# 28. Expense Architecture

The expense module contains concepts such as:

```text
Expense
ExpenseCategory
ExpenseApproval
ExpenseAttachment
ExpenseRecurring
ExpenseStatus
ExpenseType
```

Typical flow:

```text
Expense Created
      ↓
Approval
      ↓
Approved / Rejected
      ↓
Accounting
```

---

# 29. Accounting Architecture

Accounting integrates with operational modules.

Potential sources include:

```text
Sales
Payments
Purchases
Expenses
Taxes
Payroll
Refunds
```

Conceptually:

```text
Operational Modules
        │
        ▼
Accounting Entries
        │
        ▼
Financial Reports
```

Accounting should maintain an auditable history of financial transactions.

---

# 30. Employee and Attendance Architecture

Employee management is separate from authentication.

```text
User
  │
  ▼
Employee
  │
  ├── Role
  ├── Branch
  └── Employment Information
```

Attendance can then track:

```text
Employee
   │
   ▼
Attendance
   ├── Check In
   ├── Check Out
   └── Attendance Status
```

---

# 31. Payroll Architecture

Payroll depends on employee and attendance information.

```text
Employee
   │
   ├── Salary
   ├── Attendance
   └── Adjustments
          │
          ▼
       Payroll
          │
          ▼
      Accounting
```

---

# 32. Customer and Loyalty Architecture

Customers are separate from employees.

```text
Customer
   │
   ├── Orders
   ├── Reservations
   └── Loyalty
```

Loyalty may include:

```text
Points
Rewards
Transactions
Redemptions
```

---

# 33. Notification Architecture

Notifications provide a common mechanism for:

```text
System notifications
Order notifications
Kitchen notifications
Reservation notifications
Payment notifications
Administrative notifications
```

Realtime delivery can use WebSocket/SSE where appropriate.

---

# 34. Audit Architecture

Important business and administrative actions should be auditable.

Example:

```text
User
  │
  ▼
Action
  │
  ▼
Audit Log
```

Audit information may include:

```text
userId
tenantId
action
module
entityId
timestamp
oldValue
newValue
```

Sensitive information should not be logged unnecessarily.

---

# 35. Reporting Architecture

Reporting should consume information from operational modules rather than duplicating business logic.

Potential reports:

```text
Sales
Orders
Products
Inventory
Purchases
Expenses
Taxes
Employees
Attendance
Payroll
Customers
Reservations
Accounting
```

For MongoDB-heavy reporting, aggregation pipelines can be implemented through custom repositories/services.

---

# 36. Subscription Architecture

The SaaS platform supports subscription plans.

Example plan limits:

```text
branchesLimit
usersLimit
menuItemsLimit
ordersPerMonth
monthlyPrice
yearlyPrice
```

Conceptually:

```text
Restaurant
   │
   ▼
Subscription
   │
   ▼
Subscription Plan
```

Subscription rules should be enforced at the service/application layer before operations that exceed plan limits.

---

# 37. API Response Architecture

The project uses common response wrappers such as:

```text
ApiResponse
PageResponse
```

Typical paginated response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

The exact response structure should remain consistent across modules.

---

# 38. Search Architecture

Search-heavy modules should use a dedicated search criteria model.

Example:

```text
OrganizationSearchCriteria
BranchSearchCriteria
TableSearchCriteria
OrderSearchCriteria
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

This supports:

- Multiple filters
- Pagination
- Sorting
- Text/search fields
- Active/inactive filtering
- Tenant filtering

---

# 39. MongoDB Relationships

The project may use MongoDB references where appropriate, including `@DBRef`.

Example:

```text
Organization
   │
   └── Branch
```

References should be used carefully because excessive document references can increase query complexity.

For high-frequency operational data, embedding or storing stable IDs may be preferable depending on the access pattern.

---

# 40. UUID Architecture

The system uses UUID identifiers.

MongoDB UUID handling is explicitly configured using the standard UUID representation.

Conceptually:

```text
Java UUID
    ↓
MongoDB UUID STANDARD
```

This avoids UUID codec/representation mismatches between Java and MongoDB.

---

# 41. Configuration Architecture

The configuration layer includes responsibilities such as:

```text
config/
│
├── ApplicationInitializer
├── JwtConfig
├── MongoConfig
├── MongoUuidConfig
├── SecurityConfig
├── SwaggerConfig
├── WebSocketConfig
├── RateLimiter
└── RefreshTokenCleanupScheduler
```

Configuration should remain separate from business modules.

---

# 42. Security Architecture

Security responsibilities include:

```text
Authentication
Authorization
Password hashing
JWT validation
Refresh token management
Token invalidation
Rate limiting
Permission checks
```

Password storage must use BCrypt or another appropriate password hashing mechanism.

Passwords must never be stored as plain text.

---

# 43. Token Invalidation

The access/refresh token system uses token-versioning concepts.

Conceptually:

```text
User
 └── tokenVersion
```

When a logout/security invalidation operation requires it:

```text
tokenVersion++
```

Previously issued tokens with the old version can then be rejected.

This provides a mechanism for invalidating tokens without relying solely on token expiration.

---

# 44. Frontend Architecture

The React frontend is organized around pages, components, shared UI, API clients, and realtime services.

Conceptually:

```text
src/
│
├── components/
├── pages/
├── layouts/
├── services/
├── hooks/
├── utils/
├── context/
├── routes/
└── assets/
```

The frontend communicates with Spring Boot through REST APIs.

---

# 45. Frontend API Communication

A shared Axios client should be used for backend communication.

Conceptually:

```text
React Component
      ↓
API Service / Axios Client
      ↓
Spring Boot REST API
```

The shared client is responsible for common concerns such as:

- Base URL
- Authorization header
- Refresh-token handling
- Common error handling

---

# 46. Frontend Realtime Updates

For SSE-enabled modules:

```text
React Page
   │
   ▼
EventSource
   │
   ▼
Spring Boot SSE Endpoint
```

Example:

```text
/api/branch/stream
```

When an event arrives:

```text
SSE Event
   ↓
React State Update
   ↓
UI Refresh
```

The goal is to update only the relevant UI state rather than reloading the entire browser page.

---

# 47. Error Handling

The backend should provide consistent error responses.

Errors should distinguish between:

```text
Validation Error
Authentication Error
Authorization Error
Not Found
Conflict
Business Rule Violation
Database Error
Unexpected Server Error
```

Business exceptions should not expose internal stack traces to clients.

---

# 48. Validation

Validation should occur at appropriate boundaries.

```text
API Input
   ↓
Validation
   ↓
Handler
   ↓
Service Business Rules
```

Examples:

- Required fields
- Email format
- Numeric ranges
- Duplicate business identifiers
- Subscription limits
- Tenant ownership
- Valid entity relationships

---

# 49. Permission Enforcement

Permission checks should exist at the backend, not only in the React UI.

Frontend permission checks are useful for hiding unavailable actions:

```text
User Permission
      ↓
React UI
```

But the backend remains authoritative:

```text
Request
   ↓
Spring Security
   ↓
Permission Check
   ↓
Business Logic
```

A user must not gain access simply by manually calling an API endpoint.

---

# 50. Data Ownership and Tenant Security

Every tenant-sensitive query must respect tenant boundaries.

Bad:

```text
findById(id)
```

when the ID alone is not sufficient to establish tenant ownership.

Preferred conceptual approach:

```text
findByIdAndTenantId(id, tenantId)
```

or equivalent tenant-aware repository/custom-query logic.

This principle applies to:

```text
Organizations
Branches
Employees
Customers
Menus
Tables
Orders
Inventory
Purchases
Expenses
Reports
and other tenant-owned resources
```

---

# 51. Auditability

Business-critical operations should be traceable.

Examples:

```text
Who created the order?
Who changed its status?
Who modified a menu price?
Who approved an expense?
Who changed a user's permissions?
Who deleted/reactivated a record?
```

Audit logs should preserve enough information to investigate these operations.

---

# 52. Module Dependency Principle

Modules may depend on other modules through well-defined interfaces and IDs.

Avoid uncontrolled circular dependencies.

Example:

```text
Order
 ├── Menu
 ├── Customer
 ├── Table
 ├── Tax
 ├── Payment
 └── Kitchen
```

But:

```text
Menu ↔ Order ↔ Inventory ↔ Menu
```

should not become a tightly coupled implementation cycle.

Business dependencies should be deliberate.

---

# 53. Separation of Concerns

The project follows the principle:

```text
Controller
    = HTTP

Handler
    = Application orchestration

Service
    = Business logic

Transformer
    = Entity ↔ Model conversion

Repository
    = Database access

Domain
    = Persistence model

Model
    = API/application representation
```

This makes the application easier to test, maintain, and extend.

---

# 54. Recommended Module Dependency Overview

```text
                         ┌──────────────┐
                         │ Subscription │
                         └──────┬───────┘
                                │
                                ▼
┌────────────┐          ┌──────────────┐
│    Auth    │─────────▶│ Organization │
└─────┬──────┘          └──────┬───────┘
      │                         │
      ▼                         ▼
┌────────────┐             ┌────────┐
│    User    │             │ Branch │
└─────┬──────┘             └───┬────┘
      │                        │
      ▼                        ▼
┌────────────┐           ┌──────────────┐
│  Employee  │           │ Floor / Table│
└────────────┘           └──────┬───────┘
                                │
                                ▼
                         ┌────────────┐
                         │Reservation │
                         └─────┬──────┘
                               │
                               ▼
                         ┌──────────┐
                         │  Order   │
                         └────┬─────┘
                              │
            ┌─────────────────┼──────────────────┐
            ▼                 ▼                  ▼
        ┌────────┐       ┌─────────┐        ┌─────────┐
        │ Kitchen│       │ Payment │        │ Delivery│
        └────────┘       └─────────┘        └─────────┘
            │
            ▼
       ┌──────────┐
       │ Inventory│◀──── Purchase ◀──── Vendor
       └────┬─────┘
            │
            ▼
        ┌────────┐
        │ Recipe │◀──── Menu
        └────────┘

Operational Modules
        │
        ▼
┌──────────────────────────────┐
│ Tax / Expense / Accounting   │
└──────────────┬───────────────┘
               │
               ▼
           Reporting
```

---

# 55. Current Core Modules

The project already contains or has been actively implementing concepts in these areas:

```text
Authentication
Organization
Branch
User
Employee
Customer

Role
Permission
RolePermission

Subscription

Menu
Category
MenuItem
MenuVariant
Modifier
ModifierGroup
MenuItemModifierGroup

Floor
Table
TableStatusHistory
TableCombination

Order
OrderItem
OrderItemModifier
OrderPayment
OrderDiscount
OrderTax
OrderStatusHistory
OrderDelivery
OrderKitchenTicket
OrderAttachment
OrderRefund
OrderSplit
OrderSplitItem

Expense
ExpenseApproval
ExpenseAttachment
ExpenseCategory
ExpenseRecurring
ExpenseStatus
ExpenseType
```

Additional modules are planned/being expanded around:

```text
Vendor
Recipe
Inventory
Purchase
Reservation
Kitchen
Delivery
Tax
Accounting
Attendance
Payroll
Loyalty
Notification
Audit
Report
Settings
```

---

# 56. Scalability Strategy

The first implementation can remain a modular monolith.

```text
React
   ↓
Spring Boot Modular Monolith
   ↓
MongoDB
```

This is preferable to prematurely splitting every module into microservices.

If the platform grows significantly, modules with independent scaling requirements can later be extracted.

Potential candidates could include:

```text
Notification
Reporting
Kitchen realtime
Payment processing
Search
```

The initial architecture should therefore maintain clean module boundaries even while running as one application.

---

# 57. Performance Principles

Important performance practices include:

- Pagination for large collections
- Indexed tenant IDs
- Indexed frequently searched fields
- Avoiding unbounded queries
- MongoDB aggregation for reporting
- Efficient projection when full documents are unnecessary
- Realtime updates instead of unnecessary full-page reloads
- Caching where justified
- Avoiding excessive `@DBRef` traversal
- Background processing for expensive operations

---

# 58. API Design Principles

REST endpoints should follow consistent conventions.

Examples:

```text
GET    /api/branch
GET    /api/branch/{id}
POST   /api/branch
PUT    /api/branch/{id}
DELETE /api/branch/{id}
PATCH  /api/branch/{id}/restore
GET    /api/branch/search
GET    /api/branch/stream
```

Exact endpoint naming may vary by module, but consistency is required across the application.

---

# 59. Development Principles

The project should follow:

### Single Responsibility Principle

Each class should have one clear responsibility.

### Don't Repeat Yourself

Common functionality should be reused instead of copied across modules.

### Keep Controllers Thin

Business logic belongs in services.

### Keep Repositories Focused

Database logic belongs in repositories/custom repositories.

### Explicit Module Boundaries

A module should expose only what other modules need.

### Backend Is Authoritative

Frontend permission checks must never replace backend authorization.

### Tenant Isolation Is Mandatory

Every tenant-owned operation must respect tenant boundaries.

---

# 60. Target Architecture

The long-term target is:

```text
                         ┌──────────────────────┐
                         │      Web Client      │
                         │ React + Vite +       │
                         │ Tailwind             │
                         └──────────┬───────────┘
                                    │
                           REST / SSE / WS
                                    │
                                    ▼
                    ┌─────────────────────────────┐
                    │      Spring Boot API        │
                    │                             │
                    │ Authentication              │
                    │ Authorization               │
                    │ Tenant Context              │
                    │                             │
                    │ ┌─────────────────────────┐ │
                    │ │ Modular Business Layer  │ │
                    │ │                         │ │
                    │ │ Organization / Branch    │ │
                    │ │ User / Employee         │ │
                    │ │ Menu / Recipe           │ │
                    │ │ Inventory / Purchase    │ │
                    │ │ Floor / Table           │ │
                    │ │ Reservation / Order     │ │
                    │ │ Kitchen / Delivery      │ │
                    │ │ Tax / Payment           │ │
                    │ │ Expense / Accounting    │ │
                    │ │ Payroll / Loyalty       │ │
                    │ │ Notification / Audit    │ │
                    │ │ Reports / Settings      │ │
                    │ └─────────────────────────┘ │
                    └──────────────┬──────────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │     MongoDB       │
                         │                   │
                         │ Shared Database   │
                         │ Shared Collections│
                         │ tenantId isolation│
                         └───────────────────┘
```

---

# 61. Architecture Goal

The architecture is designed to provide:

- Multi-tenant restaurant management
- POS and order management
- Branch management
- Menu management
- Table and reservation management
- Kitchen operations
- Inventory and purchasing
- Employee and payroll management
- Expenses and accounting
- Customer and loyalty management
- Subscription-based SaaS
- RBAC and fine-grained permissions
- Realtime operational updates
- Auditable business operations
- Scalable modular development

The core architectural rule is:

```text
Keep modules independent,
keep layers responsible,
keep tenant data isolated,
keep security on the backend,
and keep business logic out of controllers.
```
