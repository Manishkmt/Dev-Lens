# DevLens REST API Contract & Specification

**Base URL**: `http://localhost:8080`  
**Authentication Header**: `Authorization: Bearer <jwt-token>`  
**Standard Error Payload**:
```json
{
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "email": "Email must be a valid email address",
    "password": "Password must be at least 6 characters"
  }
}
```

---

## 1. Authentication (`/api/auth`)

### 1.1 User Registration
- **Method**: `POST`
- **Endpoint**: `/api/auth/register`
- **Auth**: None (Public)
- **Request Body**:
```json
{
  "name": "Alex Chen",
  "email": "alex.chen@university.edu",
  "password": "Password123"
}
```
- **Response `201 Created`**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IkFsZXggQ2hlbiIsImVtYWlsIjoiYWxleC5jaGVuQHVuaXZlcnNpdHkuZWR1Iiwicm9sZSI6IlVTRVIifQ.signature",
  "user": {
    "id": 1,
    "name": "Alex Chen",
    "email": "alex.chen@university.edu",
    "role": "USER",
    "defaultLevel": "INTERMEDIATE",
    "theme": "dark",
    "createdAt": "2026-10-11T10:00:00Z"
  }
}
```

### 1.2 User Login
- **Method**: `POST`
- **Endpoint**: `/api/auth/login`
- **Auth**: None (Public)
- **Request Body**:
```json
{
  "email": "alex.chen@university.edu",
  "password": "Password123"
}
```
- **Response `200 OK`**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IkFsZXggQ2hlbiIsImVtYWlsIjoiYWxleC5jaGVuQHVuaXZlcnNpdHkuZWR1Iiwicm9sZSI6IlVTRVIifQ.signature",
  "user": {
    "id": 1,
    "name": "Alex Chen",
    "email": "alex.chen@university.edu",
    "role": "USER",
    "defaultLevel": "INTERMEDIATE",
    "theme": "dark",
    "createdAt": "2026-10-11T10:00:00Z"
  }
}
```

---

## 2. User Profile (`/api/users`)

### 2.1 Get Current User Profile
- **Method**: `GET`
- **Endpoint**: `/api/users/me`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": 1,
  "name": "Alex Chen",
  "email": "alex.chen@university.edu",
  "role": "USER",
  "defaultLevel": "INTERMEDIATE",
  "theme": "dark",
  "createdAt": "2026-10-11T10:00:00Z"
}
```

### 2.2 Update Profile & Preferences
- **Method**: `PUT`
- **Endpoint**: `/api/users/me`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "Alex Chen",
  "defaultLevel": "ADVANCED",
  "theme": "dark"
}
```
- **Response `200 OK`**:
```json
{
  "id": 1,
  "name": "Alex Chen",
  "email": "alex.chen@university.edu",
  "role": "USER",
  "defaultLevel": "ADVANCED",
  "theme": "dark",
  "updatedAt": "2026-10-11T10:30:00Z"
}
```

### 2.3 Change Password
- **Method**: `PUT`
- **Endpoint**: `/api/users/me/password`
- **Auth**: Required
- **Request Body**:
```json
{
  "currentPassword": "Password123",
  "newPassword": "NewPassword456"
}
```
- **Response `200 OK`**:
```json
{
  "status": 200,
  "message": "Password updated successfully"
}
```

---

## 3. Projects (`/api/projects`)

### 3.1 List Projects
- **Method**: `GET`
- **Endpoint**: `/api/projects`
- **Auth**: Required
- **Response `200 OK`**:
```json
[
  {
    "id": 1,
    "name": "CS201: Data Structures & Algorithms",
    "description": "Core curriculum benchmark with AVL Trees, Dijkstra graph traversal, and dual-pivot quicksort.",
    "language": "java",
    "qualityScore": 94,
    "fileCount": 12,
    "createdAt": "2026-09-15T08:00:00Z",
    "lastAnalyzed": "2026-10-10T14:22:00Z"
  },
  {
    "id": 2,
    "name": "Distributed Task Scheduler",
    "description": "Thread pool executor and priority queue implementation in Java with bounded buffers.",
    "language": "java",
    "qualityScore": 86,
    "fileCount": 8,
    "createdAt": "2026-09-20T11:00:00Z",
    "lastAnalyzed": "2026-10-09T18:15:00Z"
  }
]
```

