# DevLens — Frontend Application

> **Tagline**: *"See your code. Understand your code. Improve your code."*  
> An AI-powered code analysis and developer assistance platform built for programming students.

---

## 🎨 Design Philosophy & 60-30-10 Token System

DevLens faithfully reproduces the **Stitch** design specifications (`/stitch_export`):
- **60% Navy Canvas**: `#0B1326` & `#0F172A` deep backgrounds minimizing eye fatigue.
- **30% Slate Panels**: `#1E293B` elevated cards, sidebars, and dialogs delineated with precision `#334155` borders.
- **10% Electric Indigo Accent**: `#6366F1` / `#8083FF` for primary actions, glowing focus rings, and active states.
- **Semantic Telemetry Colors**:
  - Cyan (`#22D3EE` / `#5DE6FF`): AI inference, diff highlights, AST tags.
  - Emerald (`#22C55E` / `#10B981`): Clean compilation, passing tests.
  - Amber (`#F59E0B`): Static analysis warnings and boundary alerts.
  - Rose (`#EF4444`): Hazards, syntax regressions, and runtime exceptions.
- **Dual Typographic Discipline**:
  - **Inter**: Clean UI traversal and readable pedagogical guidance.
  - **JetBrains Mono**: Fixed-width code inspection, AST hashes, line numbers, and complexity mathematics.

---

## 📂 Project Architecture

```
devlens/frontend/
├── index.html                   # Auth gateway router (redirects to dashboard or login)
├── README.md                    # This developer guide
├── assets/
│   └── logo.svg                 # High-resolution vector brand asset
├── css/
│   ├── tokens.css               # 60-30-10 CSS variables, dark/light theme definitions
│   ├── base.css                 # Global resets, typography hierarchy, custom scrollbars
│   ├── components.css           # Buttons, cards, modals, tables, toasts, shared shell
│   └── pages.css                # Split-screen auth, CodeMirror theme, Mermaid container
├── js/
│   ├── api.js                   # Single API gateway with USE_MOCK flag
│   ├── auth.js                  # Token storage, route guarding & session manager
│   ├── layout.js                # Injects shared sidebar, topbar, active highlight, notifications
│   ├── theme.js                 # Dark & light theme switcher with persistence
│   ├── toast.js                 # Floating toast notification system
│   ├── utils.js                 # HTML escaping, formatters, debounce, clipboard
│   ├── charts.js                # Chart.js integration for activity & donut graphs
│   ├── mock/                    # Complete mock datasets matching docs/API_CONTRACT.md
│   │   ├── authMock.js
│   │   ├── usersMock.js
│   │   ├── projectsMock.js
│   │   ├── filesMock.js
│   │   ├── dashboardMock.js
│   │   ├── analysesMock.js
│   │   ├── historyMock.js
│   │   ├── aiMock.js
│   │   ├── reportsMock.js
│   │   └── notificationsMock.js
│   └── pages/                   # One modular script per HTML view
│       ├── login.js
│       ├── dashboard.js
│       ├── projects.js
│       ├── project-detail.js
│       ├── editor.js
│       ├── history.js
│       ├── learning.js
│       ├── compare.js
│       ├── assistant.js
│       ├── reports.js
│       ├── report-view.js
│       └── settings.js
├── pages/
│   ├── login.html               # Split-screen auth (Sign in & Register tabs)
│   ├── register.html            # Registration entry point
│   ├── dashboard.html           # 4 stat cards, 2 Chart.js graphs, recent analyses
│   ├── projects.html            # Repository grid, search, filters, create/edit modal
│   ├── project-detail.html      # Project file registry & file uploads
│   ├── editor.html              # CodeMirror 5, 7 analysis tabs, Mermaid flowchart
│   ├── history.html             # Audit trail, filters, pagination, viva notes
│   ├── learning.html            # Stepper walkthrough, interactive quiz, college viva FAQ
│   ├── compare.html             # Dual CodeMirror diff, comparison matrix, verdict card
│   ├── assistant.html           # AI chat stream, session drawer, prompt chips, code copy
│   ├── reports.html             # Audit reports grid, generate modal, PDF download tooltip
│   ├── report-view.html         # Printable full quality audit document
│   ├── settings.html            # Profile edit, default level, theme, password change
│   └── 404.html                 # Custom 404 error page matching theme
└── docs/
    └── API_CONTRACT.md          # Complete REST API specification with sample payloads
```

---

## 🚀 How to Run Locally

Because the frontend is built using standard **ES Modules (ESM)** without Node.js or bundlers, it should be served over an HTTP server so browsers allow module imports.

