# Frontend Configuration

This document describes the frontend development environment, project configuration, dependencies, environment variables, API communication, styling, routing, and development workflow for the Restaurant Management System.

The frontend is built with React and Vite and communicates with the Spring Boot backend through REST APIs and real-time communication endpoints.

---

## 1. Technology Stack

The frontend uses the following technologies:

| Technology         | Purpose                           |
| ------------------ | --------------------------------- |
| React              | UI development                    |
| Vite               | Development server and build tool |
| JavaScript         | Application programming           |
| Tailwind CSS       | UI styling                        |
| Axios              | HTTP communication                |
| React Toastify     | Notifications                     |
| Lucide React       | Icons                             |
| Server-Sent Events | Real-time server updates          |
| WebSocket / SockJS | Real-time communication           |

---

## 2. Prerequisites

Before running the frontend, install:

* Node.js
* npm
* Git
* Visual Studio Code or another JavaScript IDE

Verify the installation:

```bash
node --version
npm --version
```

Recommended Node.js version should be an active LTS release compatible with the project's dependencies.

---

## 3. Frontend Project Structure

The frontend follows a feature-oriented structure.

```text
frontend/
│
├── public/
│
├── src/
│   │
│   ├── assets/
│   │
│   ├── components/
│   │   ├── common/
│   │   ├── layout/
│   │   ├── modal/
│   │   └── table/
│   │
│   ├── pages/
│   │   ├── auth/
│   │   ├── dashboard/
│   │   ├── organization/
│   │   ├── branch/
│   │   ├── subscription/
│   │   ├── settings/
│   │   └── ...
│   │
│   ├── services/
│   │   ├── axiosClient.js
│   │   └── ...
│   │
│   ├── hooks/
│   │
│   ├── context/
│   │
│   ├── utils/
│   │
│   ├── routes/
│   │
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
│
├── .env
├── .env.example
├── .gitignore
├── index.html
├── package.json
├── package-lock.json
├── vite.config.js
└── README.md
```

The exact directory structure may evolve as additional modules are implemented.

---

## 4. Creating the Frontend

A new React frontend can be created using Vite:

```bash
npm create vite@latest frontend
```

Select:

```text
Framework: React
Variant: JavaScript
```

Then install dependencies:

```bash
cd frontend
npm install
```

---

## 5. Required Dependencies

Install the main frontend dependencies:

```bash
npm install axios react-router-dom react-toastify lucide-react
```

Install Tailwind CSS according to the version used by the project.

For a Tailwind-based Vite setup, keep the Tailwind configuration consistent with the installed Tailwind version rather than mixing configuration patterns from different major versions.

---

## 6. Environment Configuration

Environment-specific configuration should not be hardcoded throughout the application.

Create:

```text
.env
```

Example:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

For real-time endpoints:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_SSE_BASE_URL=http://localhost:8080/api
```

The `.env` file should not contain secrets that can be exposed to the browser.

Vite only exposes variables prefixed with:

```text
VITE_
```

to frontend code.

---

## 7. Environment Example File

Create:

```text
.env.example
```

Example:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_SSE_BASE_URL=http://localhost:8080/api
```

Developers can copy the example file into their local environment:

```bash
copy .env.example .env
```

On Linux/macOS:

```bash
cp .env.example .env
```

---

## 8. Axios Configuration

All REST API communication should use a centralized Axios client instead of creating separate Axios configurations throughout the application.

Example:

```javascript
import axios from "axios";

const axiosClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("accessToken");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

export default axiosClient;
```

This provides a centralized location for:

* API base URL
* Authentication headers
* Request configuration
* Future token refresh handling
* Global request behavior

---

## 9. API Communication

Frontend modules should communicate with the backend through the centralized Axios client.

Example:

```javascript
import axiosClient from "../../services/axiosClient";

export const getOrganizations = async (params) => {
    const response = await axiosClient.get("/organization/search", {
        params,
    });

    return response.data;
};
```

Avoid writing:

```javascript
axios.get("http://localhost:8080/api/organization");
```

directly inside components.

The API base URL should remain centralized.

---

## 10. Authentication Storage

After successful authentication, the frontend receives authentication information from the backend.

