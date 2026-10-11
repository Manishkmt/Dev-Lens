/**
 * DevLens Shared Layout Shell
 * Injects responsive sidebar and topbar, manages active navigation, theme toggle, and notifications
 */

import { auth } from './auth.js';
import { theme } from './theme.js';
import { api } from './api.js';
import { toast } from './toast.js';

export function initLayout(activeKey = 'dashboard') {
  const user = auth.getUser() || { name: 'Alex Chen', email: 'alex.chen@university.edu', role: 'Staff Engineer' };
  const isPagesDir = window.location.pathname.includes('/pages/');
  const rootPath = isPagesDir ? '../' : './';
  const pagesPath = isPagesDir ? '' : 'pages/';

  // Render Sidebar
  const sidebarContainer = document.getElementById('sidebar-container');
  if (sidebarContainer) {
    sidebarContainer.innerHTML = `
      <aside class="sidebar" id="app-sidebar">
        <div>
          <div class="sidebar-header">
            <a href="${pagesPath}dashboard.html" style="display: flex; align-items: center; gap: 0.75rem; text-decoration: none;">
              <img src="${rootPath}assets/logo.svg" alt="DevLens Logo" class="sidebar-logo"/>
              <div class="sidebar-brand-text">
                <span class="sidebar-brand-name">DevLens</span>
                <span class="sidebar-brand-version">Dev Core v2.4</span>
              </div>
            </a>
          </div>

          <nav class="sidebar-nav">
            <div class="sidebar-section-title">Workspace</div>
            <a href="${pagesPath}dashboard.html" class="nav-link ${activeKey === 'dashboard' ? 'active' : ''}" data-key="dashboard">
              <span class="material-symbols-outlined">dashboard</span>
              <span>Dashboard</span>
            </a>
            <a href="${pagesPath}projects.html" class="nav-link ${activeKey === 'projects' ? 'active' : ''}" data-key="projects">
              <span class="material-symbols-outlined">folder_open</span>
              <span>Projects</span>
            </a>
            <a href="${pagesPath}editor.html" class="nav-link ${activeKey === 'editor' ? 'active' : ''}" data-key="editor">
              <span class="material-symbols-outlined">terminal</span>
              <span>Code Editor</span>
            </a>
            <a href="${pagesPath}history.html" class="nav-link ${activeKey === 'history' ? 'active' : ''}" data-key="history">
              <span class="material-symbols-outlined">history_toggle_off</span>
              <span>Analysis History</span>
            </a>

            <div class="sidebar-section-title">Intelligence</div>
            <a href="${pagesPath}learning.html" class="nav-link ${activeKey === 'learning' ? 'active' : ''}" data-key="learning">
              <span class="material-symbols-outlined">school</span>
              <span>Learning Mode</span>
            </a>
            <a href="${pagesPath}compare.html" class="nav-link ${activeKey === 'compare' ? 'active' : ''}" data-key="compare">
              <span class="material-symbols-outlined">difference</span>
              <span>Code Compare</span>
            </a>
            <a href="${pagesPath}assistant.html" class="nav-link ${activeKey === 'assistant' ? 'active' : ''}" data-key="assistant">
              <span class="material-symbols-outlined" style="color: var(--color-secondary);">smart_toy</span>
              <span>AI Assistant</span>
              <span class="badge badge-ai" style="margin-left: auto; font-size: 10px; padding: 1px 6px;">AI</span>
            </a>
            <a href="${pagesPath}reports.html" class="nav-link ${activeKey === 'reports' ? 'active' : ''}" data-key="reports">
              <span class="material-symbols-outlined">insights</span>
              <span>Reports</span>
            </a>
          </nav>
        </div>

        <div class="sidebar-footer">
          <a href="${pagesPath}settings.html" class="nav-link ${activeKey === 'settings' ? 'active' : ''}" data-key="settings">
            <span class="material-symbols-outlined">settings</span>
            <span>Settings</span>
          </a>
          <div class="system-status-indicator">
            <span>Status: Healthy</span>
            <span class="status-dot-pulse"></span>
          </div>
        </div>
      </aside>
    `;
  }

  // Render Topbar
  const topbarContainer = document.getElementById('topbar-container');
  if (topbarContainer) {
    const currentTheme = theme.getTheme();
    const themeIcon = currentTheme === 'light' ? 'dark_mode' : 'light_mode';
    const initials = user.name ? user.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() : 'AC';

    topbarContainer.innerHTML = `
      <header class="topbar">
        <div style="display: flex; align-items: center; gap: 0.75rem; flex: 1;">
          <button class="mobile-menu-toggle" id="mobile-sidebar-toggle" aria-label="Toggle Navigation">
            <span class="material-symbols-outlined">menu</span>
          </button>
          <div class="topbar-search-box input-with-icon">
            <span class="material-symbols-outlined">search</span>
            <input type="text" class="form-input" id="global-search-input" placeholder="Search codebase, symbols, commits..."/>
            <button class="input-action-btn" id="search-kbd-btn" title="Search Shortcut">
              <kbd>⌘K</kbd>
            </button>
          </div>
        </div>

        <div class="topbar-actions">
          <!-- Notification Bell -->
          <div style="position: relative;">
            <button class="topbar-btn" id="notif-btn" aria-label="Notifications" title="Notifications">
              <span class="material-symbols-outlined">notifications</span>
              <span class="badge-dot" id="notif-badge"></span>
            </button>
            <div class="dropdown-menu" id="notif-dropdown">
              <div class="dropdown-header" style="display: flex; justify-content: space-between; align-items: center;">
                <span>Notifications</span>
                <button id="mark-all-read-btn" style="background:none; border:none; color:var(--color-primary-light); font-size:11px; cursor:pointer;">Mark all read</button>
              </div>
              <div id="notif-list-container">
                <div style="padding: 1rem; text-align: center; color: var(--text-muted);">Loading notifications...</div>
              </div>
            </div>
          </div>

          <!-- Theme Toggle -->
          <button class="topbar-btn theme-toggle-btn" id="theme-toggle-btn" aria-label="Toggle Theme" title="Toggle Light/Dark Theme">
            <span class="material-symbols-outlined">${themeIcon}</span>
          </button>

          <div style="width: 1px; height: 18px; background: var(--border-color); margin: 0 4px;"></div>

          <!-- User Menu -->
          <div class="user-profile-menu">
            <button class="user-profile-btn" id="user-menu-btn" aria-label="User Profile">
              <div class="user-avatar-img">${initials}</div>
              <div class="user-info-text" style="display: none; @media(min-width: 768px){display:flex;}">
                <span class="user-display-name">${user.name || 'Alex Chen'}</span>
                <span class="user-role-badge">${user.role === 'ADMIN' ? 'Instructor' : 'Student Pro'}</span>
              </div>
              <span class="material-symbols-outlined" style="font-size: 16px; color: var(--text-muted);">expand_more</span>
            </button>
            <div class="dropdown-menu" id="user-dropdown">
              <div class="dropdown-header">
                <div>${user.name || 'Alex Chen'}</div>
                <div style="text-transform: none; font-weight: 400; font-size: 11px; color: var(--text-muted);">${user.email || 'alex.chen@university.edu'}</div>
              </div>
              <a href="${pagesPath}settings.html" class="dropdown-item">
                <span class="material-symbols-outlined" style="font-size: 18px;">person</span>
                <span>Account Settings</span>
              </a>
              <a href="${pagesPath}history.html" class="dropdown-item">
                <span class="material-symbols-outlined" style="font-size: 18px;">history</span>
                <span>My Analyses</span>
              </a>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item" id="logout-btn" style="color: var(--color-rose);">
                <span class="material-symbols-outlined" style="font-size: 18px;">logout</span>
                <span>Log Out</span>
              </div>
            </div>
          </div>
        </div>
      </header>
    `;
  }

  // Setup layout events
  setupLayoutEvents();
}

