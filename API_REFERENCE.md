# Restaurant ERP SaaS --- API Reference

## 1. Purpose

This document defines the API standards and reference structure for the
Restaurant ERP SaaS backend.

The API is designed for:

-   Restaurant owners
-   Branch managers
-   Cashiers
-   Waiters
-   Kitchen staff
-   Inventory staff
-   Customers
-   Super administrators
-   React frontend applications
-   Bruno API testing
-   Future mobile/POS clients
-   Future third-party integrations

The API follows REST principles and uses JWT authentication,
permission-based authorization, tenant isolation, pagination,
validation, soft deletion, and realtime SSE/WebSocket communication.

------------------------------------------------------------------------

# 2. API Architecture

``` text
React / Mobile / POS / External Client
                |
                v
        REST API / SSE / WebSocket
                |
                v
           Controller
                |
                v
             Handler
                |
                v
             Service
                |
                v
           Transformer
                |
                v
           Repository
                |
                v
             MongoDB
```

### Responsibility

  -----------------------------------------------------------------------
Layer                               Responsibility
  ----------------------------------- -----------------------------------
Controller                          HTTP endpoint, request parameters,
authentication/authorization

Handler                             API orchestration and response
preparation

Service                             Business rules

Transformer                         Entity ↔ model conversion

Repository                          MongoDB access

MongoDB                             Persistent storage
-----------------------------------------------------------------------

Controllers should not contain business logic.

------------------------------------------------------------------------

# 3. Base URL

## Local Development

``` text
http://localhost:8080
```

## API Prefix

``` text
/api
```

Example:

``` text
http://localhost:8080/api/organization
```

## Production

The production base URL should be configured through environment
variables.

Example:

``` text
https://api.example.com
```

Production URLs must not be hardcoded in frontend source code.

------------------------------------------------------------------------

# 4. Content Type

Most API requests and responses use:

``` http
Content-Type: application/json
```

For file uploads:

``` http
Content-Type: multipart/form-data
```

For SSE:

``` http
Accept: text/event-stream
```

For WebSocket:

``` text
WebSocket / STOMP / SockJS
```

depending on the configured realtime endpoint.

------------------------------------------------------------------------

# 5. Authentication

Protected APIs use JWT Bearer authentication.

``` http
Authorization: Bearer <access-token>
```

Example:

``` http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The access token should be sent with every protected request.

------------------------------------------------------------------------

# 6. Authentication Endpoints

Recommended authentication API structure:

``` text
/api/auth
```

## Login

``` http
POST /api/auth/login
```

Request:

``` json
{
  "email": "owner@example.com",
  "password": "password"
}
```

Response:

``` json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": "uuid",
    "fullName": "Restaurant Owner",
    "email": "owner@example.com",
    "roleId": "uuid",
    "role": "RESTAURANT_OWNER",
    "referralCode": "ABC123",
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "permissions": [
      "ORGANIZATION_VIEW",
      "ORGANIZATION_CREATE",
      "BRANCH_VIEW"
    ]
  }
}
```

## Refresh Token

``` http
POST /api/auth/refresh
```

Request:

``` json
{
  "refreshToken": "refresh-token"
}
```

## Logout

``` http
POST /api/auth/logout
```

Header:

``` http
Authorization: Bearer <access-token>
```

Logout invalidates the current authentication session/token version as
configured by the backend.

## Current User

``` http
GET /api/auth/me
```

------------------------------------------------------------------------

# 7. Standard API Response

The application should use a consistent response wrapper.

Example:

``` json
{
  "success": true,
  "message": "Organization created successfully",
  "data": {
    "id": "uuid",
    "name": "Demo Restaurant"
  }
}
```

Recommended conceptual structure:

``` text
ApiResponse<T>
├── success
├── message
└── data
```

### Successful response

``` json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

### Failed response

``` json
{
  "success": false,
  "message": "Organization not found",
  "data": null
}
```

The exact Java implementation may include additional metadata when
required.

------------------------------------------------------------------------

# 8. Pagination Response

Collection endpoints should use a standard page response.

Example:

``` json
{
  "success": true,
  "message": "Organizations retrieved successfully",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 125,
    "totalPages": 7,
    "first": true,
    "last": false
  }
}
```

Recommended conceptual structure:

``` text
PageResponse<T>
├── content
├── page
├── size
├── totalElements
├── totalPages
├── first
└── last
```

------------------------------------------------------------------------

# 9. Pagination Parameters

Default:

``` text
?page=0&size=20
```

Example:

``` http
GET /api/organization/search?page=0&size=20
```

### Parameters

Parameter       Meaning
  --------------- ------------------------
page            Zero-based page number
size            Number of records
sortBy          Field used for sorting
sortDirection   ASC or DESC

Example:

``` http
GET /api/organization/search?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

------------------------------------------------------------------------

# 10. Search API Convention

Search endpoints should use:

``` text
GET /api/{module}/search
```

Example:

``` http
GET /api/organization/search
```

Possible parameters:

``` text
?page=0
&size=20
&search=demo
&isActive=true
&sortBy=createdAt
&sortDirection=DESC
```

Not every module needs every parameter.

Search criteria should be represented by a dedicated search
criteria/model class where appropriate.

------------------------------------------------------------------------

# 11. CRUD Convention

Standard CRUD endpoints:

``` text
POST   /api/{module}
GET    /api/{module}/{id}
PUT    /api/{module}/{id}
GET    /api/{module}/search
DELETE /api/{module}/{id}
PATCH  /api/{module}/{id}/restore
```

Example:

``` text
POST   /api/organization
GET    /api/organization/{id}
PUT    /api/organization/{id}
GET    /api/organization/search
DELETE /api/organization/{id}
PATCH  /api/organization/{id}/restore
```

The exact endpoint can vary when the domain requires a different
operation.

------------------------------------------------------------------------

# 12. Soft Delete

The system uses soft deletion through:

``` text
isActive
```

A normal delete should generally mark the entity inactive rather than
physically removing it.

Example:

``` http
DELETE /api/organization/{id}
```

Result:

``` json
{
  "success": true,
  "message": "Organization deleted successfully",
  "data": null
}
```

Restore:

``` http
PATCH /api/organization/{id}/restore
```

Result:

``` json
{
  "success": true,
  "message": "Organization restored successfully",
  "data": null
}
```

------------------------------------------------------------------------

# 13. Active/Inactive Filtering

Search endpoints can support:

``` text
isActive=true
```

or:

``` text
isActive=false
```

Example:

``` http
GET /api/branch/search?isActive=true&page=0&size=20
```

Default behavior should be clearly defined per repository/service and
should normally prevent inactive records from appearing in normal
operational queries.

------------------------------------------------------------------------

# 14. HTTP Status Codes

Recommended status codes:

Status   Meaning
  -------- ---------------------------------------------
200      Successful request
201      Resource created
204      Successful request with no response body
400      Invalid request
401      Authentication required/invalid
403      Permission denied
404      Resource not found
409      Conflict
422      Validation/business-rule failure where used
429      Too many requests
500      Internal server error

------------------------------------------------------------------------

# 15. Validation Errors

Example:

``` json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Invalid email address",
    "branchName": "Branch name is required"
  }
}
```

Validation should happen before business processing.

Typical annotations include:

``` java
@NotNull
@NotBlank
@Email
@Size
@Min
@Max
@Positive
@PositiveOrZero
```

------------------------------------------------------------------------

# 16. Authentication vs Authorization

These are separate concerns.

### Authentication

Answers:

``` text
Who are you?
```

Handled by:

``` text
JWT
User
JwtAuthFilter
SecurityConfig
```

### Authorization

Answers:

``` text
What are you allowed to do?
```

Handled by:

``` text
Role
Permission
RolePermission
@PreAuthorize
```

Example:

``` java
@PreAuthorize("hasAuthority('ORGANIZATION_CREATE')")
```

------------------------------------------------------------------------

# 17. Permission Naming Convention

Permissions should follow:

``` text
MODULE_ACTION
```

Examples:

``` text
ORGANIZATION_CREATE
ORGANIZATION_VIEW
ORGANIZATION_UPDATE
ORGANIZATION_DELETE
ORGANIZATION_REACTIVATE
```

Branch:

``` text
BRANCH_CREATE
BRANCH_VIEW
BRANCH_UPDATE
BRANCH_DELETE
BRANCH_REACTIVATE
```

Role/permission administration can use:

``` text
ROLE_CREATE
ROLE_VIEW
ROLE_UPDATE
ROLE_DELETE

PERMISSION_CREATE
PERMISSION_VIEW
PERMISSION_UPDATE
PERMISSION_DELETE
```

------------------------------------------------------------------------

# 18. Tenant Isolation

The application uses:

``` text
Shared Database
        +
Shared Collections
        +
tenantId
```

Every tenant-owned document must be associated with the correct tenant.

Conceptually:

``` text
Organization
   |
   +-- tenantId
   |
   +-- Branch
   +-- Employee
   +-- Menu
   +-- Inventory
   +-- Orders
   +-- Reservations
   +-- Tables
