/**
 * DevLens Mock Analysis History Module
 */

let initialHistory = [
  {
    id: 501,
    title: "BubbleSort.java Algorithm Analysis",
    projectName: "CS201: Data Structures & Algorithms",
    projectId: 1,
    fileId: 101,
    language: "java",
    type: "ALL",
    qualityScore: 88,
    issuesCount: 2,
    notes: "Verified early termination flag with Prof. Miller. Ready for midterm submission.",
    code: `public class BubbleSort {
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }
}`,
    createdAt: "2026-10-11T12:30:00Z"
  },
  {
    id: 500,
    title: "QuickSort.java Partitioning Audit",
    projectName: "CS201: Data Structures & Algorithms",
    projectId: 1,
    fileId: 102,
    language: "java",
    type: "COMPLEXITY",
    qualityScore: 96,
    issuesCount: 0,
    notes: "Lomuto partition verified O(n log n) expected time.",
    code: `public class QuickSort { ... }`,
    createdAt: "2026-10-10T14:22:00Z"
  },
  {
    id: 499,
    title: "BinarySearchTree.java Balancing Review",
    projectName: "CS201: Data Structures & Algorithms",
    projectId: 1,
    fileId: 103,
    language: "java",
    type: "REVIEW",
    qualityScore: 91,
    issuesCount: 1,
    notes: "Recursive insert verified. Recommended AVL self-balancing rotation.",
    code: `public class BinarySearchTree { ... }`,
    createdAt: "2026-10-09T16:15:00Z"
  },
  {
    id: 498,
    title: "ThreadPoolTaskExecutor Thread Safety",
    projectName: "Distributed Task Scheduler",
    projectId: 2,
    fileId: 201,
    language: "java",
    type: "ERRORS",
    qualityScore: 86,
    issuesCount: 3,
    notes: "Added ReentrantLock to prevent queue race condition.",
    code: `public class ThreadPoolTaskExecutor { ... }`,
    createdAt: "2026-10-09T09:40:00Z"
  },
  {
    id: 497,
    title: "Lexer.cpp Grammar Tokenizer",
    projectName: "Compiler Lexer & LL(1) Parser",
    projectId: 4,
    fileId: 401,
    language: "cpp",
    type: "FLOWCHART",
    qualityScore: 82,
    issuesCount: 2,
    notes: "State transition diagram synthesized from regex AST.",
    code: `class Lexer { ... }`,
    createdAt: "2026-10-06T12:10:00Z"
  }
];

export function mockGetHistory({ search = "", type = "", from = "", to = "", page = 0, size = 10 } = {}) {
  let filtered = [...initialHistory];

  if (search && search.trim()) {
    const q = search.toLowerCase();
    filtered = filtered.filter(h =>
      h.title.toLowerCase().includes(q) ||
      h.projectName.toLowerCase().includes(q) ||
      (h.notes && h.notes.toLowerCase().includes(q))
    );
  }

  if (type && type !== "ALL") {
    filtered = filtered.filter(h => h.type.toUpperCase() === type.toUpperCase());
  }

  const totalElements = filtered.length;
  const totalPages = Math.ceil(totalElements / size) || 1;
  const start = page * size;
  const content = filtered.slice(start, start + size);

  return {
    content,
    totalElements,
    totalPages,
    page: Number(page),
    size: Number(size)
  };
}

export function mockGetHistoryDetail(id) {
  const item = initialHistory.find(h => h.id === Number(id));
  if (!item) {
    throw { status: 404, error: "History record not found" };
  }
  return { ...item };
}

export function mockUpdateNotes(id, notes) {
  const item = initialHistory.find(h => h.id === Number(id));
  if (!item) {
    throw { status: 404, error: "History record not found" };
  }
  item.notes = notes;
  item.updatedAt = new Date().toISOString();
  return { ...item };
}

export function mockDeleteHistory(id) {
  const index = initialHistory.findIndex(h => h.id === Number(id));
  if (index === -1) {
    throw { status: 404, error: "History record not found" };
  }
  initialHistory.splice(index, 1);
  return { success: true };
}
