---
trigger: always_on
---

AI Agent Rules & Development Guidelines

Project Target: S1 Final Project / Capstone (Fullstack Web Application)

Document Reference: Ketentuan Umum Pembuatan Projekan S1

🚀 Core Objective & Role

You are an expert Fullstack Software Engineer and System Architect AI Agent. Your objective is to design, develop, test, and document a production-ready, highly compliant Fullstack Application based on the specific guidelines set forth below.

All generated code, architectures, and implementations must strictly adhere to these rules without omission.

🎨 FRONTEND RULES (Client-Side)

1. Responsive Layout Strategy

Breakpoints Requirement:

Mobile: < 768px

Tablet: 769px - 1024px

Desktop: > 1024px

Rule: Every main page must adjust fluidly to all screen sizes without horizontal scrollbars, layout breakage, or overflow errors.

2. Authentication Flow

Components Required: Login, Register, Logout, Forgot Password, Reset Password.

Storage: JWT Token must be securely stored using LocalStorage or Cookie.

Protected Routes: Unauthenticated users must not access private pages.

Session Handling:

Successful login automatically redirects to /dashboard.

User sessions must persist across page reloads/browser refreshes.

Logout must purge JWT tokens, clear local storage/cookies, and reset state.

3. Client-Side Routing

Implement robust Client-Side Routing (e.g., React Router, Next.js App/Pages Router, Vue Router).

Route Types:

Public Routes: Accessible by anyone (e.g., Login, Register, Landing Page).

Private Routes: Accessible only by authenticated users.

Role-Based Routes: Restricted based on user roles (e.g., /admin/*).

Automatic redirection to unauthorized page or login when access is denied.

4. Real-Time Dynamic Dashboard

Dashboard must not contain hardcoded static data.

Fetch and display dynamic data from the backend via APIs.

Required Widgets:

Summary Cards (Key metrics overview).

Total Data Counters.

Statistical Charts / Graphs.

Recent Activity Logs.

5. Standard CRUD Interface

Every major entity must have a full interface suite:

List View (Table/Grid)

Detail View

Create Form

Edit Form

Delete Action (with confirmation modal)

All CRUD actions must connect directly to the REST API endpoints.

6. Search, Filter, & Sort Capabilities

Must be implemented on all main list pages.

Search: Keyword-based searching.

Filter: Multi-criteria filtering (e.g., Status, Category, Date range).

Sorting: Newest, Oldest, Alphabetical (A-Z, Z-A).

Rule: Search, Filter, and Sorting must be able to operate simultaneously.

7. Pagination Standard

Required on all dynamic list pages.

Components: Previous/Next buttons, Page numbers, Total item counter, Items-per-page selector (e.g., 10, 25, 50).

8. File Upload Interface

Forms must support file upload capability (specifically Image files .jpg, .png, .webp or .pdf documents).

9. Form Validation

Real-time inline feedback/error messages.

Rules to enforce: Required fields, Min/Max length, Email format, Phone number format, Password confirmation match.

10. Notifications & Error Handling

Toast Notifications: Trigger interactive toasts for success, error, warning, and info states across all CRUD actions.

Error Pages / Fallbacks: Gracefully handle and render specific pages/views for:

401 Unauthorized

403 Forbidden

404 Not Found

500 Internal Server Error

Network/API failure fallback UI.

⚙️ BACKEND RULES (Server-Side)

1. RESTful API Standard

Strict HTTP Methods usage: GET, POST, PUT, PATCH, DELETE.

Return appropriate HTTP Status Codes (e.g., 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 422 Unprocessable Entity, 500 Internal Server Error).

2. Auth & RBAC (Role-Based Access Control)

Auth Endpoints: Register, Login, Logout, Forgot Password, Reset Password, Refresh Token (Optional/Bonus).

RBAC: Minimum of 2 roles (e.g., Admin vs User) with distinct permissions enforced via middleware.

3. Complete CRUD & Entity Count

Minimum of 6 core entities.

All entities must have full functional Create, Read, Update, and Delete endpoints (no dummy/stubbed handlers).

4. Server-Side Input Validation

All POST and PUT endpoints must validate payloads.

Validation Rules: Required, Email, Unique, Min, Max, Enum, Numeric, Date.

Return structured error JSON responses for invalid inputs (HTTP 422 / 400).

5. File Upload Handling

Server must process and store uploaded Images or PDF files, returning accessible file URLs or storage paths.

6. Global Error Handling

Unified exception interceptor/middleware returning consistent JSON structures across:
400, 401, 403, 404, 422, and 500.

7. API Security Controls

Implementation of mandatory security practices:

Password hashing (e.g., bcrypt, argon2).

JWT Authentication middleware.

CORS policies configuration.

Request Sanitization & SQL Injection prevention (via ORM/Parameterized queries).

XSS Protection.

8. Search, Filter, Sort, & Pagination API

Standardize GET list endpoints to handle query parameters:

GET /api/v1/products?page=1&limit=10

GET /api/v1/products?search=laptop

GET /api/v1/products?status=active

GET /api/v1/products?sort=name

GET /api/v1/products?category=1

9. API Documentation

Provide interactive or structured API documentation using either Swagger/OpenAPI or a Postman Collection covering 100% of the active endpoints.

🗄️ DATABASE RULES

Entity Count: Minimum of 6 primary tables.

Relationships: Minimum of 5 explicit relationships, covering:

One-to-One (1:1)

One-to-Many (1:N)

Many-to-One (N:1)

Many-to-Many (N:M)

Integrity: Explicit Primary Keys and Foreign Keys defined.

Normalization: Database schema normalized up to at least 3rd Normal Form (3NF).

Audit Timestamps: Every primary table must contain created_at and updated_at columns.

Soft Delete: At least 2 tables must implement soft delete (deleted_at timestamp column).

Database Seeding: Include database seeds with a minimum of 20 realistic records per primary table for demonstration/testing.

📦 SUBMISSION & MONOREPO RULES

1. Monorepo Structure

Frontend and Backend code must reside within a single public GitHub repository.

Recommended folder structure:

root/
├── frontend/
├── backend/
├── docs/
└── README.md


2. README.md Requirements

The README.md file must be comprehensive and include:

Application Title & Brief Description

Key Features Showcase

Tech Stack Used

Monorepo Directory Structure

Installation & Setup Instructions (Step-by-Step for Local Running)

Demo Account Credentials (e.g., Admin & User logins)

3. System Architecture Design

Provide a system flowchart / architecture diagram document inside the repository (/docs or linked in README.md).