```

Tenant filtering must happen on the backend.

The frontend must never be trusted to enforce tenant isolation.

------------------------------------------------------------------------

# 19. Tenant Security Rule

A request must not be allowed to access another tenant's records merely
by changing:

``` text
id
tenantId
organizationId
branchId
```

Example of an unsafe request:

``` http
GET /api/order/{another-tenant-order-id}
```

The service/repository layer must verify tenant ownership.

------------------------------------------------------------------------

# 20. Organization APIs

Base path:

``` text
/api/organization
```

### Create

``` http
POST /api/organization
```

### Get by ID

``` http
GET /api/organization/{id}
```

### Update

``` http
PUT /api/organization/{id}
```

### Search

``` http
GET /api/organization/search
```

### Delete

``` http
DELETE /api/organization/{id}
```

### Restore

``` http
PATCH /api/organization/{id}/restore
```

### SSE

``` http
GET /api/organization/stream
```

Typical events:

``` text
organization-created
organization-updated
organization-deleted
organization-restored
```

------------------------------------------------------------------------

# 21. Branch APIs

Base path:

``` text
/api/branch
```

### Create

``` http
POST /api/branch
```

### Get

``` http
GET /api/branch/{id}
```

### Update

``` http
PUT /api/branch/{id}
```

### Search

``` http
GET /api/branch/search
```

### Delete

``` http
DELETE /api/branch/{id}
```

### Restore

``` http
PATCH /api/branch/{id}/restore
```

### SSE

``` http
GET /api/branch/stream
```

Example branch search:

``` http
GET /api/branch/search?page=0&size=20&branchName=Main&city=Bahawalpur&isActive=true
```

------------------------------------------------------------------------

# 22. Subscription Plan APIs

Base path:

``` text
/api/subscription-plan
```

Recommended endpoints:

``` text
POST   /api/subscription-plan
GET    /api/subscription-plan/{id}
PUT    /api/subscription-plan/{id}
GET    /api/subscription-plan/search
DELETE /api/subscription-plan/{id}
PATCH  /api/subscription-plan/{id}/restore
GET    /api/subscription-plan/stream
```

Subscription plan fields include concepts such as:

``` text
name
branchesLimit
usersLimit
menuItemsLimit
ordersPerMonth
monthlyPrice
yearlyPrice
```

------------------------------------------------------------------------

# 23. Role APIs

Base path:

``` text
/api/role
```

Recommended endpoints:

``` text
POST   /api/role
GET    /api/role/{id}
PUT    /api/role/{id}
GET    /api/role/search
DELETE /api/role/{id}
PATCH  /api/role/{id}/restore
```

------------------------------------------------------------------------

# 24. Permission APIs

Base path:

``` text
/api/permission
```

Recommended endpoints:

``` text
POST   /api/permission
GET    /api/permission/{id}
PUT    /api/permission/{id}
GET    /api/permission/search
DELETE /api/permission/{id}
PATCH  /api/permission/{id}/restore
GET    /api/permission/stream
```

Permission matrix:

``` http
GET /api/role-permission/matrix
```

------------------------------------------------------------------------

# 25. Role Permission APIs

Base path:

``` text
/api/role-permission
```

Recommended operations:

``` text
POST /api/role-permission
GET  /api/role-permission/{id}
PUT  /api/role-permission/{id}
GET  /api/role-permission/search
DELETE /api/role-permission/{id}
```

For matrix-style permission management, the API should expose a
structure that allows the frontend to display:

``` text
Role
   |
   +-- Module
          |
          +-- CREATE
          +-- VIEW
          +-- UPDATE
          +-- DELETE
          +-- REACTIVATE
```

------------------------------------------------------------------------

# 26. User APIs

Base path:

``` text
/api/user
```

Typical endpoints:

``` text
POST   /api/user
GET    /api/user/{id}
PUT    /api/user/{id}
GET    /api/user/search
DELETE /api/user/{id}
PATCH  /api/user/{id}/restore
```

User management must enforce tenant and role restrictions.

------------------------------------------------------------------------

# 27. Employee APIs

Base path:

``` text
/api/employee
```

Recommended endpoints:

``` text
POST   /api/employee
GET    /api/employee/{id}
PUT    /api/employee/{id}
GET    /api/employee/search
DELETE /api/employee/{id}
PATCH  /api/employee/{id}/restore
```

Employee data should remain separate from authentication identity where
the domain model requires it.

------------------------------------------------------------------------

# 28. Customer APIs

Base path:

``` text
/api/customer
```

Recommended endpoints:

``` text
POST   /api/customer
GET    /api/customer/{id}
PUT    /api/customer/{id}
GET    /api/customer/search
DELETE /api/customer/{id}
PATCH  /api/customer/{id}/restore
```

Customer-specific APIs can later include:

``` text
/api/customer/{id}/orders
/api/customer/{id}/loyalty
/api/customer/{id}/reservations
```

------------------------------------------------------------------------

# 29. Menu APIs

Menu domain:

``` text
Category
MenuItem
MenuVariant
Modifier
ModifierGroup
MenuItemModifierGroup
```

Recommended paths:

``` text
/api/category
/api/menu-item
/api/menu-variant
/api/modifier
/api/modifier-group
/api/menu-item-modifier-group
```

Common operations:

``` text
POST
GET /{id}
PUT /{id}
GET /search
DELETE /{id}
PATCH /{id}/restore
```

------------------------------------------------------------------------

# 30. Recipe APIs

Base path:

``` text
/api/recipe
```

Typical domain relationships:

``` text
Recipe
   |
   +-- MenuItem
   |
   +-- Ingredients
   |
   +-- Quantity
   |
   +-- Unit
```

Recommended endpoints:

``` text
POST   /api/recipe
GET    /api/recipe/{id}
PUT    /api/recipe/{id}
GET    /api/recipe/search
DELETE /api/recipe/{id}
PATCH  /api/recipe/{id}/restore
```

------------------------------------------------------------------------

# 31. Inventory APIs

Inventory APIs should support:

``` text
Items
Stock
Stock Movement
Stock Adjustment
Stock Transfer
Low Stock
Inventory Counts
```

Possible structure:

``` text
/api/inventory
/api/inventory-item
/api/stock
/api/stock-movement
/api/stock-adjustment
/api/stock-transfer
```

Inventory APIs must maintain branch and tenant isolation.

------------------------------------------------------------------------

# 32. Purchase APIs

Purchase domain can include:

``` text
Purchase
PurchaseItem
PurchaseOrder
GoodsReceipt
PurchaseReturn
```

Possible paths:

``` text
/api/purchase
/api/purchase-order
/api/goods-receipt
/api/purchase-return
```

Typical lifecycle:

``` text
Draft
  |
  v
Submitted
  |
  v
Approved
  |
  v
Received
  |
  v
