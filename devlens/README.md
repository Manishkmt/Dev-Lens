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

## 🔬 Module 3: Static Code Analysis Engine (Status: ✅ Complete)

A rule-based Abstract Syntax Tree (AST) analysis engine powered by **JavaParser** (`javaparser-core:3.26.2`). Deterministic, ultra-fast, and requires zero external AI tokens or API subscriptions.

### Engine Capabilities:
1. **AST Syntax Diagnostic (`com.devlens.analysis.syntax.SyntaxAnalyzer`)**:
   - Strict Java compilation unit validation with humanized error messages and exact line/column pointers.
2. **Deterministic Code Hazard Detector (`com.devlens.analysis.errors.ErrorAnalyzer`)**:
   - Reference equality on Strings (`str == "val"` vs `.equals()`).
   - Empty catch blocks with silent exception swallowing.
   - Infinite loop detection (`while (true)` or stagnant counter conditions).
   - Potential `NullPointerException` dereferences and unverified nullable invocations.
   - Out-of-bounds loop indices in classic array traversal algorithms (e.g., `arr.length` vs `arr.length - 1` in bubble sort).
   - Unreachable statements and unused local variables.
3. **Asymptotic Complexity Analyzer (`com.devlens.analysis.complexity.ComplexityAnalyzer`)**:
   - Calculates Big-O time and auxiliary space complexity: `O(1)`, `O(log n)`, `O(n)`, `O(n log n)`, `O(n²)`, `O(n³)`, `O(2^n)`.
   - Generates step-by-step mathematical reasoning and nested loop multiplier metrics for student education.
4. **Code Quality & Cyclomatic Scoring (`com.devlens.analysis.quality.QualityAnalyzer`)**:
   - Measures method length, parameter bloat, naming conventions, and cyclomatic branching complexity.
   - Produces a weighted quality score: `0.40 * Reliability + 0.25 * Maintainability + 0.20 * BestPractices + 0.15 * Readability`.
5. **Control Flow Graph & Mermaid Visualizer (`com.devlens.analysis.flowchart.FlowchartGenerator`)**:
   - Synthesizes control flow nodes, decision diamonds, and loops into live Mermaid flowchart definitions (`graph TD`).
6. **API Endpoints (`/api/analyze/**`)**:
   - `POST /api/analyze/errors`
   - `POST /api/analyze/complexity`
   - `POST /api/analyze/review`
   - `POST /api/analyze/flowchart`
   - `POST /api/analyze/all`
   - 100 KB payload boundary enforcement (HTTP 400).
   - Defensive error handling: bad code and syntax errors return structured 200 responses with actionable feedback, never 500 exceptions.
7. **Frontend Editor Integration**:
   - `devlens/frontend/js/api.js` connects live to `/api/analyze/*` (`MOCK_CONFIG.analyze = false`).
   - "Apply Fix" directly mutates code in the browser editor.
   - Interactive Mermaid diagrams render dynamically in the Visual Execution Flow tab.
   - Runs automatically save snapshots to `/api/analyses` to immediately populate History and Dashboard.
8. **Automated & E2E Testing**:
   - 14 comprehensive JUnit 5 tests in `CodeAnalysisEngineTest` (20 total backend tests pass with 0 failures).
   - Full browser execution verified and recorded via video artifact.

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