### 3.2 Create Project
- **Method**: `POST`
- **Endpoint**: `/api/projects`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "Operating System Kernels Simulation",
  "description": "Virtual memory paging simulation and round robin scheduler.",
  "language": "java"
}
```
- **Response `201 Created`**:
```json
{
  "id": 3,
  "name": "Operating System Kernels Simulation",
  "description": "Virtual memory paging simulation and round robin scheduler.",
  "language": "java",
  "qualityScore": 0,
  "fileCount": 0,
  "createdAt": "2026-10-11T12:00:00Z",
  "lastAnalyzed": null
}
```

### 3.3 Get Project Detail
- **Method**: `GET`
- **Endpoint**: `/api/projects/{id}`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": 1,
  "name": "CS201: Data Structures & Algorithms",
  "description": "Core curriculum benchmark with AVL Trees, Dijkstra graph traversal, and dual-pivot quicksort.",
  "language": "java",
  "qualityScore": 94,
  "fileCount": 12,
  "createdAt": "2026-09-15T08:00:00Z",
  "lastAnalyzed": "2026-10-10T14:22:00Z",
  "files": [
    {
      "id": 101,
      "name": "BubbleSort.java",
      "path": "src/algorithms/BubbleSort.java",
      "size": 756,
      "qualityScore": 88,
      "issuesCount": 2,
      "lastAnalyzed": "2026-10-10T14:20:00Z"
    },
    {
      "id": 102,
      "name": "QuickSort.java",
      "path": "src/algorithms/QuickSort.java",
      "size": 1240,
      "qualityScore": 96,
      "issuesCount": 0,
      "lastAnalyzed": "2026-10-10T14:22:00Z"
    }
  ]
}
```

### 3.4 Update Project
- **Method**: `PUT`
- **Endpoint**: `/api/projects/{id}`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "CS201: Data Structures & Algorithms (Updated)",
  "description": "Updated course syllabus code lab.",
  "language": "java"
}
```
- **Response `200 OK`**:
```json
{
  "id": 1,
  "name": "CS201: Data Structures & Algorithms (Updated)",
  "description": "Updated course syllabus code lab.",
  "language": "java",
  "qualityScore": 94,
  "fileCount": 12,
  "updatedAt": "2026-10-11T12:10:00Z"
}
```

### 3.5 Delete Project
- **Method**: `DELETE`
- **Endpoint**: `/api/projects/{id}`
- **Auth**: Required
- **Response `204 No Content`**

---

## 4. Project Files (`/api/projects/{id}/files` & `/api/files/{id}`)

### 4.1 List Project Files
- **Method**: `GET`
- **Endpoint**: `/api/projects/{id}/files`
- **Auth**: Required
- **Response `200 OK`**:
```json
[
  {
    "id": 101,
    "name": "BubbleSort.java",
    "path": "src/algorithms/BubbleSort.java",
    "size": 756,
    "qualityScore": 88,
    "issuesCount": 2,
    "lastAnalyzed": "2026-10-10T14:20:00Z"
  }
]
```

### 4.2 Add or Upload File
- **Method**: `POST`
- **Endpoint**: `/api/projects/{id}/files`
- **Auth**: Required
- **Request Body**:
```json
{
  "name": "MergeSort.java",
  "path": "src/algorithms/MergeSort.java",
  "content": "public class MergeSort {\n    // Code here\n}"
}
```
- **Response `201 Created`**:
```json
{
  "id": 103,
  "projectId": 1,
  "name": "MergeSort.java",
  "path": "src/algorithms/MergeSort.java",
  "size": 520,
  "qualityScore": null,
  "issuesCount": 0,
  "content": "public class MergeSort {\n    // Code here\n}",
  "createdAt": "2026-10-11T12:15:00Z"
}
```

### 4.3 Get File Detail & Content
- **Method**: `GET`
- **Endpoint**: `/api/files/{id}`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": 101,
  "projectId": 1,
  "name": "BubbleSort.java",
  "path": "src/algorithms/BubbleSort.java",
  "size": 756,
  "qualityScore": 88,
  "issuesCount": 2,
  "content": "public class BubbleSort {\n    public static void bubbleSort(int[] arr) {\n        int n = arr.length;\n        boolean swapped;\n        for (int i = 0; i < n - 1; i++) {\n            swapped = false;\n            for (int j = 0; j < n - i - 1; j++) {\n                if (arr[j] > arr[j + 1]) {\n                    int temp = arr[j];\n                    arr[j] = arr[j + 1];\n                    arr[j + 1] = temp;\n                    swapped = true;\n                }\n            }\n            if (!swapped) break;\n        }\n    }\n}",
  "lastAnalyzed": "2026-10-10T14:20:00Z"
}
```