Completed
```

------------------------------------------------------------------------

# 33. Vendor APIs

Base path:

``` text
/api/vendor
```

Typical endpoints:

``` text
POST   /api/vendor
GET    /api/vendor/{id}
PUT    /api/vendor/{id}
GET    /api/vendor/search
DELETE /api/vendor/{id}
PATCH  /api/vendor/{id}/restore
```

------------------------------------------------------------------------

# 34. Floor APIs

Base path:

``` text
/api/floor
```

Recommended endpoints:

``` text
POST   /api/floor
GET    /api/floor/{id}
PUT    /api/floor/{id}
GET    /api/floor/search
DELETE /api/floor/{id}
PATCH  /api/floor/{id}/restore
```

Floor belongs to the organization/branch structure as defined by the
domain model.

------------------------------------------------------------------------

# 35. Table Management APIs

Table management includes:

``` text
RestaurantTable
TableStatus
TableStatusHistory
TableCombination
TableAssignment
```

Possible API paths:

``` text
/api/table
/api/table-status-history
/api/table-combination
/api/table-assignment
```

Common table operations:

``` text
POST   /api/table
GET    /api/table/{id}
PUT    /api/table/{id}
GET    /api/table/search
DELETE /api/table/{id}
PATCH  /api/table/{id}/restore
```

Table status history:

``` text
POST /api/table-status-history
GET  /api/table-status-history/search
GET  /api/table-status-history/table/{tableId}
```

Table combination:

``` text
POST   /api/table-combination
GET    /api/table-combination/{id}
PUT    /api/table-combination/{id}
GET    /api/table-combination/search
DELETE /api/table-combination/{id}
```

Table assignment:

``` text
POST   /api/table-assignment
GET    /api/table-assignment/{id}
PUT    /api/table-assignment/{id}
GET    /api/table-assignment/search
DELETE /api/table-assignment/{id}
```

Table assignment may be used by reservation workflows while the
underlying table resource remains part of table management.

------------------------------------------------------------------------

# 36. Reservation APIs

Base path:

``` text
/api/reservation
```

Typical operations:

``` text
POST   /api/reservation
GET    /api/reservation/{id}
PUT    /api/reservation/{id}
GET    /api/reservation/search
DELETE /api/reservation/{id}
PATCH  /api/reservation/{id}/restore
```

Additional workflow endpoints may include:

``` text
POST /api/reservation/{id}/confirm
POST /api/reservation/{id}/cancel
POST /api/reservation/{id}/check-in
POST /api/reservation/{id}/no-show
POST /api/reservation/{id}/complete
```

------------------------------------------------------------------------

# 37. Order APIs

Order domain:

``` text
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
```

Base path:

``` text
/api/order
```

Core operations:

``` text
POST   /api/order
GET    /api/order/{id}
PUT    /api/order/{id}
GET    /api/order/search
DELETE /api/order/{id}
```

Order lifecycle operations:

``` text
POST /api/order/{id}/confirm
POST /api/order/{id}/cancel
POST /api/order/{id}/hold
POST /api/order/{id}/resume
POST /api/order/{id}/complete
```

Order status history:

``` text
GET /api/order/{id}/status-history
```

Order items:

``` text
POST /api/order/{id}/items
PUT  /api/order/{id}/items/{itemId}
DELETE /api/order/{id}/items/{itemId}
```

Payments:

``` text
POST /api/order/{id}/payments
GET  /api/order/{id}/payments
```

Refunds:

``` text
POST /api/order/{id}/refunds
GET  /api/order/{id}/refunds
```

Splitting:

``` text
POST /api/order/{id}/split
GET  /api/order/{id}/splits
```

------------------------------------------------------------------------

# 38. Kitchen APIs

Base path:

``` text
/api/kitchen
```

Kitchen operations can include:

``` text
GET  /api/kitchen/tickets
GET  /api/kitchen/tickets/{id}
POST /api/kitchen/tickets/{id}/accept
POST /api/kitchen/tickets/{id}/start
POST /api/kitchen/tickets/{id}/ready
POST /api/kitchen/tickets/{id}/complete
```

Kitchen APIs should support realtime updates through SSE/WebSocket where
required by the POS workflow.

------------------------------------------------------------------------

# 39. Delivery APIs

Base path:

``` text
/api/delivery
```

Possible operations:

``` text
POST /api/delivery
GET  /api/delivery/{id}
PUT  /api/delivery/{id}
GET  /api/delivery/search
```

Workflow:

``` text
Pending
   |
Assigned
   |
Picked Up
   |
Out for Delivery
   |
Delivered
```

------------------------------------------------------------------------

# 40. Tax APIs

Base path:

``` text
/api/tax
```

Typical operations:

``` text
POST   /api/tax
GET    /api/tax/{id}
PUT    /api/tax/{id}
GET    /api/tax/search
DELETE /api/tax/{id}
PATCH  /api/tax/{id}/restore
```

Taxes may be applied to:

``` text
Menu Items
Orders
Invoices
Purchases
```

depending on the accounting model.

------------------------------------------------------------------------

# 41. Payment Method APIs

Base path:

``` text
/api/payment-method
```

Possible payment methods:

``` text
CASH
CARD
BANK_TRANSFER
ONLINE
WALLET
OTHER
```

The exact values should be represented by the application's enum/domain
model.

------------------------------------------------------------------------

# 42. Expense APIs

Expense domain includes:

``` text
Expense
ExpenseApproval
ExpenseAttachment
ExpenseCategory
ExpenseRecurring
ExpenseStatus
ExpenseType
```

Possible paths:

``` text
/api/expense
/api/expense-category
/api/expense-approval
/api/expense-recurring
```

Expense workflow:

``` text
Draft
  |
Submitted
  |
Approved / Rejected
  |
