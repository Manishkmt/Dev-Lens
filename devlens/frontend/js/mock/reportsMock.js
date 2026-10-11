/**
 * DevLens Mock Reports Module
 */

let initialReports = [
  {
    id: 1,
    title: "CS201 Midterm Code Quality Audit",
    projectName: "CS201: Data Structures & Algorithms",
    projectId: 1,
    author: "Alex Chen",
    overallScore: 92,
    issuesFound: 3,
    summary: "Comprehensive evaluation of student algorithms coursework including sorting routines and tree structures. Code demonstrates high adherence to structural correctness with minor boundary optimizations suggested.",
    metrics: {
      readability: 94,
      maintainability: 88,
      reliability: 90,
      bestPractices: 96
    },
    issues: [
      { line: 9, type: "WARNING", title: "Boundary Inefficiency in BubbleSort" },
      { line: 2, type: "INFO", title: "Missing Null Check in bubbleSort Entry" },
      { line: 34, type: "INFO", title: "Non-final local variables in Partition loop" }
    ],
    createdAt: "2026-10-10T15:00:00Z"
  },
  {
    id: 2,
    title: "BubbleSort.java Algorithm Benchmark Report",
    projectName: "CS201: Data Structures & Algorithms",
    projectId: 1,
    author: "Alex Chen",
    overallScore: 88,
    issuesFound: 2,
    summary: "Automated static analysis and asymptotic verification for BubbleSort.java. Verified O(n²) worst-case and adaptive O(n) early break. Confidence grade: A.",
    metrics: {
      readability: 92,
      maintainability: 74,
      reliability: 68,
      bestPractices: 80
    },
    issues: [
      { line: 9, type: "WARNING", title: "Boundary Inefficiency: Redundant tail comparisons" },
      { line: 2, type: "INFO", title: "Missing Null Guard on array parameter" }
    ],
    createdAt: "2026-10-11T11:00:00Z"
  },
  {
    id: 3,
    title: "Distributed Task Scheduler Architecture Review",
    projectName: "Distributed Task Scheduler",
    projectId: 2,
    author: "Alex Chen",
    overallScore: 86,
    issuesFound: 4,
    summary: "Concurrency and locking evaluation for thread pool queue implementation. Synchronized collections checked for race conditions.",
    metrics: {
      readability: 85,
      maintainability: 82,
      reliability: 84,
      bestPractices: 88
    },
    issues: [
      { line: 42, type: "WARNING", title: "Potential Thread Starvation in PriorityQueue poll" }
    ],
    createdAt: "2026-10-09T18:30:00Z"
  }
];

export function mockGetReports() {
  return [...initialReports];
}

export function mockGetReportDetail(id) {
  const report = initialReports.find(r => r.id === Number(id));
  if (!report) {
    throw { status: 404, error: "Report not found" };
  }
  return { ...report };
}

export function mockCreateReport({ title, projectId, analysisId }) {
  if (!title || !title.trim()) {
    throw { status: 400, error: "Report title is required" };
  }

  const newReport = {
    id: Math.max(...initialReports.map(r => r.id), 0) + 1,
    title: title.trim(),
    projectName: "CS201: Data Structures & Algorithms",
    projectId: Number(projectId) || 1,
    author: "Alex Chen",
    overallScore: 88,
    issuesFound: 2,
    summary: "Newly generated analysis audit report. All AST checks passed.",
    metrics: {
      readability: 90,
      maintainability: 85,
      reliability: 80,
      bestPractices: 88
    },
    issues: [
      { line: 9, type: "WARNING", title: "Boundary Inefficiency" },
      { line: 2, type: "INFO", title: "Defensive Check Recommendation" }
    ],
    createdAt: new Date().toISOString()
  };

  initialReports.unshift(newReport);
  return newReport;
}
