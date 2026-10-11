/**
 * DevLens Mock Projects Module
 */

let initialProjects = [
  {
    id: 1,
    name: "CS201: Data Structures & Algorithms",
    description: "Core curriculum benchmark with AVL Trees, Dijkstra graph traversal, and dual-pivot quicksort.",
    language: "java",
    qualityScore: 94,
    fileCount: 4,
    createdAt: "2026-09-15T08:00:00Z",
    lastAnalyzed: "2026-10-10T14:22:00Z"
  },
  {
    id: 2,
    name: "Distributed Task Scheduler",
    description: "Thread pool executor and priority queue implementation in Java with bounded buffers.",
    language: "java",
    qualityScore: 86,
    fileCount: 3,
    createdAt: "2026-09-20T11:00:00Z",
    lastAnalyzed: "2026-10-09T18:15:00Z"
  },
  {
    id: 3,
    name: "Async HTTP Gateway Microservice",
    description: "Non-blocking request dispatcher with token bucket rate limiting and retry backoff.",
    language: "java",
    qualityScore: 91,
    fileCount: 6,
    createdAt: "2026-09-25T14:30:00Z",
    lastAnalyzed: "2026-10-08T09:40:00Z"
  },
  {
    id: 4,
    name: "Compiler Lexer & LL(1) Parser",
    description: "Deterministic finite automaton token generator for mathematical grammar expressions.",
    language: "cpp",
    qualityScore: 82,
    fileCount: 5,
    createdAt: "2026-09-28T16:00:00Z",
    lastAnalyzed: "2026-10-06T12:10:00Z"
  },
  {
    id: 5,
    name: "Neural Network Forward Pass",
    description: "Tensor matrix multiplication and activation gradient calculations in pure Python.",
    language: "python",
    qualityScore: 78,
    fileCount: 3,
    createdAt: "2026-10-01T10:00:00Z",
    lastAnalyzed: "2026-10-05T17:20:00Z"
  },
  {
    id: 6,
    name: "Realtime Collaborative Canvas",
    description: "WebSocket binary message stream handler with conflict-free replicated data types.",
    language: "typescript",
    qualityScore: 89,
    fileCount: 8,
    createdAt: "2026-10-03T13:45:00Z",
    lastAnalyzed: "2026-10-07T11:05:00Z"
  }
];

export function mockGetProjects() {
  return [...initialProjects];
}

export function mockGetProject(id) {
  const proj = initialProjects.find(p => p.id === Number(id));
  if (!proj) {
    throw { status: 404, error: "Project not found" };
  }
  return { ...proj };
}

export function mockCreateProject({ name, description, language }) {
  if (!name || !name.trim()) {
    throw {
      status: 400,
      error: "Validation failed",
      fields: { name: "Project title is required" }
    };
  }

  const newProj = {
    id: Math.max(...initialProjects.map(p => p.id), 0) + 1,
    name: name.trim(),
    description: description || "No description provided.",
    language: language || "java",
    qualityScore: 0,
    fileCount: 0,
    createdAt: new Date().toISOString(),
    lastAnalyzed: null
  };

  initialProjects.unshift(newProj);
  return newProj;
}

export function mockUpdateProject(id, { name, description, language }) {
  const index = initialProjects.findIndex(p => p.id === Number(id));
  if (index === -1) {
    throw { status: 404, error: "Project not found" };
  }

  if (name) initialProjects[index].name = name.trim();
  if (description !== undefined) initialProjects[index].description = description;
  if (language) initialProjects[index].language = language;
  initialProjects[index].updatedAt = new Date().toISOString();

  return { ...initialProjects[index] };
}

export function mockDeleteProject(id) {
  const index = initialProjects.findIndex(p => p.id === Number(id));
  if (index === -1) {
    throw { status: 404, error: "Project not found" };
  }
  initialProjects.splice(index, 1);
  return { success: true };
}
