/**
 * DevLens Reports Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api, USE_MOCK } from '../api.js';
import { toast } from '../toast.js';
import { escapeHtml, timeAgo } from '../utils.js';

let reportsList = [];

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('reports');

  setupEventListeners();
  await loadReports();
});

async function loadReports() {
  try {
    reportsList = await api.reports.getAll();
    renderReports(reportsList);
  } catch (err) {
    console.error('Failed to load reports:', err);
    toast.error('Failed to load reports');
  }
}

function renderReports(list) {
  const grid = document.getElementById('reports-grid');
  if (!grid) return;

  if (list.length === 0) {
    grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-muted);">No reports created yet. Click "Generate Audit Report" above to build your first report.</div>`;
    return;
  }

  grid.innerHTML = list.map(r => `
    <article class="card card-hover" style="display: flex; flex-direction: column; justify-content: space-between;">
      <div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <span class="badge badge-primary">Report #${r.id}</span>
          <span class="badge ${r.overallScore >= 90 ? 'badge-success' : 'badge-primary'}">
            Score: ${r.overallScore}/100
          </span>
        </div>

        <h3 style="font-size: 1.1rem; font-weight: 600; margin-bottom: 0.35rem;">
          <a href="report-view.html?id=${r.id}" style="color: var(--text-primary); text-decoration: none;">
            ${escapeHtml(r.title)}
          </a>
        </h3>
        <p style="font-size: 12.5px; color: var(--text-secondary); margin-bottom: 0.75rem;">
          ${escapeHtml(r.projectName)}
        </p>

        <div style="background: var(--bg-lowest); padding: 0.625rem 0.75rem; border-radius: var(--radius-sm); font-size: 12px; color: var(--text-muted); margin-bottom: 1rem;">
          <span>Issues flagged: <strong style="color: var(--color-rose);">${r.issuesFound}</strong></span>
          <span style="margin: 0 4px;">•</span>
          <span>Author: ${escapeHtml(r.author || 'Alex Chen')}</span>
        </div>
      </div>

      <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--border-subtle); padding-top: 0.75rem;">
        <span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted);">
          ${timeAgo(r.createdAt)}
        </span>

        <div style="display: flex; gap: 0.4rem;">
          <!-- Download PDF Button (Disabled in Mock mode with tooltip) -->
          <button class="btn btn-sm btn-secondary download-pdf-btn" data-id="${r.id}" ${USE_MOCK ? 'disabled title="PDF generation available when backend is connected"' : 'title="Download PDF"'}>
            <span class="material-symbols-outlined" style="font-size: 15px;">picture_as_pdf</span>
            <span>PDF</span>
          </button>

          <a href="report-view.html?id=${r.id}" class="btn btn-sm btn-primary">
            <span>View</span>
            <span class="material-symbols-outlined" style="font-size: 14px;">arrow_forward</span>
          </a>
        </div>
      </div>
    </article>
  `).join('');

  // Attach PDF tooltip click
  grid.querySelectorAll('.download-pdf-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      if (USE_MOCK) {
        toast.info('PDF generation available when backend is connected');
      }
    });
  });
}

function setupEventListeners() {
  const modal = document.getElementById('report-modal');
  const openModalBtn = document.getElementById('open-new-report-modal-btn');
  const closeModalBtn = document.getElementById('close-report-modal-btn');
  const cancelModalBtn = document.getElementById('cancel-report-modal-btn');
  const form = document.getElementById('report-form');

  openModalBtn?.addEventListener('click', () => {
    document.getElementById('report-title-input').value = '';
    modal.classList.add('open');
  });

  closeModalBtn?.addEventListener('click', () => modal.classList.remove('open'));
  cancelModalBtn?.addEventListener('click', () => modal.classList.remove('open'));

  form?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('report-title-input').value.trim();
    const projectId = document.getElementById('report-project-select').value;

    if (!title) return;

    try {
      const created = await api.reports.create({ title, projectId });
      reportsList.unshift(created);
      renderReports(reportsList);
      modal.classList.remove('open');
      toast.success('Quality audit report generated!');
    } catch (err) {
      toast.error('Failed to create report');
    }
  });
}