The application may store:

```text
accessToken
refreshToken
user
permissions
role
```

Example:

```javascript
localStorage.setItem("accessToken", response.data.data.accessToken);
localStorage.setItem("refreshToken", response.data.data.refreshToken);
localStorage.setItem(
    "user",
    JSON.stringify(response.data.data)
);
```

Authentication-related data should only be stored according to the application's security requirements.

Access tokens should never be logged to the browser console.

---

## 11. Authentication Flow

The general authentication flow is:

```text
Login Page
     |
     v
Authentication API
     |
     v
Spring Security
     |
     v
JWT Access Token
     |
     v
Frontend Storage
     |
     v
Axios Interceptor
     |
     v
Protected API
```

For authenticated requests:

```text
Authorization: Bearer <access-token>
```

is added automatically by the Axios client.

---

## 12. Routing

Application routes should be managed centrally.

Example:

```javascript
import { BrowserRouter, Routes, Route } from "react-router-dom";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />

                <Route
                    path="/dashboard"
                    element={<Dashboard />}
                />

                <Route
                    path="/organizations"
                    element={<Organizations />}
                />

                <Route
                    path="/branches"
                    element={<Branches />}
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
```

Protected routes should be handled through an authentication or permission guard.

---

## 13. Permission-Based UI

The frontend supports permission-based access control.

Example permissions:

```text
ORGANIZATION_CREATE
ORGANIZATION_VIEW
ORGANIZATION_UPDATE
ORGANIZATION_DELETE
ORGANIZATION_REACTIVATE

BRANCH_CREATE
BRANCH_VIEW
BRANCH_UPDATE
BRANCH_DELETE
BRANCH_REACTIVATE
```

Permissions can be loaded after authentication and stored in the application's authentication state.

UI elements can then be conditionally rendered.

Example:

```javascript
{hasPermission("ORGANIZATION_CREATE") && (
    <button>
        Create Organization
    </button>
)}
```

Frontend permission checks improve the user interface experience, but they must not be treated as the application's security boundary.

The backend must independently enforce authorization.

---

## 14. Layout

The application uses a common dashboard layout containing components such as:

```text
Dashboard Layout
│
├── Sidebar
├── Header
├── Main Content
└── Notifications
```

The sidebar is responsible for navigation between application modules.

Navigation items can be filtered according to the authenticated user's permissions.

---

## 15. Responsive Design

The frontend is designed for:

* Desktop
* Laptop
* Tablet
* Mobile

Tailwind CSS responsive utilities should be used instead of maintaining separate desktop and mobile applications.

Example:

```jsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
    ...
</div>
```

The interface should remain usable on smaller screens, particularly for restaurant operations such as:

* POS
* Tables
* Orders
* Kitchen
* Reservations
* Inventory

---

## 16. Tailwind CSS

Tailwind CSS is used for application styling.

Common utility classes are used directly in React components.

Example:

```jsx
<button
    className="
        px-4
        py-2
        rounded-lg
        font-medium
        transition
        hover:opacity-90
    "
>
    Save
</button>
```

Reusable UI components should be created when the same visual pattern is used across multiple modules.

---

## 17. Notifications

React Toastify is used for user notifications.

Example:

```javascript
import { toast } from "react-toastify";

toast.success("Organization created successfully");
```

Error example:

```javascript
toast.error("Unable to create organization");
```

The application should use consistent notification messages throughout the frontend.

---

## 18. Real-Time Updates

The frontend uses Server-Sent Events and WebSocket communication for selected modules.

### SSE

Example endpoint:

```text
/api/organization/stream
```

The frontend can establish an SSE connection:

```javascript
const eventSource = new EventSource(
    `${import.meta.env.VITE_SSE_BASE_URL}/organization/stream`
);

eventSource.addEventListener("organization-created", (event) => {
    const organization = JSON.parse(event.data);

    // Update the UI
});
```

The same approach can be used for modules such as:

```text
Organization
Branch
Permission
Table
```

---

## 19. WebSocket

WebSocket communication is used where bidirectional real-time communication is required.

SockJS can be used to establish compatibility with the backend WebSocket configuration.