### 4.4 Update File Content
- **Method**: `PUT`
- **Endpoint**: `/api/files/{id}`
- **Auth**: Required
- **Request Body**:
```json
{
  "content": "public class BubbleSort {\n    // modified code\n}"
}
```
- **Response `200 OK`**:
```json
{
  "id": 101,
  "name": "BubbleSort.java",
  "updatedAt": "2026-10-11T12:20:00Z"
}
```

### 4.5 Delete File
- **Method**: `DELETE`
- **Endpoint**: `/api/files/{id}`
- **Auth**: Required
- **Response `204 No Content`**

---

## 5. Dashboard (`/api/dashboard`)

### 5.1 Dashboard Stats
- **Method**: `GET`
- **Endpoint**: `/api/dashboard/stats`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "totalAnalyses": 128,
  "analysesGrowth": "+14% this week",
  "bugsDetectedAndFixed": 42,
  "highSeverityResolved": 8,
  "avgQualityScore": 88,
  "qualityTier": "Grade A Tier",
  "activeProjects": 9,
  "languages": ["Java", "Python", "C++", "TypeScript"]
}
```

### 5.2 Dashboard Chart Data
- **Method**: `GET`
- **Endpoint**: `/api/dashboard/chart-data`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "activity": {
    "labels": ["Sep 12", "Sep 16", "Sep 20", "Sep 24", "Sep 28", "Oct 02", "Oct 06", "Oct 10"],
    "analysesCount": [12, 19, 14, 25, 22, 30, 28, 38],
    "bugsFound": [3, 5, 2, 8, 4, 7, 5, 8]
  },
  "distribution": {
    "labels": ["Error Detection", "Complexity Analysis", "Code Review", "Flowchart Gen"],
    "data": [45, 30, 15, 10]
  }
}
```

---

## 6. Code Analysis Engine (`/api/analyze` & `/api/analyses`)

### 6.1 Run Error & Debugging Analysis
- **Method**: `POST`
- **Endpoint**: `/api/analyze/errors`
- **Auth**: Required
- **Request Body**:
```json
{
  "language": "java",
  "code": "public class BubbleSort { ... }",
  "level": "INTERMEDIATE"
}
```
- **Response `200 OK`**:
```json
{
  "status": "success",
  "issuesCount": 2,
  "warnings": 1,
  "infos": 1,
  "issues": [
    {
      "id": "ISSUE-1",
      "type": "WARNING",
      "title": "Boundary Inefficiency",
      "line": 7,
      "endLine": 7,
      "codeSnippet": "for (int j = 0; j < n - 1; j++)",
      "explanation": "Inner loop compares already-sorted elements at the end of the array on later passes without subtracting 'i'.",
      "whyItHappens": "Each outer pass guarantees that the i-th largest element settles in its final spot.",
      "suggestedFix": "Change loop termination condition to 'j < n - i - 1'.",
      "fixCode": "for (int j = 0; j < n - i - 1; j++) {"
    },
    {
      "id": "ISSUE-2",
      "type": "INFO",
      "title": "Potential Missing Null Guard",
      "line": 2,
      "endLine": 2,
      "codeSnippet": "public static void bubbleSort(int[] arr)",
      "explanation": "Method does not verify if arr is null before accessing arr.length.",
      "whyItHappens": "Calling arr.length on null raises NullPointerException.",
      "suggestedFix": "Add 'if (arr == null || arr.length <= 1) return;' at start.",
      "fixCode": "if (arr == null || arr.length <= 1) return;"
    }
  ]
}
```

