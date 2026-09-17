# Restaurant ERP — Authentication Setup

## 1. Purpose

The authentication system provides secure identity management for the Restaurant ERP SaaS platform.

It is responsible for:

- User registration and login
- BCrypt password hashing
- JWT access tokens
- JWT refresh tokens
- JWT validation
- Refresh-token lifecycle
- Logout and token invalidation
- Role and permission integration
- Tenant-aware authentication
- Authentication rate limiting
- Security auditing

Authentication answers **"Who are you?"**.

Authorization answers **"What are you allowed to do?"**.

Tenant isolation answers **"Which restaurant's data can you access?"**.

---

## 2. High-Level Authentication Architecture

```text
React Frontend
      │
      │ Login / API Request
      ▼
AuthController
      │
      ▼
AuthHandler
      │
      ▼
AuthService
      │
 ┌────┼──────────────┐
 ▼    ▼              ▼
User  BCrypt       JwtService
Repo  Encoder          │
 │                     ▼
 ▼              Access / Refresh
MongoDB               Tokens
```

Protected requests:

```text
React
  │
  │ Authorization: Bearer <access-token>
  ▼
JwtAuthFilter
  │
  ▼
JWT Validation
  │
  ▼
SecurityContext
  │
  ▼
Role + Permission Check
  │
  ▼
Tenant Authorization
  │
  ▼
Controller
```

---

## 3. Authentication Package Structure

```text
auth/
│
├── controller/
│   └── AuthController.java
│
├── domain/
│   ├── User.java
│   └── RefreshToken.java
│
├── model/
│   ├── LoginModel.java
│   ├── RegisterModel.java
│   ├── RefreshTokenModel.java
│   ├── LogoutModel.java
│   └── AuthResponseModel.java
│
├── repository/
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
│
├── service/
│   ├── AuthService.java
│   ├── JwtService.java
│   └── RefreshTokenService.java
│
├── transformer/
│   └── AuthTransformer.java
│
└── handler/
    └── AuthHandler.java
```

Security-specific infrastructure may also live under:

```text
config/
├── SecurityConfig.java
├── JwtConfig.java
└── JwtAuthFilter.java
```

---

## 4. User Authentication Entity

The authenticated identity is based on the application `User`.

Conceptually:

```text
User
│
├── id
├── fullName
├── email
├── password
├── role
├── tenantId
├── tokenVersion
├── isActive
├── createdAt
└── updatedAt
```

The user record establishes:

```text
Identity
Tenant
Role
Account status
Token version
```

Passwords must never be exposed through API models.

---

## 5. Password Security

Passwords are stored using BCrypt.

```text
Plain Password
      │
      ▼
BCryptPasswordEncoder
      │
      ▼
Password Hash
      │
      ▼
MongoDB
```

Login verification:

```text
Entered Password
      │
      ▼
BCrypt.matches(...)
      │
      ▼
Stored Password Hash
```

Never:

- Store plain-text passwords
- Return passwords in API responses
- Log passwords
- Put passwords inside JWT claims

---

## 6. Registration Flow

```text
POST /api/auth/register
          │
          ▼
AuthController
          │
          ▼
AuthHandler
          │
          ▼
AuthService
          │
          ├── Validate request
          ├── Check email/account rules
          ├── Hash password
          ├── Assign controlled role
          ├── Establish tenant
          └── Save User
                    │
                    ▼
                 MongoDB
```

The client must not be trusted to assign privileged roles such as:

```text
SUPER_ADMIN
RESTAURANT_OWNER
```

Privileged account creation should follow the platform's controlled onboarding process.

---

## 7. Login Flow

Endpoint:

```text
POST /api/auth/login
```

Flow:

```text
Login Request
     │
     ▼
AuthController
     │
     ▼
AuthHandler
     │
     ▼
AuthService
     │
     ▼
Find User
     │
     ▼
Check isActive
     │
     ▼
Verify BCrypt Password
     │
     ▼
Load Role / Permissions
     │
     ▼
Generate Access Token
     │
     ▼
Generate Refresh Token
     │
     ▼
Persist Refresh Token
     │
     ▼
AuthResponseModel
```

---

## 8. Authentication Response

The current authentication response can contain:

```text
id
fullName
email
roleId
role
referralCode
accessToken
refreshToken
permissions
```

Example:

```json
{
  "id": "...",
  "fullName": "Restaurant Owner",
  "email": "owner@example.com",
  "roleId": "...",
  "role": "RESTAURANT_OWNER",
  "referralCode": "...",
  "accessToken": "...",
  "refreshToken": "...",
  "permissions": [
    "BRANCH_VIEW",
    "BRANCH_CREATE",
    "ORDER_VIEW"
  ]
}
```