The frontend should keep WebSocket connection logic outside individual UI components whenever possible.

A shared service or hook can manage:

* Connection
* Subscription
* Messages
* Reconnection
* Cleanup

---

## 20. Component Design

Components should have a single clear responsibility.

For example:

```text
BranchPage
│
├── BranchToolbar
├── BranchTable
├── BranchModal
└── Pagination
```

The page should coordinate the workflow while reusable components should handle individual UI responsibilities.

Avoid putting large amounts of API, filtering, pagination, and modal logic directly into one component.

---

## 21. Page and Module Pattern

A typical module can follow this structure:

```text
organization/
│
├── OrganizationPage.jsx
├── OrganizationTable.jsx
├── OrganizationModal.jsx
├── organizationService.js
└── components/
```

The exact structure can be adjusted as the frontend grows.

The main goal is to keep business modules independent and maintainable.

---

## 22. API Error Handling

API errors should be handled consistently.

Example:

```javascript
try {
    const response = await axiosClient.post(
        "/organization",
        payload
    );

    return response.data;
} catch (error) {
    const message =
        error.response?.data?.message ||
        "Something went wrong";

    toast.error(message);

    throw error;
}
```

Common HTTP responses include:

```text
200 OK
201 CREATED
400 BAD REQUEST
401 UNAUTHORIZED
403 FORBIDDEN
404 NOT FOUND
409 CONFLICT
500 INTERNAL SERVER ERROR
```

Authentication-related errors should be handled centrally where possible.

---

## 23. Pagination

Large datasets should use server-side pagination.

Example request:

```text
GET /organization/search?page=0&size=20
```

Additional query parameters can be used for:

```text
search
sort
direction
isActive
```

Example:

```text
GET /organization/search?page=0&size=20&sort=createdAt&direction=desc
```

The frontend pagination component should consume the backend's pagination response rather than loading the complete dataset unnecessarily.

---

## 24. Search and Filtering

List pages should support server-side search and filtering where appropriate.

Example:

```text
Organization
├── Name
├── Email
├── City
└── Status
```

For branches:

```text
Branch
├── Branch Name
├── Branch Code
├── City
├── Phone
├── Organization
└── Status
```

Search requests should be debounced where necessary to avoid excessive API requests.

---

## 25. Soft Delete and Restore

The frontend should distinguish between:

```text
Active
Inactive
```

records.

Delete operations should follow the backend's soft-delete behavior where applicable.

Example UI actions:

```text
Active Record
    |
    └── Deactivate

Inactive Record
    |
    └── Reactivate
```

Permanent deletion should only be exposed where the backend explicitly supports it.

---

## 26. Loading States

Every API-driven page should provide an appropriate loading state.

Example:

```jsx
{loading ? (
    <LoadingSpinner />
) : (
    <OrganizationTable data={organizations} />
)}
```

Loading states should be used for:

* Initial page loading
* Table loading
* Form submission
* Search
* Pagination
* Delete/restore operations

---

## 27. Form Handling

Forms should maintain clear separation between:

```text
Form State
Validation
API Request
Response Handling
UI Feedback
```

Example:

```text
OrganizationModal
        |
        v
Validate Form
        |
        v
organizationService
        |
        v
POST /organization
        |
        v
Success / Error
        |
        v
Update UI
```

---

## 28. Realtime UI Updates

Where SSE or WebSocket events are available, the frontend should update the current data instead of forcing a complete page reload.

For example:

```text
Create Organization
        |
        v
Backend saves record
        |
        v
SSE Event
        |
        v
Frontend receives event
        |
        v
Organization table updates
```

The same principle applies to update, delete, restore, and status-change events.

---

## 29. Development Server

Start the frontend with:

```bash
npm run dev
```

For LAN development, Vite can be configured to listen on all network interfaces.

Example:

```javascript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
    plugins: [react()],
    server: {
        host: true,
        port: 5173,
    },
});
```

The frontend can then be accessed from another device on the same network using the development machine's local IP address.

---

## 30. Production Build

Create a production build using:

```bash
npm run build
```

The generated production files are placed in:

```text
dist/
```

Preview the production build locally:

```bash
npm run preview
```