### 6.2 Run Complexity Analysis
- **Method**: `POST`
- **Endpoint**: `/api/analyze/complexity`
- **Auth**: Required
- **Request Body**:
```json
{
  "language": "java",
  "code": "public class BubbleSort { ... }",
  "level": "INTERMEDIATE"
}
```
- **Response `200 OK`**:
```json
{
  "timeComplexity": {
    "worst": "O(n²)",
    "average": "O(n²)",
    "best": "O(n)"
  },
  "spaceComplexity": {
    "auxiliary": "O(1)",
    "inPlace": true
  },
  "explanation": "Nested loops execute quadratic comparisons in worst/average case. With the swapped flag, already sorted arrays exit in linear time.",
  "loopMultiplier": {
    "lines": "Lines 5 - 16",
    "operationsFormula": "½(n² - n) comparisons"
  }
}
```

### 6.3 Run Code Review Analysis
- **Method**: `POST`
- **Endpoint**: `/api/analyze/review`
- **Auth**: Required
- **Request Body**:
```json
{
  "language": "java",
  "code": "public class BubbleSort { ... }",
  "level": "INTERMEDIATE"
}
```
- **Response `200 OK`**:
```json
{
  "overallScore": 88,
  "breakdown": {
    "readability": { "score": 92, "note": "Clean variable naming and concise structure." },
    "maintainability": { "score": 74, "note": "Modular method, but loop logic could extract swap function." },
    "reliability": { "score": 68, "note": "Missing defensive bounds check for null or empty array." },
    "bestPractices": { "score": 80, "note": "Follows standard Java conventions and styling." }
  },
  "recommendations": [
    {
      "priority": "HIGH",
      "line": 2,
      "title": "Add defensive input validation",
      "description": "Check for null array before reading length."
    },
    {
      "priority": "MEDIUM",
      "line": 9,
      "title": "Extract swap logic into a helper method",
      "description": "Increases readability and hot loop inline opportunities."
    }
  ]
}
```

### 6.4 Generate Flowchart
- **Method**: `POST`
- **Endpoint**: `/api/analyze/flowchart`
- **Auth**: Required
- **Request Body**:
```json
{
  "language": "java",
  "code": "public class BubbleSort { ... }",
  "level": "INTERMEDIATE"
}
```
- **Response `200 OK`**:
```json
{
  "mermaid": "graph TD\n    A([Start: bubbleSort]) --> B[n = arr.length]\n    B --> C{i < n - 1?}\n    C -- Yes --> D[swapped = false]\n    D --> E{j < n - i - 1?}\n    E -- Yes --> F{arr[j] > arr[j+1]?}\n    F -- Yes --> G[Swap arr[j], arr[j+1]\nswapped = true]\n    F -- No --> H[Next j]\n    G --> H\n    H --> E\n    E -- No --> I{!swapped?}\n    I -- Yes (Break) --> J([End: Array Sorted])\n    I -- No --> K[i++]\n    K --> C\n    C -- No --> J",
  "nodes": [
    { "id": "A", "label": "Start: bubbleSort", "type": "terminal" },
    { "id": "C", "label": "Outer Loop Condition (i < n - 1)", "type": "decision", "line": 5 },
    { "id": "F", "label": "Compare arr[j] > arr[j+1]", "type": "decision", "line": 8 },
    { "id": "G", "label": "Swap Elements", "type": "action", "line": 9 }
  ]
}
```

### 6.5 Comprehensive Analysis (`/api/analyze/all`)
- **Method**: `POST`
- **Endpoint**: `/api/analyze/all`
- **Auth**: Required
- **Request Body**:
```json
{
  "language": "java",
  "code": "public class BubbleSort { ... }",
  "level": "INTERMEDIATE"
}
```
- **Response `200 OK`**:
Returns combined object containing `explanation`, `errors`, `complexity`, `review`, `flowchart`, `optimization`, and `docs`.