---

## 9. JWT Access Token

The access token authenticates normal protected API requests.

Client requests use:

```http
Authorization: Bearer <access-token>
```

Useful JWT claims may include:

```text
sub
userId
tenantId
role
tokenVersion
iat
exp
```

Never put sensitive information such as passwords into JWT claims.

---

## 10. JWT Refresh Token

Refresh tokens allow the client to obtain a new access token without logging in again.

```text
Access Token Expired
        │
        ▼
Refresh Token
        │
        ▼
POST /api/auth/refresh
        │
        ▼
Validate Refresh Token
        │
        ▼
Validate User
        │
        ▼
Issue New Access Token
```

Refresh tokens should have a longer lifetime than access tokens.

---

## 11. Refresh Token Persistence

Refresh tokens are persisted so the server can revoke and validate them.

Conceptually:

```text
RefreshToken
│
├── id
├── token
├── userId
├── expiryDate
├── revoked
├── createdAt
└── ...
```

Persistence allows the application to:

- Revoke sessions
- Detect expired tokens
- Rotate refresh tokens
- Support logout
- Clean up expired records
- Perform security invalidation

---

## 12. Refresh Token Rotation

Recommended lifecycle:

```text
Old Refresh Token
       │
       ▼
Validate
       │
       ▼
Revoke Old Token
       │
       ▼
Create New Refresh Token
       │
       ▼
Create New Access Token
```

This limits the useful lifetime of a compromised refresh token.

---

## 13. Token Version

The `User` contains a token-version value.

Example:

```text
User.tokenVersion = 1
```

JWT contains:

```text
tokenVersion = 1
```

When all existing tokens need to be invalidated:

```text
User.tokenVersion
       │
       ▼
       2
```

Old tokens contain:

```text
tokenVersion = 1
```

Therefore:

```text
Old JWT
   ↓
Version mismatch
   ↓
Rejected
```

This provides application-level token invalidation.

---

## 14. Logout

Logout should invalidate server-side authentication state.

```text
POST /api/auth/logout
          │
          ▼
AuthService
          │
          ├── Revoke Refresh Token
          │
          └── Invalidate token version/session
                    │
                    ▼
                 MongoDB
```

Frontend local-storage/session cleanup alone is not sufficient to implement server-side logout.

---

## 15. JWT Authentication Filter

`JwtAuthFilter` runs before protected controllers.

```text
HTTP Request
     │
     ▼
Read Authorization Header
     │
     ▼
Bearer Token?
  ┌──┴──┐
 No     Yes
 │       │
 ▼       ▼
Continue Extract JWT
Request    │
           ▼
      Validate Signature
           │
           ▼
      Validate Expiration
           │
           ▼
      Extract User Identity
           │
           ▼
        Load User
           │
           ▼
      Check Account Active
           │
           ▼
      Check Token Version
           │
           ▼
    Create Authentication
           │
           ▼
     SecurityContext
```

The filter should focus on authentication and must not contain business logic.

---

## 16. SecurityContext

After successful JWT validation:

```text
JwtAuthFilter
      │
      ▼
Authentication
      │
      ▼
SecurityContextHolder
```

The authentication context should provide information needed by the application, such as:

```text
userId
email
role
authorities
tenantId
```

---

## 17. Role and Permission Architecture

```text
User
 │
 ▼
Role
 │
 ▼
RolePermission
 │
 ▼
Permission
```

Example:

```text
Restaurant Owner
      │
      ▼
Role
      │
      ├── ORGANIZATION_VIEW
      ├── BRANCH_VIEW
      ├── BRANCH_CREATE
      ├── MENU_VIEW
      ├── ORDER_VIEW
      └── REPORT_VIEW
```

Authentication loads the identity.

Authorization uses the role and permissions.

---

## 18. Permission Authorities

During authentication:

```text
User
  │
  ▼
Role
  │
  ▼
RolePermission
  │
  ▼
Permission
  │
  ▼
Spring Security Authorities
```

Example authorities:

```text
BRANCH_VIEW
BRANCH_CREATE
BRANCH_UPDATE
BRANCH_DELETE
ORDER_VIEW
```

A protected controller can enforce permissions with Spring Security:

```java
@PreAuthorize("hasAuthority('BRANCH_CREATE')")
```

---

## 19. Tenant-Aware Authentication