Paid
```

------------------------------------------------------------------------

# 43. Accounting APIs

Possible accounting modules:

``` text
/api/accounting
/api/account
/api/journal-entry
/api/ledger
/api/invoice
/api/payment
```

Accounting APIs should maintain immutable financial history where
required.

Financial records should not be casually deleted after posting.

Use reversal/correction workflows when appropriate.

------------------------------------------------------------------------

# 44. Attendance APIs

Base path:

``` text
/api/attendance
```

Possible operations:

``` text
POST /api/attendance/check-in
POST /api/attendance/check-out
GET  /api/attendance/search
GET  /api/attendance/employee/{employeeId}
```

------------------------------------------------------------------------

# 45. Payroll APIs

Base path:

``` text
/api/payroll
```

Possible resources:

``` text
Payroll
PayrollItem
Salary
Deduction
Bonus
Payslip
```

Example:

``` text
POST /api/payroll
GET  /api/payroll/{id}
PUT  /api/payroll/{id}
GET  /api/payroll/search
```

------------------------------------------------------------------------

# 46. Loyalty APIs

Base path:

``` text
/api/loyalty
```

Possible operations:

``` text
POST /api/loyalty/customer/{customerId}/earn
POST /api/loyalty/customer/{customerId}/redeem
GET  /api/loyalty/customer/{customerId}/balance
GET  /api/loyalty/customer/{customerId}/history
```

------------------------------------------------------------------------

# 47. Notification APIs

Base path:

``` text
/api/notification
```

Possible operations:

``` text
GET   /api/notification
GET   /api/notification/{id}
PATCH /api/notification/{id}/read
PATCH /api/notification/read-all
DELETE /api/notification/{id}
```

Realtime notifications can be delivered through:

``` text
SSE
WebSocket
```

------------------------------------------------------------------------

# 48. Audit APIs

Base path:

``` text
/api/audit
```

Audit records should capture important actions such as:

``` text
CREATE
UPDATE
DELETE
RESTORE
LOGIN
LOGOUT
PASSWORD_CHANGE
PERMISSION_CHANGE
STATUS_CHANGE
FINANCIAL_ACTION
```

Example:

``` http
GET /api/audit/search
```

Audit records should normally be read-only from the application user
interface.

------------------------------------------------------------------------

# 49. Reporting APIs

Base path:

``` text
/api/report
```

Possible reports:

``` text
Sales
Orders
Revenue
Expenses
Profit
Inventory
Purchases
Taxes
Employee
Attendance
Customer
Table utilization
Kitchen performance
```

Examples:

``` text
GET /api/report/sales
GET /api/report/revenue
GET /api/report/expenses
GET /api/report/inventory
```

Reports should support date ranges where appropriate:

``` text
from=2026-01-01
to=2026-01-31
```

------------------------------------------------------------------------

# 50. Settings APIs

Base path:

``` text
/api/settings
```

Possible settings:

``` text
Restaurant settings
Branch settings
POS settings
Receipt settings
Tax settings
Notification settings
User preferences
```

Example:

``` text
GET /api/settings
PUT /api/settings
```

Settings should be tenant/branch scoped where appropriate.

------------------------------------------------------------------------

# 51. SSE --- Server-Sent Events

SSE is used for server-to-client realtime updates.

Example:

``` http
GET /api/branch/stream
```

Request:

``` http
Accept: text/event-stream
Authorization: Bearer <access-token>
```

Conceptual response:

``` text
event: branch-created
data: {"id":"uuid","branchName":"Main Branch"}
```

Possible events:

``` text
created
updated
deleted
restored
status-changed
```

The event names should be domain-specific where this improves frontend
handling.

------------------------------------------------------------------------

# 52. SSE Controller Rule

When a module supports SSE, the controller should expose the stream
endpoint.

Conceptually:

``` java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter stream() {
    return service.createEmitter();
}
```

The exact implementation should remain consistent with the application's
SSE service architecture.

------------------------------------------------------------------------

# 53. SSE Connection Lifecycle

Recommended lifecycle:

``` text
Frontend
   |
   | GET /stream
   v
Controller
   |
   v
SSE Service
   |
   v
Register Emitter
   |
   v
Wait for Domain Event
   |
   v
Send Event
   |
   v
Frontend Updates UI
```

The server should remove emitters when:

``` text
connection closes
timeout occurs
error occurs
```

------------------------------------------------------------------------

# 54. WebSocket

WebSocket is appropriate for high-frequency realtime workflows such as:

``` text
Kitchen display
Order status
POS synchronization
Notifications
Table status
Live operational dashboards
```

Conceptual flow:

``` text
Frontend
    |
    v
WebSocket
    |
    v
Backend Event
    |
    v