### 6.6 Save Analysis Result
- **Method**: `POST`
- **Endpoint**: `/api/analyses`
- **Auth**: Required
- **Request Body**:
```json
{
  "type": "COMPREHENSIVE",
  "language": "java",
  "code": "public class BubbleSort { ... }",
  "resultJson": "{ ... }",
  "projectId": 1,
  "fileId": 101,
  "notes": "Optimal early exit verified for CS201 lab."
}
```
- **Response `201 Created`**:
```json
{
  "id": 501,
  "type": "COMPREHENSIVE",
  "language": "java",
  "qualityScore": 88,
  "createdAt": "2026-10-11T12:30:00Z"
}
```

---

## 7. Analysis History (`/api/history`)

### 7.1 List Analysis History
- **Method**: `GET`
- **Endpoint**: `/api/history?search=&type=&from=&to=&page=0&size=10`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "content": [
    {
      "id": 501,
      "title": "BubbleSort.java Analysis",
      "projectName": "CS201: Data Structures & Algorithms",
      "language": "java",
      "type": "COMPREHENSIVE",
      "qualityScore": 88,
      "issuesCount": 2,
      "notes": "Optimal early exit verified for CS201 lab.",
      "createdAt": "2026-10-11T12:30:00Z"
    },
    {
      "id": 500,
      "title": "QuickSort.java Complexity Audit",
      "projectName": "CS201: Data Structures & Algorithms",
      "language": "java",
      "type": "COMPLEXITY",
      "qualityScore": 96,
      "issuesCount": 0,
      "notes": "Partition verified O(n log n).",
      "createdAt": "2026-10-10T14:22:00Z"
    }
  ],
  "totalElements": 128,
  "totalPages": 13,
  "page": 0,
  "size": 10
}
```

### 7.2 Get History Detail
- **Method**: `GET`
- **Endpoint**: `/api/history/{id}`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": 501,
  "title": "BubbleSort.java Analysis",
  "projectId": 1,
  "projectName": "CS201: Data Structures & Algorithms",
  "language": "java",
  "type": "COMPREHENSIVE",
  "code": "public class BubbleSort { ... }",
  "resultJson": {
    "summary": "Optimized Bubble Sort implementation with early termination.",
    "qualityScore": 88
  },
  "notes": "Optimal early exit verified for CS201 lab.",
  "createdAt": "2026-10-11T12:30:00Z"
}
```

### 7.3 Update Notes
- **Method**: `PUT`
- **Endpoint**: `/api/history/{id}`
- **Auth**: Required
- **Request Body**:
```json
{
  "notes": "Discussed in Lab Session with Prof. Miller. Ready for midterm."
}
```
- **Response `200 OK`**:
```json
{
  "id": 501,
  "notes": "Discussed in Lab Session with Prof. Miller. Ready for midterm.",
  "updatedAt": "2026-10-11T12:40:00Z"
}
```

### 7.4 Delete History Record
- **Method**: `DELETE`
- **Endpoint**: `/api/history/{id}`
- **Auth**: Required
- **Response `204 No Content`**

---

## 8. AI Assistance (`/api/ai`)

### 8.1 Explain Code
- **Method**: `POST`
- **Endpoint**: `/api/ai/explain`
- **Request Body**: `{ "language": "java", "code": "...", "level": "BEGINNER" }`

### 8.2 Debug Code
- **Method**: `POST`
- **Endpoint**: `/api/ai/debug`
- **Request Body**: `{ "language": "java", "code": "...", "level": "INTERMEDIATE" }`

### 8.3 Optimize Code
- **Method**: `POST`
- **Endpoint**: `/api/ai/optimize`
- **Request Body**: `{ "language": "java", "code": "...", "level": "ADVANCED" }`

### 8.4 Generate Documentation
- **Method**: `POST`
- **Endpoint**: `/api/ai/docs`
- **Request Body**: `{ "language": "java", "code": "...", "format": "JAVADOC" }`

