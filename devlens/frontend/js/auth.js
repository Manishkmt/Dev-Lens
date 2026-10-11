/**
 * DevLens Auth Helper Module
 * Manages localStorage token, user session, and route guard
 */

import { api } from './api.js';

export const auth = {
  getToken() {
    return localStorage.getItem('devlens_token');
  },

  getUser() {
    const userStr = localStorage.getItem('devlens_user');
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch (e) {
      return null;
    }
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  setSession(token, user) {
    localStorage.setItem('devlens_token', token);
    localStorage.setItem('devlens_user', JSON.stringify(user));
  },

  clearSession() {
    localStorage.removeItem('devlens_token');
    localStorage.removeItem('devlens_user');
  },

  logout() {
    this.clearSession();
    // Redirect to login page
    const isPagesDir = window.location.pathname.includes('/pages/');
    window.location.href = isPagesDir ? 'login.html' : 'pages/login.html';
  },

  // Route guard: call at the top of protected pages
  guard() {
    if (!this.isAuthenticated()) {
      const isPagesDir = window.location.pathname.includes('/pages/');
      window.location.href = isPagesDir ? 'login.html' : 'pages/login.html';
      return false;
    }
    return true;
  }
};