function setupLayoutEvents() {
  // Mobile sidebar toggle
  const mobileToggle = document.getElementById('mobile-sidebar-toggle');
  const sidebar = document.getElementById('app-sidebar');
  if (mobileToggle && sidebar) {
    mobileToggle.addEventListener('click', () => {
      sidebar.classList.toggle('mobile-open');
    });
  }

  // Theme toggle
  const themeBtn = document.getElementById('theme-toggle-btn');
  if (themeBtn) {
    themeBtn.addEventListener('click', () => {
      const newTheme = theme.toggle();
      toast.info(`Switched to ${newTheme} mode`);
    });
  }

  // User menu dropdown toggle
  const userBtn = document.getElementById('user-menu-btn');
  const userDropdown = document.getElementById('user-dropdown');
  if (userBtn && userDropdown) {
    userBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      userDropdown.classList.toggle('show');
      const notifDropdown = document.getElementById('notif-dropdown');
      if (notifDropdown) notifDropdown.classList.remove('show');
    });
  }

  // Logout action
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      auth.logout();
    });
  }

  // Notification dropdown toggle & fetch
  const notifBtn = document.getElementById('notif-btn');
  const notifDropdown = document.getElementById('notif-dropdown');
  if (notifBtn && notifDropdown) {
    notifBtn.addEventListener('click', async (e) => {
      e.stopPropagation();
      notifDropdown.classList.toggle('show');
      if (userDropdown) userDropdown.classList.remove('show');

      if (notifDropdown.classList.contains('show')) {
        await loadNotifications();
      }
    });
  }

  const markAllReadBtn = document.getElementById('mark-all-read-btn');
  if (markAllReadBtn) {
    markAllReadBtn.addEventListener('click', async (e) => {
      e.stopPropagation();
      await api.notifications.markAllRead();
      const badge = document.getElementById('notif-badge');
      if (badge) badge.style.display = 'none';
      await loadNotifications();
      toast.success('All notifications marked as read');
    });
  }

  // Close dropdowns on outside click
  document.addEventListener('click', () => {
    if (userDropdown) userDropdown.classList.remove('show');
    if (notifDropdown) notifDropdown.classList.remove('show');
  });

  // Global search shortcut ⌘K or Ctrl+K
  document.addEventListener('keydown', (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      const searchInput = document.getElementById('global-search-input');
      if (searchInput) searchInput.focus();
    }
  });

  const searchInput = document.getElementById('global-search-input');
  if (searchInput) {
    searchInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && searchInput.value.trim()) {
        const isPagesDir = window.location.pathname.includes('/pages/');
        const target = isPagesDir ? 'projects.html' : 'pages/projects.html';
        window.location.href = `${target}?q=${encodeURIComponent(searchInput.value.trim())}`;
      }
    });
  }
}

