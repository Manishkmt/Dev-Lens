# DevLens — AI-Powered Code Analysis & Developer Assistance Platform

> **Tagline**: *"See your code. Understand your code. Improve your code."*  
> Built for programming students, educators, and software engineering coursework.

---

## 🎯 Project Overview

**DevLens** is an interactive educational and code analysis platform designed to accelerate computer science learning. It translates complex source code into intuitive visual execution graphs, detects syntactic and boundary hazards, analyzes asymptotic algorithmic complexity, provides real-time AI code assistance, and prepares students for academic viva voce examinations.

---

## 🗂️ Overall System Architecture

```
devlens/
├── frontend/   # Plain HTML5, CSS3 (60-30-10 Design Tokens), Vanilla JavaScript (ES Modules)
│               # Zero Node.js, zero npm, zero React. Fully working mock engine & live backend gateway.
└── backend/    # Java 17, Spring Boot 3.x, Maven, MySQL 8, Spring Security JWT (Stateless)
```

---

## 🌟 Module 1: Complete Frontend (Status: ✅ Complete)

The complete frontend interface has been faithfully constructed using the **Stitch** design system and design tokens:
- **60% Navy Background**: `#0F172A` & `#0B1326`
- **30% Slate Panels & Cards**: `#1E293B` with `#334155` borders
- **10% Electric Indigo Accent**: `#6366F1`
- **Telemetry Colors**: Cyan (`#22D3EE`), Emerald (`#22C55E`), Amber (`#F59E0B`), Rose (`#EF4444`)
- **Typography**: Inter (UI) & JetBrains Mono (Code)

---

## 🚀 Module 2: Backend Foundation & Integration (Status: ✅ Complete)

A production-grade Java 17 Spring Boot 3 REST API with MySQL persistence and stateless JWT security:

### Key Implemented Components:
1. **Database Schema & JPA Entities**:
   - `User`: BCrypt password security, unique emails, customizable theme & learning level.
   - `Project`: Workspaces scoped strictly to the authenticated owner.
   - `SourceFile`: Multi-file storage with `LONGTEXT` code content and quality telemetry.
   - `Analysis`: Analysis records, quality scores, notes, and polymorphic analysis types.
2. **Stateless Security (Spring Security 6 + JJWT 0.12.6)**:
   - Stateless session filter (`JwtAuthenticationFilter`).
   - CORS support for ports `5500`, `127.0.0.1:5500`, and `3000`.
   - Strict owner-level data isolation (User A cannot access User B's resources).
3. **Data APIs**:
   - `/api/auth/**` (Register, Login -> JWT token + user profile).
   - `/api/users/me` (Profile view, preferences update, password change).
   - `/api/projects/**` & `/api/files/**` (Full project and source file CRUD).
   - `/api/analyses` (Reusable analysis result persistence).
   - `/api/history` (Filtered, paginated analysis history with search & notes editing).
   - `/api/dashboard/**` (Live KPI stats & time-series activity charts).
4. **Resilience & Seeding**:
   - Global `@ControllerAdvice` returning standardized `{ status, error, fields }` JSON without leaking stack traces.
   - `CommandLineRunner` automatically seeds demo student (`demo@devlens.com` / `demo123`) with sample projects and analyses.
5. **Frontend Live Integration**:
   - `frontend/js/api.js` updated with granular `MOCK_CONFIG` flags routing Module 2 endpoints directly to `http://localhost:8080`.
   - Verified live login, project workspace, history audit, and dashboard workflows in browser.
6. **Automated Test Suite**:
   - JUnit 5 & MockMvc tests (`AuthControllerTest`, `ProjectOwnershipTest`, `HistoryFilterTest`) run against an in-memory H2 database.

---

## 🏃 Quick Start Guide

### 1. Start MySQL Server
Ensure MySQL is running on `localhost:3306` with credentials `root` / `123456`.

### 2. Run Spring Boot Backend
```bash
cd devlens/backend
mvn spring-boot:run
```
Backend starts on **http://localhost:8080**.

### 3. Run Frontend
```bash
cd devlens/frontend
python -m http.server 5500
```
Open **http://localhost:5500/pages/login.html** in any modern web browser.  
Log in with demo credentials: **demo@devlens.com** / **demo123**.

---

## 🎓 College Viva Explanation Guide

1. **Layered Architecture**: Controller -> Service -> Repository -> Entity separation with immutable DTOs and Jakarta Validation.
2. **Stateless Security**: Explain HMAC-SHA256 JWT tokens passed via HTTP `Authorization: Bearer <token>` headers without session state on the server.
3. **Tenant & Data Isolation**: Highlight how all SQL queries are scoped to `@AuthenticationPrincipal UserPrincipal principal`, ensuring User B can never read or mutate User A's projects.
4. **Global Error Handling**: Demonstrates enterprise defensive programming by catching validation exceptions and returning clean JSON payloads rather than internal server stack traces.