Subscribed Clients
```

The exact STOMP destination/topic names must be documented when the
WebSocket implementation is finalized.

------------------------------------------------------------------------

# 55. File Upload APIs

File uploads should use:

``` http
multipart/form-data
```

Example:

``` http
POST /api/organization/{id}/logo
```

Request parts:

``` text
file
```

The API should validate:

``` text
File type
File size
File name
Extension
Storage destination
```

Never trust the client-provided filename or MIME type alone.

------------------------------------------------------------------------

# 56. Date and Time Format

Use ISO-8601 format for API date/time values.

Example:

``` text
2026-08-20T14:30:00
```

Date:

``` text
2026-08-20
```

For timezone-aware values, use an explicit offset when required:

``` text
2026-08-20T14:30:00+05:00
```

The application's timezone policy should be centralized rather than
implemented independently by each module.

------------------------------------------------------------------------

# 57. UUID Format

Entity identifiers use UUIDs.

Example:

``` text
550e8400-e29b-41d4-a716-446655440000
```

MongoDB UUID representation is configured consistently using:

``` text
UuidRepresentation.STANDARD
```

Frontend clients should treat IDs as strings.

------------------------------------------------------------------------

# 58. API Naming Rules

Use lowercase kebab-case or the established project convention
consistently.

Recommended:

``` text
/api/menu-item
/api/payment-method
/api/role-permission
```

Avoid inconsistent naming such as:

``` text
/api/MenuItem
/api/menuItem
/api/menu_item
```

Once an API path is released, avoid unnecessary breaking changes.

------------------------------------------------------------------------

# 59. Resource IDs

Resource IDs should be path parameters.

Correct:

``` http
GET /api/branch/550e8400-e29b-41d4-a716-446655440000
```

Avoid:

``` http
GET /api/branch?id=550e8400-e29b-41d4-a716-446655440000
```

for normal resource retrieval.

------------------------------------------------------------------------

# 60. Query Parameters

Query parameters should be used for:

``` text
Filtering
Searching
Pagination
Sorting
Date ranges
Optional views
```

Example:

``` http
GET /api/order/search
    ?page=0
    &size=20
    &status=COMPLETED
    &branchId=uuid
    &from=2026-08-01
    &to=2026-08-31
```

------------------------------------------------------------------------

# 61. Request Models

Request bodies should use dedicated model/DTO structures when the API
contract requires them.

The project convention may use a single model DTO where appropriate
rather than automatically creating separate request and response classes
for every resource.

Do not expose persistence entities directly when doing so would leak
internal implementation details.

------------------------------------------------------------------------

# 62. Response Models

Responses should expose only fields appropriate for the API consumer.

For example, an authentication response may expose:

``` text
id
fullName
email
role
permissions
accessToken
refreshToken
```

but should never expose:

``` text
password
passwordHash
security secrets
internal authentication data
```

------------------------------------------------------------------------

# 63. Controller Security

Controllers should use permission-based authorization.

Example:

``` java
@PreAuthorize("hasAuthority('BRANCH_CREATE')")
```

Different endpoints may require different permissions:

``` text
BRANCH_VIEW
BRANCH_CREATE
BRANCH_UPDATE
BRANCH_DELETE
BRANCH_REACTIVATE
```

------------------------------------------------------------------------

# 64. Service-Level Security

Controller authorization is not the only security boundary.

Services should still validate:

``` text
Tenant ownership
Branch ownership
Resource relationships
User privileges
Business rules
```

Example:

``` text
User
  |
  +-- Tenant A
       |
       +-- Branch A
            |
            +-- Order A
```

A user from Tenant B must not be able to modify Order A.

------------------------------------------------------------------------

# 65. API Error Categories

Errors should be classified into understandable categories.

### Authentication error

``` text
Invalid or expired authentication
```

### Authorization error

``` text
User does not have required permission
```

### Validation error

``` text
Input is invalid
```

### Not found

``` text
Requested resource does not exist
```

### Conflict

``` text
Operation conflicts with existing state
```

### Business rule error

``` text
Operation violates domain rules
```

### Rate limit

``` text
Too many requests
```

### Internal error

``` text
Unexpected server failure
```

------------------------------------------------------------------------

# 66. Example Error Responses

## 401

``` json
{
  "success": false,
  "message": "Authentication required",
  "data": null
}
```

## 403

``` json
{
  "success": false,
  "message": "You do not have permission to perform this action",
  "data": null
}
```

## 404

``` json
{
  "success": false,
  "message": "Branch not found",
  "data": null
}
```

## 409

``` json
{
  "success": false,
  "message": "Branch code already exists",
  "data": null
}
```

------------------------------------------------------------------------

# 67. Rate Limiting

Sensitive endpoints should be rate limited.

Especially:

``` text
Login
Refresh token
Password reset
OTP
Public registration
Public APIs
```

Example response:

``` http
429 Too Many Requests
```

The exact rate limits should be configured centrally.

------------------------------------------------------------------------

# 68. CORS

The backend should explicitly allow configured frontend origins.

Local development example:

``` text
http://localhost:5173
```

Production origins should be configured through environment variables.

Do not use unrestricted production CORS such as:

``` text
*
```

for authenticated APIs unless there is a deliberate security reason.

------------------------------------------------------------------------

# 69. CSRF

The API uses stateless JWT authentication.

CSRF strategy must remain consistent with how tokens are transported.

If access tokens are sent using:

``` http
Authorization: Bearer ...
```

the API can use a stateless CSRF strategy appropriate to that
architecture.

Do not change the strategy without reviewing the authentication
transport mechanism.

------------------------------------------------------------------------

# 70. Swagger / OpenAPI

The API should expose OpenAPI documentation in development and
controlled production environments.

Swagger should document:

``` text
Endpoints
Parameters
Request models
Response models
Authentication
Authorization
HTTP status codes
```

JWT bearer authentication should be represented in the OpenAPI security
scheme.

------------------------------------------------------------------------

# 71. Bruno Testing Structure

Bruno collections should be organized by module.

Recommended:

``` text
Restaurant ERP/
│
├── Auth/
├── Organization/
├── Branch/
├── User/
├── Employee/
├── Customer/
├── Role/
├── Permission/
├── Role Permission/
├── Subscription/
├── Menu/
├── Recipe/
├── Inventory/
├── Purchase/
├── Vendor/
├── Floor/
├── Table/
├── Reservation/
├── Order/
├── Kitchen/
├── Delivery/
├── Tax/
├── Payment/
├── Expense/
├── Accounting/
├── Attendance/
├── Payroll/
├── Loyalty/
├── Notification/
├── Audit/
├── Report/
└── Settings/
```

------------------------------------------------------------------------

# 72. Bruno Environment Variables

Recommended local variables:

``` text
baseUrl=http://localhost:8080
accessToken=
refreshToken=
organizationId=
branchId=
userId=
employeeId=
customerId=
roleId=
permissionId=
tableId=
orderId=
reservationId=
```

Use environment variables rather than repeatedly hardcoding IDs.

------------------------------------------------------------------------

# 73. Standard Bruno Flow

For a protected module:

``` text
1. Login
2. Save accessToken
3. Create resource
4. Save returned ID
5. Get resource
6. Update resource
7. Search resource
8. Delete resource
9. Verify inactive state
10. Restore resource
11. Verify active state
12. Test unauthorized access
13. Test invalid input
14. Test tenant isolation
```

------------------------------------------------------------------------

# 74. Frontend API Client

The React frontend should use a centralized Axios client.

Conceptual structure:

``` text
src/
└── api/
    ├── axiosClient.js
    ├── authApi.js
    ├── organizationApi.js
    ├── branchApi.js
    └── ...