### Option 1: VS Code Live Server (Recommended)
1. Open the project folder in VS Code.
2. Right click on `devlens/frontend/index.html` (or `pages/login.html`).
3. Click **"Open with Live Server"** (default port `5500` -> `http://localhost:5500/devlens/frontend/`).

### Option 2: Python Simple HTTP Server
Open your terminal in the `devlens/frontend` directory and run:
```bash
# Inside devlens/frontend directory
python -m http.server 5500
```
Then navigate your browser to:
👉 `http://localhost:5500/`

---

## 🔌 Switching from Mock Mode to Live Backend

In `devlens/frontend/js/api.js`:

```javascript
// Toggle between mock data and real Spring Boot REST API
export const USE_MOCK = true;  // Change to false when backend is running on :8080
export const API_BASE_URL = "http://localhost:8080";
```

- When `USE_MOCK = true`: All responses are served locally from `js/mock/*.js` with an artificial 300–500ms delay to simulate realistic network latency.
- When `USE_MOCK = false`: All requests issue `fetch()` calls to `http://localhost:8080/api/...` passing the `Authorization: Bearer <jwt>` header stored in `localStorage`.

---

## 🧪 How to Test Every Feature in Module 1

1. **Authentication**:
   - Open `index.html`. It automatically redirects to `pages/login.html`.
   - Toggle between **Sign In** and **Register**.
   - Input invalid data (empty fields or short passwords) to observe the validation error alerts.
   - Click "Sign in to DevLens" with prefilled credentials (`alex.chen@university.edu` / `Password123`) to land on `dashboard.html`.

2. **Dashboard**:
   - View the 4 stat cards with telemetry indicators.
   - Interact with the **Chart.js** line graph and donut distribution chart.
   - Check the recent analyses table and learning progress widget.

3. **Projects Ecosystem**:
   - Open **Projects** from the sidebar.
   - Search for "Algorithms" using the `/` keyboard shortcut.
   - Filter by language (Java, Python, C++, TypeScript).
   - Click **"+ Create New Project"**, fill in the modal, and save.
   - Click any project card to open **Project Details** and inspect its tracked files.

4. **Code Editor & Semantic Analysis Suite**:
   - Navigate to **Code Editor**.
   - Inspect the **CodeMirror 5** editor preloaded with `BubbleSort.java`.
   - Click **"⚡ Analyze Code"** (or press `⌘↵` / `Ctrl+Enter`) to observe the loading skeleton and 7 populated analysis tabs:
     1. **Explanation**: Executive summary, precondition/postcondition, step-by-step logic.
     2. **Errors & Debugging**: Boundary warning and null guard info cards. Click an issue card to auto-scroll and highlight the line in CodeMirror! Click **"Apply Fix"** to update the editor code in real time.
     3. **Complexity**: O(n²) worst / O(n) best analysis, nested loop multiplier callout (click to highlight lines 6–15).
     4. **Code Review**: 88/100 benchmark, readability / maintainability progress bars, recommendations.
     5. **Flowchart**: **Mermaid.js** execution graph with zoom in/out/reset controls and SVG export.
     6. **Optimization**: HotSpot C2 compiler telemetry and scalar replacement snippet.
     7. **Docs**: Auto-generated Java 17 Javadoc with one-click copy button.

5. **Learning Mode & College Viva Prep**:
   - Navigate to **Learning Mode**.
   - Step through the 4-phase algorithmic walkthrough.
   - Test your knowledge with the **Interactive Quiz** (answers validate immediately with scoring and feedback explanations).
   - Expand the **Frequently Asked Viva Questions** accordion for examination answers.

6. **Code Compare**:
   - Open **Code Compare**.
   - Inspect the side-by-side CodeMirror editors (Variant A vs Variant B).
   - Switch presets using the dropdown and click **"Compare Algorithms"**.
   - Review the side-by-side complexity matrix and AI optimization verdict.

7. **AI Assistant**:
   - Open **AI Assistant**.
   - Click a quick prompt chip (e.g. *"Why does line 9 need temp?"*) or type a custom question.
   - Watch the typing indicator and review the AI response with syntax-highlighted code blocks and copy buttons.
   - Switch or create new chat sessions.

8. **Reports**:
   - Open **Reports** to see academic audit cards.
   - Note the **"Download PDF"** button displays a tooltip stating that PDF generation connects with the backend in later modules.
   - Click **"View"** to open `report-view.html` and test the **"Print Report"** feature.

9. **Settings & Themes**:
   - Click the theme toggle icon in the topbar or open **Settings** to switch between **Dark** and **Light** modes.
   - Change your display name, default explanation level, or password and observe toast notifications.