### 8.5 Learning Walkthrough & Viva
- **Method**: `POST`
- **Endpoint**: `/api/ai/learning`
- **Request Body**: `{ "topic": "Bubble Sort", "language": "java", "level": "BEGINNER" }`
- **Response `200 OK`**:
```json
{
  "concept": {
    "title": "Bubble Sort Algorithm",
    "description": "Bubble Sort repeatedly swaps adjacent elements if they are in the wrong order until sorted."
  },
  "steps": [
    { "step": 1, "title": "Pass Initialisation", "content": "Start at index 0 and inspect adjacent pairs." },
    { "step": 2, "title": "Inversion Swap", "content": "If arr[j] > arr[j+1], swap them in place." },
    { "step": 3, "title": "Bubbling Effect", "content": "Largest element settles at the end of the array." },
    { "step": 4, "title": "Early Termination", "content": "If no swaps occurred in a pass, break early." }
  ],
  "quiz": [
    {
      "id": 1,
      "question": "What is the best-case time complexity of an optimized Bubble Sort?",
      "options": ["O(1)", "O(n)", "O(n log n)", "O(n²)"],
      "answerIndex": 1,
      "explanation": "When the array is already sorted, the swapped boolean flag breaks out after the first linear pass O(n)."
    }
  ],
  "vivaQuestions": [
    {
      "question": "Why is Bubble Sort considered a stable sorting algorithm?",
      "answer": "It only swaps adjacent elements when arr[j] > arr[j+1]. Equal elements are never swapped, preserving relative order."
    },
    {
      "question": "What is the auxiliary space complexity of Bubble Sort?",
      "answer": "O(1), because it sorts in-place using only a temporary swap variable."
    }
  ]
}
```

### 8.6 Code Comparison
- **Method**: `POST`
- **Endpoint**: `/api/ai/compare`
- **Request Body**:
```json
{
  "language": "java",
  "codeA": "public class BubbleSortStandard { ... }",
  "codeB": "public class BubbleSortOptimized { ... }"
}
```
- **Response `200 OK`**:
```json
{
  "metrics": [
    { "metric": "Time Complexity (Worst)", "variantA": "O(n²)", "variantB": "O(n²)" },
    { "metric": "Time Complexity (Best)", "variantA": "O(n²)", "variantB": "O(n)" },
    { "metric": "Auxiliary Space", "variantA": "O(1)", "variantB": "O(1)" },
    { "metric": "Comparisons on Sorted Input", "variantA": "45 operations", "variantB": "9 operations" },
    { "metric": "Memory Allocations", "variantA": "0 bytes", "variantB": "0 bytes" }
  ],
  "verdict": {
    "winner": "Variant B (Optimized)",
    "summary": "Variant B is significantly faster on nearly-sorted data due to early termination with the boolean swap check.",
    "recommendation": "Adopt Variant B for general educational benchmarks."
  }
}
```

### 8.7 Chat with AI
- **Method**: `POST`
- **Endpoint**: `/api/ai/chat`
- **Request Body**:
```json
{
  "sessionId": "session-101",
  "message": "Why does line 9 need a temporary variable?",
  "includeContext": true,
  "code": "int temp = arr[j];\narr[j] = arr[j+1];\narr[j+1] = temp;"
}
```
- **Response `200 OK`**:
```json
{
  "sessionId": "session-101",
  "messageId": "msg-202",
  "reply": "In Java, assigning `arr[j] = arr[j+1]` overwrites `arr[j]` immediately. Without storing `arr[j]` in `temp` first, its original value would be lost before copying it into `arr[j+1]`.\n\n```java\n// Swapping requires a buffer variable\nint temp = arr[j];\narr[j] = arr[j + 1];\narr[j + 1] = temp;\n```",
  "timestamp": "2026-10-11T12:45:00Z"
}
```

### 8.8 Chat Sessions List
- **Method**: `GET`
- **Endpoint**: `/api/ai/chat/sessions`
- **Auth**: Required
- **Response `200 OK`**:
```json
[
  {
    "id": "session-101",
    "title": "BubbleSort Inversion & Swapping",
    "updatedAt": "2026-10-11T12:45:00Z"
  },
  {
    "id": "session-102",
    "title": "Java 17 Pattern Matching & Records",
    "updatedAt": "2026-10-09T16:10:00Z"
  }
]
```

### 8.9 Get Chat Session Detail
- **Method**: `GET`
- **Endpoint**: `/api/ai/chat/sessions/{id}`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": "session-101",
  "title": "BubbleSort Inversion & Swapping",
  "messages": [
    {
      "id": "msg-201",
      "sender": "user",
      "text": "Why does line 9 need a temporary variable?",
      "timestamp": "2026-10-11T12:44:00Z"
    },
    {
      "id": "msg-202",
      "sender": "ai",
      "text": "In Java, assigning `arr[j] = arr[j+1]` overwrites `arr[j]` immediately...",
      "timestamp": "2026-10-11T12:45:00Z"
    }
  ]
}
```