```

The Axios client should centrally handle:

``` text
Base URL
Authorization
Token refresh
Common errors
Timeout
```

------------------------------------------------------------------------

# 75. Token Refresh Flow

Recommended flow:

``` text
Frontend
   |
   | API request
   v
Backend
   |
   | 401
   v
Axios Interceptor
   |
   | Refresh token
   v
/api/auth/refresh
   |
   v
New Access Token
   |
   v
Retry Original Request
```

If refresh fails:

``` text
Clear authentication state
Redirect to login
```

Avoid infinite refresh loops.

------------------------------------------------------------------------

# 76. API Versioning

The first version may use:

``` text
/api
```

If future breaking versions are required, introduce:

``` text
/api/v1
/api/v2
```

Do not introduce versioning inconsistently across individual modules.

------------------------------------------------------------------------

# 77. Idempotency

Operations involving financial or order processing should consider
idempotency.

Examples:

``` text
Payment
Refund
Order submission
Webhook processing
Purchase receiving
Accounting posting
```

A duplicate request should not accidentally create two payments or
refunds.

An idempotency key may be represented as:

``` http
Idempotency-Key: unique-request-id
```

when implemented.

------------------------------------------------------------------------

# 78. Concurrency

Restaurant systems have concurrent operations.

Examples:

``` text
Two cashiers editing an order
Two users assigning a table
Kitchen updating an order
Manager changing inventory
Multiple POS terminals
```

Services should protect important state transitions from invalid
concurrent updates.

Use appropriate MongoDB atomic operations and domain-level validation.

------------------------------------------------------------------------

# 79. Transaction Strategy

MongoDB transaction usage should be reserved for operations that
genuinely require atomic multi-document changes.

Examples:

``` text
Order + Payment
Stock movement + Inventory update
Purchase receipt + Stock update
Financial posting
```

Do not automatically wrap every service method in a transaction.

------------------------------------------------------------------------

# 80. Search Performance

Search APIs must be designed with indexes in mind.

Common indexed fields may include:

``` text
tenantId
organizationId
branchId
isActive
createdAt
updatedAt
code
status
email
```

Compound indexes should be added for frequent tenant-scoped queries.

------------------------------------------------------------------------

# 81. API Security Checklist

Before considering an endpoint complete:

``` text
[ ] Authentication required where appropriate
[ ] Permission defined
[ ] Tenant isolation verified
[ ] Branch isolation verified
[ ] Input validation implemented
[ ] Unauthorized access tested
[ ] Invalid ID tested
[ ] Not-found behavior tested
[ ] Duplicate/conflict behavior tested
[ ] Soft-delete behavior tested
[ ] Restore behavior tested
[ ] Pagination tested
[ ] Sorting tested
[ ] Search tested
[ ] Sensitive fields excluded
[ ] Logging reviewed
```

------------------------------------------------------------------------

# 82. Realtime Module Checklist

For modules using SSE/WebSocket:

``` text
[ ] Stream endpoint exists
[ ] Authentication is handled
[ ] Tenant isolation is enforced
[ ] Emitter/client registration exists
[ ] Create event exists
[ ] Update event exists
[ ] Delete event exists
[ ] Restore event exists
[ ] Connection cleanup exists
[ ] Frontend reconnect strategy exists
[ ] Duplicate event handling considered
```

------------------------------------------------------------------------

# 83. CRUD Module Completion Checklist

For each normal CRUD module:

``` text
[ ] Domain entity
[ ] Model/DTO
[ ] Repository
[ ] Repository custom interface if required
[ ] Repository custom implementation if required
[ ] Transformer
[ ] Service
[ ] Handler
[ ] Controller
[ ] Validation
[ ] Permission definitions
[ ] Security rules
[ ] Tenant filtering
[ ] Pagination
[ ] Search
[ ] Sorting
[ ] Soft delete
[ ] Restore
[ ] SSE/WebSocket if required
[ ] Swagger documentation
[ ] Bruno collection
```

------------------------------------------------------------------------

# 84. Recommended API Development Order

Build foundational APIs first:

``` text
1. Authentication
2. User
3. Role
4. Permission
5. Role Permission
6. Subscription
7. Organization
8. Branch
9. Employee
10. Customer
```

Then operational APIs:

``` text
11. Menu
12. Recipe
13. Vendor
14. Inventory
15. Purchase
16. Floor
17. Table
18. Reservation
19. Order
20. Kitchen
21. Delivery
22. Tax
23. Payment
```

Then business-support APIs:

``` text
24. Expense
25. Accounting
26. Attendance
27. Payroll
28. Loyalty
29. Notification
30. Audit
31. Report
32. Settings
```

------------------------------------------------------------------------

# 85. Complete High-Level API Map

``` text
/api
│
├── /auth
│
├── /user
├── /employee
├── /customer
│
├── /role
├── /permission
├── /role-permission
│
├── /subscription-plan
│
├── /organization
├── /branch
│
├── /category
├── /menu-item
├── /menu-variant
├── /modifier
├── /modifier-group
├── /menu-item-modifier-group
│
├── /recipe
├── /vendor
├── /inventory
├── /inventory-item
├── /stock
├── /stock-movement
├── /stock-adjustment
├── /stock-transfer
│
├── /purchase
├── /purchase-order
├── /goods-receipt
├── /purchase-return
│
├── /floor
├── /table
├── /table-status-history
├── /table-combination
├── /table-assignment
├── /reservation
│
├── /order
├── /kitchen
├── /delivery
├── /tax
├── /payment-method
│
├── /expense
├── /expense-category
├── /expense-approval
├── /expense-recurring
│
├── /accounting
├── /account
├── /journal-entry
├── /ledger
├── /invoice
│
├── /attendance
├── /payroll
├── /loyalty
├── /notification
├── /audit
├── /report
└── /settings
```

------------------------------------------------------------------------

# 86. Example End-to-End Restaurant Flow

A typical dine-in order can flow through the API as:

``` text
Customer/Waiter
      |
      v