This is a multi-tenant SaaS application.

Authentication must establish the user's tenant.

```text
JWT
 │
 └── tenantId
       │
       ▼
Authenticated User
       │
       ▼
Tenant Context
```

Tenant-owned data must always be accessed within the authenticated tenant boundary.

---

## 20. Tenant Security Rule

Never trust a client-provided `tenantId` to determine data ownership.

Bad conceptual flow:

```text
Request tenantId
      ↓
Query database
```

Correct conceptual flow:

```text
Authenticated User
       │
       ▼
Authenticated Tenant
       │
       ▼
Tenant-aware Query
       │
       ▼
Requested Resource
```

Example:

```java
findByIdAndTenantId(resourceId, tenantId)
```

or equivalent custom repository logic.

---

## 21. Authentication vs Resource Authorization

Being authenticated does not mean a user can access every resource.

The complete authorization decision is:

```text
Authenticated User
       +
Tenant
       +
Role / Permission
       +
Resource Ownership
       =
Authorized Operation
```

This is especially important for:

```text
Organizations
Branches
Employees
Customers
Menu
Tables
Orders
Inventory
Purchases
Expenses
Reports
```

---

## 22. User Types

The system supports different account/business identities.

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

Employees and customers should remain separate business concepts even though both may authenticate through `User`.

---

## 23. Employee Authentication

```text
User
 │
 ▼
Employee
 │
 ├── Role
 ├── Branch
 └── Employment Data
```

Authentication identity and employee business data should remain separated.

---

## 24. Customer Authentication

```text
User
 │
 ▼
Customer
```

Customer accounts must not receive employee or administrative permissions unless the application's business rules explicitly provide them.

---

## 25. Account Status

Authentication must verify:

```text
User.isActive
```

Lifecycle:

```text
Active
  │
  ▼
Deactivated
  │
  ▼
Authentication Rejected
```

A deactivated user must not continue to authenticate.

---

## 26. Authentication Endpoints

Core endpoints:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

Possible future endpoints:

```text
POST /api/auth/change-password
POST /api/auth/forgot-password
POST /api/auth/reset-password
POST /api/auth/verify-email
POST /api/auth/resend-verification
```

Only implemented endpoints should be exposed.

---

## 27. Public Endpoints

Authentication endpoints generally require public access:

```text
/api/auth/login
/api/auth/register
/api/auth/refresh
```

Other endpoints should be public only when explicitly required by the business flow.

Examples may include:

```text
Subscription plan discovery
Email verification
Password reset
Public customer registration
```

---

## 28. Security Configuration

`SecurityConfig` is responsible for:

- HTTP security
- JWT authentication
- JWT filter registration
- Public endpoints
- Protected endpoints
- Method authorization
- Session policy
- Password encoder
- CORS
- CSRF strategy where applicable

For a stateless JWT API:

```text
SessionCreationPolicy.STATELESS
```

should be used.

---

## 29. Password Encoder Bean

The application should register a password encoder as a Spring bean.

Conceptually:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Services should depend on:

```text
PasswordEncoder
```

rather than repeatedly constructing password encoders.

---

## 30. JWT Configuration

JWT configuration should be externalized.

Example:

```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION}
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION}
```

Never commit production secrets into Git.

---

## 31. Secret Management

Secrets must come from environment variables or a secure secret manager.

Examples:

```text
JWT_SECRET
MONGODB_URI
MAIL_PASSWORD
PAYMENT_SECRET
OAUTH_CLIENT_SECRET
```

Never commit these values directly to the repository.

---

## 32. Refresh Token Cleanup

Expired refresh tokens should be periodically removed or revoked.

```text
RefreshTokenCleanupScheduler
           │
           ▼
Find Expired Tokens
           │
           ▼
Delete / Revoke
```

This prevents unnecessary growth of the refresh-token collection.

---

## 33. Rate Limiting

Authentication endpoints should be protected against brute-force attacks.

Recommended targets:

```text
/login
/register
/refresh
/forgot-password
```

Flow:

```text
Client
  │
  ▼
Rate Limiter
  │
  ├── Allowed ──► Authentication
  │
  └── Limited ─► Reject / Delay
```

Limits should be configurable.

---

## 34. Failed Login Protection

A future implementation may track failed login attempts.

```text
Login Failure
      │
      ▼
Failure Counter
      │
      ▼
Threshold?
   ┌──┴──┐
  No    Yes
   │      │
   ▼      ▼
Continue Temporary
         Lock / Challenge
```