async function loadNotifications() {
  const container = document.getElementById('notif-list-container');
  if (!container) return;

  try {
    const list = await api.notifications.getAll();
    if (!list || list.length === 0) {
      container.innerHTML = `<div style="padding: 1rem; text-align: center; color: var(--text-muted); font-size: 12px;">No new notifications</div>`;
      return;
    }

    container.innerHTML = list.map(item => `
      <div class="dropdown-item notif-item ${item.read ? '' : 'unread'}" data-id="${item.id}" style="flex-direction: column; align-items: flex-start; gap: 2px; padding: 0.6rem 1rem; ${item.read ? 'opacity:0.7;' : 'background:var(--bg-surface-high);'}">
        <div style="display: flex; justify-content: space-between; width: 100%; font-size: 12px; font-weight: 600; color: var(--text-primary);">
          <span>${item.title}</span>
          <span style="font-size: 10px; color: var(--text-muted);">${item.timeAgo || 'Recently'}</span>
        </div>
        <div style="font-size: 11.5px; color: var(--text-secondary);">${item.message}</div>
      </div>
    `).join('');

    container.querySelectorAll('.notif-item').forEach(el => {
      el.addEventListener('click', async () => {
        const id = el.dataset.id;
        await api.notifications.markRead(id);
        el.style.opacity = '0.7';
        el.style.background = 'transparent';
      });
    });
  } catch (err) {
    container.innerHTML = `<div style="padding: 1rem; text-align: center; color: var(--color-rose); font-size: 12px;">Failed to load notifications</div>`;
  }
}
