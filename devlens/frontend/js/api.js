/**
 * DevLens Central API Gateway
 * Controls mock vs live backend routing via granular per-module MOCK_CONFIG flags
 */

import * as authMock from './mock/authMock.js';
import * as usersMock from './mock/usersMock.js';
import * as projectsMock from './mock/projectsMock.js';
import * as filesMock from './mock/filesMock.js';
import * as dashboardMock from './mock/dashboardMock.js';
import * as analysesMock from './mock/analysesMock.js';
import * as historyMock from './mock/historyMock.js';
import * as aiMock from './mock/aiMock.js';
import * as reportsMock from './mock/reportsMock.js';
import * as notificationsMock from './mock/notificationsMock.js';

/**
 * Granular Mock Configuration
 * Module 2 endpoints are wired directly to the live Spring Boot backend (port 8080).
 * Modules 3 and 4 remain routed to mock data until their respective phases.
 */
export const MOCK_CONFIG = {
  auth: false,          // Module 2: Live Spring Boot REST API
  users: false,         // Module 2: Live Spring Boot REST API
  projects: false,      // Module 2: Live Spring Boot REST API
  files: false,         // Module 2: Live Spring Boot REST API
  dashboard: false,     // Module 2: Live Spring Boot REST API
  history: false,       // Module 2: Live Spring Boot REST API
  analysesSave: false,  // Module 2: Live Spring Boot REST API
  analyze: false,       // Module 3: Live Code Analysis Engine!
  ai: false,            // Module 4: Live AI Integration!
  reports: true,        // Module 5: PDF & Reports (Mock until Module 5)
  notifications: true   // Module 5: Notifications (Mock until Module 5)
};

// Global fallback flag for backward compatibility
export const USE_MOCK = false;
export const API_BASE_URL = "http://localhost:8080";

// Helper for simulated network delay in mock mode
const delay = (ms = 350) => new Promise(resolve => setTimeout(resolve, ms));

// Helper for fetching auth token from localStorage
function getAuthToken() {
  return localStorage.getItem('devlens_token') || '';
}

// Live backend fetch helper
async function request(endpoint, options = {}) {
  const token = getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    let errorData;
    try {
      errorData = await response.json();
    } catch (e) {
      errorData = { status: response.status, error: response.statusText };
    }

    // Auto-clear stale or invalid JWT tokens and route to login
    if (response.status === 401 && !endpoint.startsWith('/api/auth/')) {
      localStorage.removeItem('devlens_token');
      localStorage.removeItem('devlens_user');
      const isPagesDir = window.location.pathname.includes('/pages/');
      const loginUrl = isPagesDir ? 'login.html' : 'pages/login.html';
      if (!window.location.pathname.endsWith('login.html')) {
        window.location.href = loginUrl;
      }
    }

    throw errorData;
  }

  if (response.status === 204) return null;
  return await response.json();
}