The `dist/` directory should not normally be committed to Git.

---

## 31. Git Configuration

The following files and directories should generally be excluded from version control:

```text
node_modules/
dist/
.env
.env.local
.env.*.local
```

Example `.gitignore`:

```gitignore
node_modules/
dist/
.env
.env.local
.env.*.local
```

The `.env.example` file should remain committed so that developers know which environment variables are required.

---

## 32. Development Workflow

A typical frontend development workflow is:

```text
Create / Update Feature
        |
        v
Create Page / Components
        |
        v
Create API Service
        |
        v
Connect Backend API
        |
        v
Handle Loading / Error States
        |
        v
Add Permissions
        |
        v
Add Realtime Updates
        |
        v
Test with Backend
        |
        v
Responsive Testing
        |
        v
Production Build
```

---

## 33. Module Development Guidelines

When creating a new frontend module, follow this sequence:

### Step 1: Create the Page

```text
ModulePage.jsx
```

### Step 2: Create Reusable Components

```text
ModuleTable.jsx
ModuleModal.jsx
ModuleToolbar.jsx
```

### Step 3: Create the API Service

```text
moduleService.js
```

### Step 4: Connect the Backend

Use the centralized Axios client.

### Step 5: Add Authentication

Ensure protected APIs send the JWT access token.

### Step 6: Add Permissions

Control available actions according to the authenticated user's permissions.

### Step 7: Add Pagination and Filtering

Use backend-supported search, filtering, sorting, and pagination.

### Step 8: Add Real-Time Updates

Use SSE or WebSocket where the backend exposes real-time events.

### Step 9: Test

Test:

* Create
* View
* Update
* Delete/deactivate
* Restore/reactivate
* Search
* Filtering
* Pagination
* Permissions
* Responsive layout
* Real-time updates

---

## 34. Code Organization Principles

Frontend code should follow these principles:

* Keep components focused on UI responsibilities.
* Keep API communication in service files.
* Avoid hardcoded backend URLs.
* Avoid duplicating Axios configuration.
* Reuse common UI components.
* Keep authentication logic centralized.
* Keep permission checks consistent.
* Avoid unnecessary global state.
* Clean up SSE and WebSocket connections when components unmount.
* Do not expose secrets in frontend environment variables.
* Keep mobile and desktop behavior within the same responsive interface.

---

## 35. Local Development

The complete local development environment consists of:

```text
Browser
   |
   v
React + Vite
   |
   | REST API
   v
Spring Boot :8080
   |
   v
MongoDB :27017
```

For real-time communication:

```text
Spring Boot
   |
   +---- SSE
   |
   +---- WebSocket
   |
   v
React Frontend
```

---

## 36. Frontend Configuration Checklist

Before starting development, verify:

```text
[ ] Node.js installed
[ ] npm installed
[ ] Dependencies installed
[ ] .env configured
[ ] Backend running
[ ] MongoDB running
[ ] API base URL configured
[ ] Authentication configured
[ ] Axios client configured
[ ] Routing configured
[ ] Tailwind CSS configured
[ ] Real-time endpoints configured
```

---

## 37. Recommended Production Configuration

Production environments should use environment-specific configuration.

Example:

```env
VITE_API_BASE_URL=https://api.example.com/api
VITE_SSE_BASE_URL=https://api.example.com/api
```

Production configuration should also consider:

* HTTPS
* CORS configuration
* Secure authentication handling
* API timeout configuration
* Error handling
* Build optimization
* Asset caching
* Environment separation
* Logging
* Monitoring

No production secrets should be included in the frontend source code.

---

## 38. Summary

The Restaurant Management System frontend is designed as a modular React application communicating with a Spring Boot REST API.

The frontend configuration emphasizes:

* React and Vite
* Centralized Axios communication
* JWT-based authentication
* Permission-aware UI
* Responsive Tailwind CSS design
* Server-side pagination and filtering
* SSE and WebSocket real-time updates
* Reusable components
* Modular feature organization
* Environment-based configuration
* Production-ready build practices

The frontend should evolve alongside the backend while maintaining clear separation between presentation, API communication, authentication, permissions, and real-time functionality.
