# Restaurant Management System

A full-stack restaurant management and POS platform designed to manage restaurant operations from a centralized system.

The system is being developed with a modular architecture using Spring Boot for the backend, MongoDB for data persistence, and React for the frontend. It is designed to support multiple restaurants, branches, users, roles, permissions, orders, inventory, menu management, tables, reservations, payments, and other restaurant operations.

## Overview

The Restaurant Management System is intended to provide a complete operational platform for restaurants, cafés, and fast-food businesses.

The application follows a modular and layered architecture so that individual business domains can be developed, maintained, and extended independently.

The system supports multi-tenant restaurant management through a shared database and shared collections architecture, with tenant-level data isolation using `tenantId`.

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring Security
- Spring Data MongoDB
- MongoDB
- JWT Authentication
- BCrypt Password Hashing
- Maven
- WebSocket
- Server-Sent Events (SSE)
- REST APIs

### Frontend

- React
- Vite
- JavaScript
- Tailwind CSS
- Axios
- React Toastify
- Lucide Icons

### Development Tools

- IntelliJ IDEA
- Visual Studio Code
- MongoDB
- Bruno API Client
- Git
- GitHub

## Architecture

The backend follows a modular layered architecture.

```text
Controller
    |
    v
Handler
    |
    v
Service
    |
    v
Custom Repository
    |
    v
Repository
    |
    v
MongoDB