Reservation / Table
      |
      v
POST /api/order
      |
      v
Order Created
      |
      v
Kitchen Ticket
      |
      v
Kitchen Processing
      |
      v
Order Ready
      |
      v
Payment
      |
      v
Order Completed
      |
      +----------------+
      |                |
      v                v
Inventory          Accounting
      |
      v
Stock Movement
```

Realtime:

``` text
Order Created
      |
      +--> Kitchen SSE/WebSocket
      |
      +--> POS
      |
      +--> Waiter UI
      |
      +--> Dashboard
```

------------------------------------------------------------------------

# 87. Example Authentication + Permission Flow

``` text
User
 |
 | Login
 v
POST /api/auth/login
 |
 v
JWT Access Token
 |
 v
Frontend stores authentication state
 |
 | Authorization: Bearer JWT
 v
Protected API
 |
 v
JwtAuthFilter
 |
 v
Authenticated User
 |
 v
Role / Permission Check
 |
 v
Controller
 |
 v
Handler
 |
 v
Service
 |
 v
Tenant Validation
 |
 v
Repository
 |
 v
MongoDB
```

------------------------------------------------------------------------

# 88. API Design Principles

The Restaurant ERP API follows these principles:

1.  APIs should be predictable.
2.  Authentication and authorization are separate.
3.  Tenant isolation is mandatory.
4.  Business rules belong in services.
5.  Controllers remain thin.
6.  DTO/model contracts should remain stable.
7.  Persistence entities should not automatically become public API
    contracts.
8.  Pagination should be used for large collections.
9.  Search endpoints should be indexed.
10. Soft deletion should preserve business history.
11. Financial records require special handling.
12. Realtime APIs should be used where realtime behavior adds
    operational value.
13. API errors should be consistent.
14. Sensitive information must never be returned.
15. Security must be enforced server-side.
16. APIs should be testable through Bruno.
17. APIs should be documented through OpenAPI/Swagger.
18. Breaking changes should be controlled and versioned.

------------------------------------------------------------------------

# 89. Final API Architecture

``` text
                         CLIENTS
                           |
          +----------------+----------------+
          |                |                |
        React            POS             Mobile
          |                |                |
          +----------------+----------------+
                           |
                    REST / SSE / WS
                           |
                           v
                    Spring Security
                           |
                 JWT Authentication
                           |
                 Permission Authorization
                           |
                           v
                      Controller
                           |
                           v
                        Handler
                           |
                           v
                        Service
                           |
                 +---------+---------+
                 |                   |
          Tenant Validation      Business Rules
                 |                   |
                 +---------+---------+
                           |
                           v
                      Transformer
                           |
                           v
                      Repository
                           |
                           v
                        MongoDB
                           |
                           v
                   Shared Database
                           |
                     tenantId Scope
```

------------------------------------------------------------------------

# 90. Final API Completion Standard

An API is considered production-ready only when it has:

``` text
Authentication
Authorization
Tenant Isolation
Validation
Business Rules
Consistent Response
Consistent Errors
Pagination
Search
Sorting
Soft Delete
Restore
Logging
Swagger Documentation
Bruno Tests
Security Tests
Realtime Support Where Required
```

The goal is not simply to make an endpoint return data.

The goal is to create a **secure, tenant-aware, maintainable, testable,
realtime-capable Restaurant ERP API** that can support multiple
restaurants, branches, POS terminals, employees, customers, and future
integrations.