export const api = {
  // Authentication
  auth: {
    login: async (email, password) => {
      if (MOCK_CONFIG.auth) {
        await delay();
        return authMock.mockLogin(email, password);
      }
      return request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });
    },

    register: async (name, email, password) => {
      if (MOCK_CONFIG.auth) {
        await delay();
        return authMock.mockRegister(name, email, password);
      }
      return request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ name, email, password })
      });
    }
  },

  // User Profile
  users: {
    getMe: async () => {
      if (MOCK_CONFIG.users) {
        await delay(200);
        return usersMock.mockGetMe();
      }
      return request('/api/users/me');
    },

    updateMe: async (data) => {
      if (MOCK_CONFIG.users) {
        await delay(300);
        return usersMock.mockUpdateMe(data);
      }
      return request('/api/users/me', {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    changePassword: async (data) => {
      if (MOCK_CONFIG.users) {
        await delay(400);
        return usersMock.mockChangePassword(data);
      }
      return request('/api/users/me/password', {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    }
  },

  // Projects
  projects: {
    getAll: async () => {
      if (MOCK_CONFIG.projects) {
        await delay(250);
        return projectsMock.mockGetProjects();
      }
      return request('/api/projects');
    },

    getById: async (id) => {
      if (MOCK_CONFIG.projects) {
        await delay(250);
        return projectsMock.mockGetProject(id);
      }
      return request(`/api/projects/${id}`);
    },

    create: async (data) => {
      if (MOCK_CONFIG.projects) {
        await delay(350);
        return projectsMock.mockCreateProject(data);
      }
      return request('/api/projects', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    update: async (id, data) => {
      if (MOCK_CONFIG.projects) {
        await delay(300);
        return projectsMock.mockUpdateProject(id, data);
      }
      return request(`/api/projects/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    delete: async (id) => {
      if (MOCK_CONFIG.projects) {
        await delay(300);
        return projectsMock.mockDeleteProject(id);
      }
      return request(`/api/projects/${id}`, {
        method: 'DELETE'
      });
    }
  },

  // Project Files
  files: {
    getByProject: async (projectId) => {
      if (MOCK_CONFIG.files) {
        await delay(250);
        return filesMock.mockGetProjectFiles(projectId);
      }
      return request(`/api/projects/${projectId}/files`);
    },

    getById: async (id) => {
      if (MOCK_CONFIG.files) {
        await delay(250);
        return filesMock.mockGetFile(id);
      }
      return request(`/api/files/${id}`);
    },

    add: async (projectId, data) => {
      if (MOCK_CONFIG.files) {
        await delay(350);
        return filesMock.mockAddFile(projectId, data);
      }
      return request(`/api/projects/${projectId}/files`, {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    update: async (id, data) => {
      if (MOCK_CONFIG.files) {
        await delay(300);
        return filesMock.mockUpdateFile(id, data);
      }
      return request(`/api/files/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    delete: async (id) => {
      if (MOCK_CONFIG.files) {
        await delay(300);
        return filesMock.mockDeleteFile(id);
      }
      return request(`/api/files/${id}`, {
        method: 'DELETE'
      });
    }
  },

  // Dashboard
  dashboard: {
    getStats: async () => {
      if (MOCK_CONFIG.dashboard) {
        await delay(200);
        return dashboardMock.mockGetStats();
      }
      return request('/api/dashboard/stats');
    },

    getChartData: async () => {
      if (MOCK_CONFIG.dashboard) {
        await delay(250);
        return dashboardMock.mockGetChartData();
      }
      return request('/api/dashboard/chart-data');
    }
  },

  // Code Analysis Engine
  analyze: {
    errors: async ({ language, code, level }) => {
      if (MOCK_CONFIG.analyze) {
        await delay(500);
        return analysesMock.mockAnalyzeErrors(language, code, level);
      }
      return request('/api/analyze/errors', {
        method: 'POST',
        body: JSON.stringify({ language, code, level })
      });
    },

    complexity: async ({ language, code, level }) => {
      if (MOCK_CONFIG.analyze) {
        await delay(500);
        return analysesMock.mockAnalyzeComplexity(language, code, level);
      }
      return request('/api/analyze/complexity', {
        method: 'POST',
        body: JSON.stringify({ language, code, level })
      });
    },

    review: async ({ language, code, level }) => {
      if (MOCK_CONFIG.analyze) {
        await delay(500);
        return analysesMock.mockAnalyzeReview(language, code, level);
      }
      return request('/api/analyze/review', {
        method: 'POST',
        body: JSON.stringify({ language, code, level })
      });
    },

    flowchart: async ({ language, code, level }) => {
      if (MOCK_CONFIG.analyze) {
        await delay(500);
        return analysesMock.mockAnalyzeFlowchart(language, code, level);
      }
      return request('/api/analyze/flowchart', {
        method: 'POST',
        body: JSON.stringify({ language, code, level })
      });
    },

    all: async ({ language, code, level }) => {
      if (MOCK_CONFIG.analyze) {
        await delay(600);
        return analysesMock.mockAnalyzeAll(language, code, level);
      }
      return request('/api/analyze/all', {
        method: 'POST',
        body: JSON.stringify({ language, code, level })
      });
    },

    save: async (data) => {
      if (MOCK_CONFIG.analysesSave) {
        await delay(350);
        return analysesMock.mockSaveAnalysis(data);
      }
      return request('/api/analyses', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    }
  },

  // History
  history: {
    getAll: async (params = {}) => {
      if (MOCK_CONFIG.history) {
        await delay(300);
        return historyMock.mockGetHistory(params);
      }
      // Strip empty or null keys from params
      const cleanParams = {};
      Object.keys(params).forEach(k => {
        if (params[k] !== undefined && params[k] !== null && params[k] !== '') {
          cleanParams[k] = params[k];
        }
      });
      const query = new URLSearchParams(cleanParams).toString();
      const endpoint = query ? `/api/history?${query}` : '/api/history';
      return request(endpoint);
    },

    getById: async (id) => {
      if (MOCK_CONFIG.history) {
        await delay(250);
        return historyMock.mockGetHistoryDetail(id);
      }
      return request(`/api/history/${id}`);
    },

    updateNotes: async (id, notes) => {
      if (MOCK_CONFIG.history) {
        await delay(300);
        return historyMock.mockUpdateNotes(id, notes);
      }
      return request(`/api/history/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ notes })
      });
    },

    delete: async (id) => {
      if (MOCK_CONFIG.history) {
        await delay(300);
        return historyMock.mockDeleteHistory(id);
      }
      return request(`/api/history/${id}`, {
        method: 'DELETE'
      });
    }
  },

  // AI & Learning (Module 4)
  ai: {
    explain: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(350);
        return analysesMock.mockAnalyzeExplanation(payload.language, payload.code, payload.level);
      }
      return request('/api/ai/explain', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    },

    debug: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(350);
        return analysesMock.mockAnalyzeErrors(payload.language, payload.code, payload.level);
      }
      return request('/api/ai/debug', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    },

    optimize: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(350);
        return analysesMock.mockAnalyzeOptimization(payload.language, payload.code, payload.level);
      }
      return request('/api/ai/optimize', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    },

    docs: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(350);
        return analysesMock.mockAnalyzeDocs(payload.language, payload.code, payload.level);
      }
      return request('/api/ai/docs', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    },

    chat: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(450);
        return aiMock.mockAiChat(payload);
      }
      return request('/api/ai/chat', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    },

    getChatSessions: async () => {
      if (MOCK_CONFIG.ai) {
        await delay(200);
        return aiMock.mockGetChatSessions();
      }
      return request('/api/ai/chat/sessions');
    },

    getChatSessionDetail: async (id) => {
      if (MOCK_CONFIG.ai) {
        await delay(250);
        return aiMock.mockGetChatSessionDetail(id);
      }
      return request(`/api/ai/chat/sessions/${id}`);
    },

    learning: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(350);
        return aiMock.mockAiLearning(payload);
      }
      return request('/api/ai/learning', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    },

    compare: async (payload) => {
      if (MOCK_CONFIG.ai) {
        await delay(500);
        return aiMock.mockAiCompare(payload);
      }
      return request('/api/ai/compare', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    }
  },

  // Reports (Module 4)
  reports: {
    getAll: async () => {
      if (MOCK_CONFIG.reports) {
        await delay(250);
        return reportsMock.mockGetReports();
      }
      return request('/api/reports');
    },

    getById: async (id) => {
      if (MOCK_CONFIG.reports) {
        await delay(250);
        return reportsMock.mockGetReportDetail(id);
      }
      return request(`/api/reports/${id}`);
    },

    create: async (data) => {
      if (MOCK_CONFIG.reports) {
        await delay(350);
        return reportsMock.mockCreateReport(data);
      }
      return request('/api/reports', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    }
  },

  // Notifications (Module 4)
  notifications: {
    getAll: async () => {
      if (MOCK_CONFIG.notifications) {
        await delay(150);
        return notificationsMock.mockGetNotifications();
      }
      return request('/api/notifications');
    },

    markRead: async (id) => {
      if (MOCK_CONFIG.notifications) {
        return notificationsMock.mockMarkNotificationRead(id);
      }
      return request(`/api/notifications/${id}/read`, {
        method: 'PUT'
      });
    },

    markAllRead: async () => {
      if (MOCK_CONFIG.notifications) {
        return notificationsMock.mockMarkAllNotificationsRead();
      }
      return request('/api/notifications/read-all', {
        method: 'PUT'
      });
    }
  }
};
