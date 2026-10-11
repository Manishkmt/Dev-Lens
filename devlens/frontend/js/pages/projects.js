/**
 * DevLens Projects Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { escapeHtml, timeAgo, debounce } from '../utils.js';

let allProjects = [];
let deleteTargetId = null;

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('projects');

  setupProjectEventListeners();

  // Check URL query param for search filter (?q=...)
  const urlParams = new URLSearchParams(window.location.search);
  const q = urlParams.get('q');
  if (q) {
    const searchInput = document.getElementById('project-search-input');
    if (searchInput) searchInput.value = q;
  }

  await loadProjects();
});

async function loadProjects() {
  try {
    allProjects = await api.projects.getAll();
    updateMetrics();
    applyFilters();
  } catch (error) {
    console.error('Failed to load projects:', error);
    toast.error('Failed to load projects.');
  }
}

function updateMetrics() {
  const countEl = document.getElementById('metric-repo-count');
  if (countEl) countEl.textContent = `${allProjects.length} Codebases`;
}

function applyFilters() {
  const searchInput = document.getElementById('project-search-input');
  const langFilter = document.getElementById('language-filter');
  const sortFilter = document.getElementById('sort-filter');

  const query = (searchInput?.value || '').toLowerCase().trim();
  const selectedLang = langFilter?.value || 'all';
  const selectedSort = sortFilter?.value || 'recent';

  let filtered = allProjects.filter(p => {
    const matchesSearch = !query || 
      p.name.toLowerCase().includes(query) || 
      (p.description && p.description.toLowerCase().includes(query)) ||
      p.language.toLowerCase().includes(query);
    const matchesLang = selectedLang === 'all' || p.language.toLowerCase() === selectedLang.toLowerCase();
    return matchesSearch && matchesLang;
  });

  // Sorting
  if (selectedSort === 'score') {
    filtered.sort((a, b) => (b.qualityScore || 0) - (a.qualityScore || 0));
  } else if (selectedSort === 'name') {
    filtered.sort((a, b) => a.name.localeCompare(b.name));
  } else if (selectedSort === 'files') {
    filtered.sort((a, b) => (b.fileCount || 0) - (a.fileCount || 0));
  } else {
    // recent
    filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  }

  renderProjectCards(filtered);
}

function renderProjectCards(projects) {
  const grid = document.getElementById('projects-grid');
  if (!grid) return;

  if (projects.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 4rem 1rem; color: var(--text-muted);">
        <span class="material-symbols-outlined" style="font-size: 42px; opacity: 0.5; margin-bottom: 0.5rem;">folder_off</span>
        <h3 style="font-size: 16px; font-weight: 600; color: var(--text-primary);">No matching projects found</h3>
        <p style="font-size: 13px; margin-top: 4px;">Try refining your search keyword or create a new project above.</p>
      </div>
    `;
    return;
  }

  grid.innerHTML = projects.map(p => {
    const score = p.qualityScore || 85;
    const scoreClass = score >= 90 ? 'badge-success' : score >= 80 ? 'badge-primary' : 'badge-warning';

    return `
      <article class="card card-hover" style="display: flex; flex-direction: column; justify-content: space-between; position: relative;">
        <!-- Top Gradient Accent Strip -->
        <div style="position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, var(--color-primary), var(--color-secondary)); border-top-left-radius: var(--radius-lg); border-top-right-radius: var(--radius-lg);"></div>

        <div>
          <!-- Header Badges -->
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.75rem;">
            <div style="display: flex; align-items: center; gap: 0.35rem;">
              <span class="lang-badge ${p.language || 'java'}">${(p.language || 'java').toUpperCase()}</span>
              <span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted); background: var(--bg-lowest); padding: 1px 6px; border-radius: var(--radius-xs);">
                v17 LTS
              </span>
            </div>
            <div class="badge ${scoreClass}">
              <span class="material-symbols-outlined" style="font-size: 14px;">bolt</span>
              <span>${score} / 100</span>
            </div>
          </div>

          <!-- Title & Description -->
          <h3 style="font-size: 1.05rem; font-weight: 600; margin-bottom: 0.35rem;">
            <a href="project-detail.html?id=${p.id}" style="color: var(--text-primary); text-decoration: none;" class="project-title-link">
              ${escapeHtml(p.name)}
            </a>
          </h3>
          <p style="font-size: 12.5px; color: var(--text-secondary); line-height: 1.5; margin-bottom: 1rem;">
            ${escapeHtml(p.description || 'No description provided.')}
          </p>

          <!-- Micro Metric Pill -->
          <div style="background: var(--bg-lowest); padding: 0.5rem 0.75rem; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: space-between; font-size: 12px; margin-bottom: 1rem;">
            <span style="color: var(--text-muted);">Memory Profile</span>
            <span style="font-family: var(--font-code); color: var(--color-secondary); font-weight: 600;">O(log n) avg</span>
          </div>
        </div>

        <!-- Footer Meta & Actions -->
        <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--border-subtle); padding-top: 0.75rem; font-size: 12px; color: var(--text-muted);">
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <span title="Tracked files count">📁 ${p.fileCount || 0} files</span>
            <span>•</span>
            <span title="Last analyzed">${timeAgo(p.lastAnalyzed || p.createdAt)}</span>
          </div>

          <div style="display: flex; align-items: center; gap: 0.25rem;">
            <a href="project-detail.html?id=${p.id}" class="btn btn-sm btn-ghost" title="Open Project Details">
              <span class="material-symbols-outlined" style="font-size: 16px;">visibility</span>
            </a>
            <button class="btn btn-sm btn-ghost edit-project-btn" data-id="${p.id}" title="Edit Project">
              <span class="material-symbols-outlined" style="font-size: 16px;">edit</span>
            </button>
            <button class="btn btn-sm btn-ghost delete-project-btn" data-id="${p.id}" data-name="${escapeHtml(p.name)}" title="Delete Project" style="color: var(--color-rose);">
              <span class="material-symbols-outlined" style="font-size: 16px;">delete</span>
            </button>
          </div>
        </div>
      </article>
    `;
  }).join('');

  // Attach card button listeners
  grid.querySelectorAll('.edit-project-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      openEditModal(btn.dataset.id);
    });
  });

  grid.querySelectorAll('.delete-project-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      openDeleteModal(btn.dataset.id, btn.dataset.name);
    });
  });
}

function setupProjectEventListeners() {
  const searchInput = document.getElementById('project-search-input');
  if (searchInput) {
    searchInput.addEventListener('input', debounce(() => applyFilters(), 250));
  }

  const langFilter = document.getElementById('language-filter');
  if (langFilter) {
    langFilter.addEventListener('change', () => applyFilters());
  }

  const sortFilter = document.getElementById('sort-filter');
  if (sortFilter) {
    sortFilter.addEventListener('change', () => applyFilters());
  }

  // Create Modal Open
  document.getElementById('open-create-modal-btn')?.addEventListener('click', () => {
    openCreateModal();
  });

  // Modal Closers
  document.getElementById('close-project-modal-btn')?.addEventListener('click', closeProjectModal);
  document.getElementById('cancel-project-modal-btn')?.addEventListener('click', closeProjectModal);

  // Form Submit
  document.getElementById('project-form')?.addEventListener('submit', handleProjectFormSubmit);

  // Delete Modal Events
  document.getElementById('close-delete-modal-btn')?.addEventListener('click', closeDeleteModal);
  document.getElementById('cancel-delete-btn')?.addEventListener('click', closeDeleteModal);
  document.getElementById('confirm-delete-btn')?.addEventListener('click', handleDeleteProject);

  // Keyboard shortcut '/' for search
  document.addEventListener('keydown', (e) => {
    if (e.key === '/' && document.activeElement !== searchInput) {
      e.preventDefault();
      searchInput?.focus();
    }
  });
}

function openCreateModal() {
  document.getElementById('project-modal-title').textContent = 'Create New Project';
  document.getElementById('edit-project-id').value = '';
  document.getElementById('proj-name').value = '';
  document.getElementById('proj-desc').value = '';
  document.getElementById('proj-lang').value = 'java';
  document.getElementById('proj-name-error').style.display = 'none';

  document.getElementById('project-modal').classList.add('open');
  document.getElementById('proj-name').focus();
}

function openEditModal(id) {
  const proj = allProjects.find(p => p.id === Number(id));
  if (!proj) return;

  document.getElementById('project-modal-title').textContent = 'Edit Project';
  document.getElementById('edit-project-id').value = proj.id;
  document.getElementById('proj-name').value = proj.name;
  document.getElementById('proj-desc').value = proj.description || '';
  document.getElementById('proj-lang').value = proj.language || 'java';
  document.getElementById('proj-name-error').style.display = 'none';

  document.getElementById('project-modal').classList.add('open');
  document.getElementById('proj-name').focus();
}

function closeProjectModal() {
  document.getElementById('project-modal').classList.remove('open');
}

async function handleProjectFormSubmit(e) {
  e.preventDefault();

  const id = document.getElementById('edit-project-id').value;
  const name = document.getElementById('proj-name').value.trim();
  const desc = document.getElementById('proj-desc').value.trim();
  const lang = document.getElementById('proj-lang').value;
  const errorEl = document.getElementById('proj-name-error');

  if (!name) {
    errorEl.textContent = 'Project title is required';
    errorEl.style.display = 'block';
    return;
  }
  errorEl.style.display = 'none';

  try {
    if (id) {
      const updated = await api.projects.update(id, { name, description: desc, language: lang });
      const idx = allProjects.findIndex(p => p.id === Number(id));
      if (idx !== -1) allProjects[idx] = { ...allProjects[idx], ...updated };
      toast.success('Project updated successfully');
    } else {
      const created = await api.projects.create({ name, description: desc, language: lang });
      allProjects.unshift(created);
      toast.success('Project created successfully');
    }

    closeProjectModal();
    updateMetrics();
    applyFilters();
  } catch (err) {
    console.error('Save project error:', err);
    toast.error(err.error || 'Failed to save project');
  }
}

function openDeleteModal(id, name) {
  deleteTargetId = id;
  document.getElementById('delete-project-name').textContent = `"${name}"`;
  document.getElementById('delete-modal').classList.add('open');
}

function closeDeleteModal() {
  deleteTargetId = null;
  document.getElementById('delete-modal').classList.remove('open');
}

async function handleDeleteProject() {
  if (!deleteTargetId) return;

  try {
    await api.projects.delete(deleteTargetId);
    allProjects = allProjects.filter(p => p.id !== Number(deleteTargetId));
    toast.success('Project deleted successfully');
    closeDeleteModal();
    updateMetrics();
    applyFilters();
  } catch (err) {
    console.error('Delete project error:', err);
    toast.error(err.error || 'Failed to delete project');
  }
}
