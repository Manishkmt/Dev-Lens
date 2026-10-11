/**
 * DevLens Project Detail Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { escapeHtml, timeAgo } from '../utils.js';

let currentProjectId = null;
let projectFiles = [];

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('projects');

  const urlParams = new URLSearchParams(window.location.search);
  currentProjectId = urlParams.get('id') || 1;

  setupEventListeners();
  await loadProjectDetail();
});

async function loadProjectDetail() {
  try {
    const project = await api.projects.getById(currentProjectId);
    if (project) {
      document.getElementById('proj-title').textContent = project.name;
      document.getElementById('proj-description').textContent = project.description || '';
      document.getElementById('proj-lang-badge').textContent = (project.language || 'java').toUpperCase();
      document.getElementById('proj-health-text').textContent = `${project.qualityScore || 90} / 100`;
      
      const openEditorBtn = document.getElementById('open-editor-btn');
      if (openEditorBtn) openEditorBtn.href = `editor.html?projectId=${project.id}`;
    }

    projectFiles = await api.files.getByProject(currentProjectId);
    renderFilesTable(projectFiles);

  } catch (err) {
    console.error('Failed to load project detail:', err);
    toast.error('Failed to load project details.');
  }
}

function renderFilesTable(files) {
  const tbody = document.getElementById('project-files-tbody');
  const badge = document.getElementById('files-count-badge');
  if (badge) badge.textContent = `${files.length} Files Tracked`;

  if (!tbody) return;

  if (files.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2.5rem;">No files added yet. Click "+ Add / Upload File" above to get started.</td></tr>`;
    return;
  }

  tbody.innerHTML = files.map(f => `
    <tr>
      <td>
        <div style="display: flex; align-items: center; gap: 0.5rem; font-weight: 600; color: var(--text-primary);">
          <span class="material-symbols-outlined" style="font-size: 18px; color: var(--color-primary-light);">description</span>
          <a href="editor.html?fileId=${f.id}" style="color: var(--text-primary); text-decoration: none;">
            ${escapeHtml(f.name)}
          </a>
        </div>
      </td>
      <td style="font-family: var(--font-code); font-size: 11.5px; color: var(--text-muted);">
        ${escapeHtml(f.path || f.name)}
      </td>
      <td style="font-family: var(--font-code); font-size: 11.5px; color: var(--text-secondary);">
        ${f.size ? (f.size / 1024).toFixed(1) + ' KB' : '1.2 KB'}
      </td>
      <td>
        <span class="badge ${f.qualityScore >= 90 ? 'badge-success' : 'badge-primary'}">
          ${f.qualityScore || 85}/100
        </span>
      </td>
      <td>
        ${f.issuesCount > 0 
          ? `<span class="badge badge-warning">${f.issuesCount} Issues</span>` 
          : `<span class="badge badge-success">Clean AST</span>`}
      </td>
      <td style="font-family: var(--font-code); font-size: 11.5px; color: var(--text-muted);">
        ${timeAgo(f.lastAnalyzed)}
      </td>
      <td>
        <div style="display: flex; align-items: center; gap: 0.35rem;">
          <a href="editor.html?fileId=${f.id}" class="btn btn-sm btn-primary" style="height: 28px; padding: 0 8px; font-size: 11px;">
            <span class="material-symbols-outlined" style="font-size: 14px;">bolt</span>
            <span>Analyze</span>
          </a>
          <button class="btn btn-sm btn-ghost delete-file-btn" data-id="${f.id}" title="Delete file" style="color: var(--color-rose); padding: 4px;">
            <span class="material-symbols-outlined" style="font-size: 16px;">delete</span>
          </button>
        </div>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.delete-file-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      const fileId = btn.dataset.id;
      if (confirm('Are you sure you want to delete this file?')) {
        await api.files.delete(fileId);
        projectFiles = projectFiles.filter(f => f.id !== Number(fileId));
        renderFilesTable(projectFiles);
        toast.success('File deleted successfully');
      }
    });
  });
}

function setupEventListeners() {
  const modal = document.getElementById('file-modal');
  const openModalBtn = document.getElementById('open-add-file-modal-btn');
  const closeModalBtn = document.getElementById('close-file-modal-btn');
  const cancelModalBtn = document.getElementById('cancel-file-modal-btn');
  const fileForm = document.getElementById('file-form');

  openModalBtn?.addEventListener('click', () => {
    document.getElementById('file-name-input').value = '';
    document.getElementById('file-path-input').value = '';
    document.getElementById('file-content-input').value = '';
    modal.classList.add('open');
  });

  closeModalBtn?.addEventListener('click', () => modal.classList.remove('open'));
  cancelModalBtn?.addEventListener('click', () => modal.classList.remove('open'));

  fileForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('file-name-input').value.trim();
    const path = document.getElementById('file-path-input').value.trim();
    const content = document.getElementById('file-content-input').value;

    if (!name) return;

    try {
      const newFile = await api.files.add(currentProjectId, { name, path, content });
      projectFiles.push(newFile);
      renderFilesTable(projectFiles);
      modal.classList.remove('open');
      toast.success('File added to project!');
    } catch (err) {
      toast.error('Failed to add file');
    }
  });
}
