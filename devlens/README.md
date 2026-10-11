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
│               # Zero Node.js, zero npm, zero React. Fully working mock engine.
└── backend/    # [Upcoming in Module 2] Java 17, Spring Boot 3.x, MySQL, Spring Security JWT
```

---

## 🌟 Module 1: Complete Frontend (Current Status: ✅ Complete)

The complete frontend interface has been faithfully constructed using the **Stitch** design system and design tokens:
- **60% Navy Background**: `#0F172A` & `#0B1326`
- **30% Slate Panels & Cards**: `#1E293B` with `#334155` borders
- **10% Electric Indigo Accent**: `#6366F1`
- **Telemetry Colors**: Cyan (`#22D3EE`), Emerald (`#22C55E`), Amber (`#F59E0B`), Rose (`#EF4444`)
- **Typography**: Inter (UI) & JetBrains Mono (Code)

### Implemented Views & Features
1. **Split-Screen Authentication (`pages/login.html`)**: Algorithmic viz hero on left, Sign In and Register tabs with client validation on right.
2. **Dashboard (`pages/dashboard.html`)**: 4 stat KPI cards, Chart.js telemetry activity line chart & distribution donut chart, recent analyses table, and viva streak widget.
3. **Projects Ecosystem (`pages/projects.html`, `pages/project-detail.html`)**: Search, language filters, sort options, project create/edit/delete modals, and tracked file registry.
4. **Code Editor Suite (`pages/editor.html`)**: **CodeMirror 5** integration preloaded with Java Bubble Sort, 7 deep analysis tabs (Explanation, Errors & Debugging, Complexity, Code Review, Flowchart via **Mermaid.js**, Optimization, Docs), line jump highlighting, and real-time "Apply Fix" updates.
5. **Code Compare (`pages/compare.html`)**: Dual CodeMirror side-by-side comparison, algorithm complexity matrix, and optimization verdict card.
6. **Learning Mode & Viva Prep (`pages/learning.html`)**: Stepper walkthrough, interactive quiz with instant scoring and explanations, and college viva examination accordion.
7. **AI Assistant (`pages/assistant.html`)**: Session drawer, prompt chips, typing indicator, and syntax-highlighted code blocks with copy buttons.
8. **Reports & Document View (`pages/reports.html`, `pages/report-view.html`)**: Comprehensive audit records, print report functionality, and PDF tooltip.
9. **Settings & Themes (`pages/settings.html`)**: Profile info, default explanation level, theme switcher, and password security.
10. **Central API Gateway (`js/api.js`)**: `USE_MOCK = true` serves rich realistic Java Bubble Sort datasets with latency simulation, ready to toggle to live backend in Module 2.
11. **API Contract Single Source of Truth (`docs/API_CONTRACT.md`)**: Full endpoint specification with request and response payloads.

---

## 🚀 How to Run the Frontend

1. **Via VS Code Live Server**:
   - Right click `devlens/frontend/index.html` -> **"Open with Live Server"** (port `5500`).
2. **Via Python Local Server**:
   ```bash
   cd devlens/frontend
   python -m http.server 5500
   ```
   Open `http://localhost:5500/` in any browser.

---

## 🎓 College Viva Explanation Guide

When presenting this project in a college viva or evaluation:
1. **Separation of Concerns**: The frontend is completely decoupled from the backend and communicates exclusively through `api.js` via a strict REST API contract (`docs/API_CONTRACT.md`).
2. **Design Tokens (60-30-10)**: Explain how CSS variables in `tokens.css` establish mathematical color hierarchy, eliminating visual fatigue with slate surfaces and highlighting interactive nodes with electric indigo.
3. **State Management**: Authentication state and user preferences are safely managed via `localStorage` with route guards in `auth.js` that prevent unauthorized access.
4. **Interactive AST Visualizations**: Describe how CodeMirror line-markers coordinate with static analysis findings, allowing students to jump to bugs and apply refactors directly.