---

## 9. Reports (`/api/reports`)

### 9.1 List Reports
- **Method**: `GET`
- **Endpoint**: `/api/reports`
- **Auth**: Required
- **Response `200 OK`**:
```json
[
  {
    "id": 1,
    "title": "CS201 Midterm Code Quality Audit",
    "projectName": "CS201: Data Structures & Algorithms",
    "author": "Alex Chen",
    "overallScore": 92,
    "issuesFound": 3,
    "createdAt": "2026-10-10T15:00:00Z"
  },
  {
    "id": 2,
    "title": "BubbleSort.java Algorithm Benchmark",
    "projectName": "CS201: Data Structures & Algorithms",
    "author": "Alex Chen",
    "overallScore": 88,
    "issuesFound": 2,
    "createdAt": "2026-10-11T11:00:00Z"
  }
]
```

### 9.2 Create Report
- **Method**: `POST`
- **Endpoint**: `/api/reports`
- **Auth**: Required
- **Request Body**:
```json
{
  "title": "BubbleSort.java Algorithm Benchmark",
  "projectId": 1,
  "analysisId": 501
}
```
- **Response `201 Created`**:
```json
{
  "id": 2,
  "title": "BubbleSort.java Algorithm Benchmark",
  "overallScore": 88,
  "createdAt": "2026-10-11T11:00:00Z"
}
```

### 9.3 Get Report Detail
- **Method**: `GET`
- **Endpoint**: `/api/reports/{id}`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": 2,
  "title": "BubbleSort.java Algorithm Benchmark",
  "projectName": "CS201: Data Structures & Algorithms",
  "author": "Alex Chen",
  "overallScore": 88,
  "summary": "Algorithm meets criteria with optimal early termination. 2 low-severity issues flagged.",
  "metrics": {
    "readability": 92,
    "maintainability": 74,
    "reliability": 68,
    "bestPractices": 80
  },
  "issues": [
    { "line": 7, "type": "WARNING", "title": "Boundary Inefficiency" },
    { "line": 2, "type": "INFO", "title": "Missing Null Guard" }
  ],
  "createdAt": "2026-10-11T11:00:00Z"
}
```

### 9.4 Download Report PDF
- **Method**: `GET`
- **Endpoint**: `/api/reports/{id}/pdf`
- **Auth**: Required
- **Response `200 OK`**: Binary PDF stream (`Content-Type: application/pdf`)

---

## 10. Notifications (`/api/notifications`)

### 10.1 List Notifications
- **Method**: `GET`
- **Endpoint**: `/api/notifications`
- **Auth**: Required
- **Response `200 OK`**:
```json
[
  {
    "id": 1,
    "title": "Analysis Completed",
    "message": "BubbleSort.java scored 88/100 (Grade A).",
    "read": false,
    "createdAt": "2026-10-11T12:30:00Z"
  },
  {
    "id": 2,
    "title": "Project Health Alert",
    "message": "CS201 Algorithms indexed successfully with 12 files.",
    "read": true,
    "createdAt": "2026-10-10T14:00:00Z"
  }
]
```

### 10.2 Mark Notification as Read
- **Method**: `PUT`
- **Endpoint**: `/api/notifications/{id}/read`
- **Auth**: Required
- **Response `200 OK`**:
```json
{
  "id": 1,
  "read": true
}
```