Account-locking mechanisms must avoid allowing attackers to trivially lock legitimate users.

---

## 35. CORS

Development may allow the configured Vite frontend origin, for example:

```text
http://localhost:5173
```

Production should allow only trusted deployed frontend origins.

Avoid unrestricted production CORS unless explicitly required by the security architecture.

---

## 36. CSRF

The CSRF strategy depends on token transport.

For bearer access tokens sent through:

```http
Authorization: Bearer <token>
```

the CSRF model differs from cookie-based authentication.

If refresh tokens are stored in cookies, the cookie-based CSRF implications must be handled explicitly.

The frontend and backend must use one consistent token-transport strategy.

---

## 37. Frontend Authentication State

Conceptually:

```text
Authentication State
│
├── user
├── role
├── permissions
├── accessToken
└── refresh/session state
```

The frontend uses this state to:

- Show authenticated UI
- Hide unavailable actions
- Attach access tokens
- Refresh authentication
- Redirect unauthenticated users

Frontend checks are for user experience only.

---

## 38. Axios Authentication Flow

```text
React
  │
  ▼
Axios Client
  │
  ├── Add Authorization Header
  │
  ▼
Spring Boot
```

When an access token expires:

```text
401
 │
 ▼
Refresh Token
 │
 ▼
New Access Token
 │
 ▼
Retry Original Request
```

If refresh fails:

```text
Clear Authentication
       │
       ▼
Redirect to Login
```

The refresh mechanism must prevent infinite retry loops.

---

## 39. Current User Endpoint

The `/me` endpoint can reconstruct the authenticated user's current state.

```text
GET /api/auth/me
```

Flow:

```text
JWT
 │
 ▼
Authenticated Identity
 │
 ▼
Load User
 │
 ▼
Return User Model
```

This is useful after a browser reload.

---

## 40. Authentication Errors

Typical authentication failures include:

```text
Invalid credentials
Account inactive
Invalid access token
Expired access token
Invalid refresh token
Revoked refresh token
Token version mismatch
Unauthorized
Forbidden
```

Internal stack traces and database details must not be returned to clients.

---

## 41. HTTP 401 vs 403

### 401 Unauthorized

The request is not successfully authenticated.

Examples:

```text
Missing token
Invalid token
Expired token
Invalid credentials
```

### 403 Forbidden

The user is authenticated but does not have permission.

Example:

```text
Cashier
   │
   ▼
POST /api/organization
   │
   ▼
No ORGANIZATION_CREATE
   │
   ▼
403 Forbidden
```

---

## 42. Password Change

Authenticated password change:

```text
Authenticated User
       │
       ▼
Verify Current Password
       │
       ▼
Validate New Password
       │
       ▼
Hash New Password
       │
       ▼
Save User
       │
       ▼
Invalidate Existing Tokens
```

Existing sessions should be invalidated when required by the security policy.

---

## 43. Forgot Password

Future reset flow:

```text
Email
  │
  ▼
Find Account
  │
  ▼
Generate Short-Lived Reset Token
  │
  ▼
Send Reset Link
  │
  ▼
User Opens Link
  │
  ▼
Validate Token
  │
  ▼
Set New Password
  │
  ▼
Invalidate Existing Tokens
```

Reset tokens should be:

- Short-lived
- Single-use
- Securely generated
- Invalidated after use

Public responses should avoid revealing whether a particular email exists.

---

## 44. Email Verification

If enabled:

```text
Registration
     │
     ▼
Verification Token
     │
     ▼
Email
     │
     ▼
User Clicks Link
     │
     ▼
Validate Token
     │
     ▼
Email Verified
```

Authentication can require verification where business rules demand it.

---

## 45. Authentication Audit Events

Useful audit events include:

```text
USER_LOGIN_SUCCESS
USER_LOGIN_FAILED
USER_LOGOUT
PASSWORD_CHANGED
PASSWORD_RESET
REFRESH_TOKEN_CREATED
REFRESH_TOKEN_REVOKED
ACCOUNT_ACTIVATED
ACCOUNT_DEACTIVATED
ROLE_CHANGED
PERMISSIONS_CHANGED
```

Never log:

```text
Plain passwords
Full access tokens
Full refresh tokens
JWT secrets
Database passwords
```

---

## 46. Authentication Database Collections

Core collections:

```text
users
refresh_tokens
```

Related collections:

```text
roles
permissions
role_permissions
organizations
branches
```

The actual collection names depend on MongoDB mappings.

---

## 47. Authentication Indexes

Likely indexes include:

```text
users.email
users.tenantId
users.tenantId + email

refresh_tokens.token
refresh_tokens.userId
refresh_tokens.expiryDate
```

The final uniqueness strategy must match the application's account model.

---

## 48. Email Uniqueness

The system must define whether email uniqueness is:

```text
Global:
email
```

or:

```text
Per Tenant:
tenantId + email
```

The same rule must be used consistently for:

```text
Registration
Login
Account lookup
Account management
Database indexes
```

---

## 49. Authentication Sequence

```text
┌────────┐       ┌──────────────┐       ┌────────────┐
│ Client │       │ Auth Service │       │  MongoDB   │
└───┬────┘       └──────┬───────┘       └─────┬──────┘
    │                    │                     │
    │ Login              │                     │
    ├───────────────────►│                     │
    │                    │ Find User           │
    │                    ├────────────────────►│
    │                    │◄────────────────────┤
    │                    │                     │
    │                    │ Verify BCrypt       │
    │                    │                     │
    │                    │ Generate JWT        │
    │                    │                     │
    │                    │ Save Refresh Token  │
    │                    ├────────────────────►│
    │                    │◄────────────────────┤
    │                    │                     │
    │ AuthResponse       │                     │
    │◄───────────────────┤                     │
```

---

## 50. Complete Protected Request

```text
                    React
                      │
                      ▼
              Axios Authentication
                      │
                      ▼
             Authorization Header
                      │
                      ▼
              Spring Security
                      │
                      ▼
               JwtAuthFilter
                      │
                      ▼
               JWT Validation
                      │
                      ▼
               SecurityContext
                      │
                      ▼
              Role + Permission
                      │
                      ▼
               Tenant Context
                      │
                      ▼
              Resource Ownership
                      │
                ┌─────┴─────┐
                │           │
             Allowed       Denied
                │           │
                ▼           ▼
            Controller     403/401
                │
                ▼
             Handler
                │
                ▼
             Service
                │
                ▼
        Tenant-aware Repository
                │
                ▼
             MongoDB
```

---

## 51. Authentication Design Rules

1. Never store plain-text passwords.
2. Never return passwords in API responses.
3. Never put passwords into JWT claims.
4. Never trust client-provided roles.
5. Never trust client-provided tenant ownership.
6. Always validate JWT signature and expiration.
7. Validate token version where enabled.
8. Validate refresh tokens server-side.
9. Revoke/rotate refresh tokens according to the security strategy.
10. Check `isActive` before authenticating.
11. Enforce authorization on the backend.
12. Apply rate limiting to authentication endpoints.
13. Keep JWT secrets outside source control.
14. Never log authentication secrets.
15. Keep authentication separate from business logic.
16. Use tenant-aware queries for tenant-owned data.
17. Invalidate existing sessions after security-sensitive credential changes where required.
18. Keep public endpoints explicitly configured.
19. Never expose stack traces through authentication APIs.
20. Frontend permission checks must never be the security boundary.

---

## 52. Final Authentication Architecture

```text
                         ┌─────────────────────┐
                         │      React App      │
                         └──────────┬──────────┘
                                    │
                              Login / API
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   AuthController    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    AuthHandler      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     AuthService     │
                         └──────────┬──────────┘
                                    │
             ┌──────────────────────┼─────────────────────┐
             │                      │                     │
             ▼                      ▼                     ▼
       UserRepository       PasswordEncoder        JwtService
             │                                            │
             ▼                                            ▼
          MongoDB                                  Access Token
             │                                    Refresh Token
             │                                            │
             └──────────────────┬─────────────────────────┘
                                ▼
                       RefreshTokenService
                                │
                                ▼
                             MongoDB

Protected Request
        │
        ▼
   JwtAuthFilter
        │
        ▼
   SecurityContext
        │
        ▼
 Role + Permission
        │
        ▼
 Tenant Authorization
        │
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
 Tenant-aware Repository
        │
        ▼
 MongoDB
```

---

## 53. Core Security Chain

```text
PASSWORD
   ↓
BCrypt HASH
   ↓
LOGIN
   ↓
JWT ACCESS + REFRESH
   ↓
JWT FILTER
   ↓
AUTHENTICATION
   ↓
ROLE
   ↓
PERMISSIONS
   ↓
TENANT CONTEXT
   ↓
RESOURCE AUTHORIZATION
   ↓
BUSINESS OPERATION
```

The authentication system therefore establishes a secure identity while the authorization and tenant-isolation layers ensure that authenticated users can perform only the operations and access only the data permitted by the application's security model.
