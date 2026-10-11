# DevLens Backend

DevLens REST API backend built with **Java 17**, **Spring Boot 3.x**, and **Maven**.  
Provides secure JWT authentication, strict user data isolation, code repository management, and code analysis persistence for programming students.

---

## 1. Prerequisites

- **Java Development Kit (JDK)**: Java 17 or higher (JDK 21 LTS supported).
- **Apache Maven**: 3.8+ (or use `./mvnw`).
- **MySQL Server**: 8.0+ running on port `3306`.

---

## 2. MySQL Installation & Configuration

### Option A: Local MySQL Community Server (Windows)
1. Download and install **MySQL Community Server 8.0+** from [dev.mysql.com](https://dev.mysql.com/downloads/installer/).
2. During installation, set root user password to `123456`.
3. Start the MySQL service:
   ```powershell
   net start MySQL
   ```
4. Verify port `3306` is accessible:
   ```powershell
   Test-NetConnection -ComputerName localhost -Port 3306
   ```

### Option B: Docker Container
Alternatively, spin up MySQL 8 in Docker with a single command:
```bash
docker run --name devlens-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=devlens -d mysql:8.0
```

### Application Database Properties
The backend automatically creates the `devlens` database and manages table creation:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/devlens?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

---

## 3. Running the Backend

### Run via Maven
From the `devlens/backend` directory:
```bash
mvn spring-boot:run
```

Or using the Maven wrapper:
```bash
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**.

---

## 4. Default Demo Account

When the database is empty on first startup, a `CommandLineRunner` (`DemoDataSeeder`) automatically seeds a demo student account:

- **Email**: `demo@devlens.com`
- **Password**: `demo123`
- **Role**: `USER`
- **Pre-loaded Projects**:
  - `CS201: Data Structures & Algorithms` (with `BubbleSort.java` and `QuickSort.java`)
  - `Distributed Task Scheduler` (with `TaskWorker.java`)
- **Pre-loaded Analyses**: Comprehensive, Complexity Audit, and Code Review records.

---

## 5. Architecture & Package Structure

Base package: `com.devlens`
```
com.devlens/
├── auth/                 # Authentication (Register, Login, JWT tokens)
│   ├── controller/
│   ├── dto/
│   └── service/
├── user/                 # User entity & profile management
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
├── project/              # Project & SourceFile repositories & controllers
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
├── analysis/             # Static Code Analysis Engine & Analysis Persistence
│   ├── controller/       # AnalysisController & AnalyzeController (/api/analyze/**)
│   ├── dto/              # Comprehensive, Error, Complexity, Review & Flowchart DTOs
│   ├── entity/           # Analysis entity
│   ├── repository/       # AnalysisRepository
│   ├── service/          # CodeAnalysisEngineService & AnalysisService
│   ├── syntax/           # SyntaxAnalyzer (JavaParser AST syntax diagnostic)
│   ├── errors/           # ErrorAnalyzer (Rule-based hazards, nulls, ==, loops)
│   ├── complexity/       # ComplexityAnalyzer (O(1) to O(2^n) time & space)
│   ├── quality/          # QualityAnalyzer (Cyclomatic, method length, weighted score)
│   └── flowchart/        # FlowchartGenerator (CFG graph nodes & Mermaid TD)
├── history/              # Analysis history search, filtering & notes
│   ├── controller/
│   ├── dto/
│   └── service/
├── dashboard/            # Aggregate stats & activity timeline charts
│   ├── controller/
│   ├── dto/
│   └── service/
└── common/               # Shared cross-cutting concerns
    ├── config/           # SecurityConfig, JwtTokenProvider, DataSeeder
    ├── dto/              # ErrorResponse, ApiResponse
    └── exception/        # GlobalExceptionHandler (@ControllerAdvice)
```

---

## 6. Security & Data Isolation Guarantees

1. **Stateless JWT Security**: Pass the bearer token in every request:
   `Authorization: Bearer <jwt-token>`
2. **Public Endpoints**: Only `/api/auth/**` and CORS `OPTIONS` requests are public.
3. **Owner Scoping**:
   - Every SQL query is parameterized by the authenticated user's ID (`@AuthenticationPrincipal UserPrincipal principal`).
   - User A cannot view, edit, or delete User B's projects, files, or analyses.
   - Cross-user queries return `404 Not Found` to prevent resource enumeration.
4. **Global Exception Handling**:
   - Validation failures return structured JSON:
     ```json
     {
       "status": 400,
       "error": "Validation failed",
       "fields": {
         "email": "Email must be a valid email address"
       }
     }
     ```
   - Zero stack traces or internal implementation details are leaked to clients.
5. **Payload Limits & Defensive Handling**:
   - `/api/analyze/**` endpoints enforce a 100 KB payload ceiling (HTTP 400).
   - Malformed syntax or non-compilable code returns structured 200 responses with exact syntax error feedback, never 500 server crashes.

---

## 7. Running Automated Tests

A comprehensive JUnit 5 and MockMvc test suite verifies authentication, ownership isolation, history filtering, and the complete static code analysis engine using an in-memory H2 database test profile:

```bash
mvn test
```

### Verified Test Suites (20 Tests, 0 Failures):
- `AuthControllerTest`:
  - User registration and token issuance
  - Rejection of duplicate email addresses
  - Successful login with credentials
  - Rejection of invalid passwords (401)
- `ProjectOwnershipTest`:
  - Multi-tenant data isolation: User B receives 404 attempting to read or delete User A's project
- `HistoryFilterTest`:
  - Type-based filtering (`DEBUG`, `COMPLEXITY`)
  - Full-text search across notes and languages
  - Cross-user history scoping isolation
- `CodeAnalysisEngineTest` (Static Code Analysis Engine):
  - String equality reference trap (`==` vs `.equals()`)
  - Empty catch block detection
  - Infinite loop detection (`while(true)`)
  - Array boundary hazard in classic bubble sort (`arr.length` vs `arr.length - 1`)
  - Unreachable statement detection
  - Potential `NullPointerException` dereference
  - Asymptotic complexity calculation (`O(n²)` bubble sort, `O(n log n)` merge sort, `O(2^n)` recursive Fibonacci, `O(1)` constant math)
  - Code quality cyclomatic scoring and line length warnings
  - Mermaid flowchart generation (`graph TD` with control flow nodes and decision diamonds)
  - Syntax error diagnostic handling
  - Non-Java language educational notice
  - 100 KB payload boundary enforcement
