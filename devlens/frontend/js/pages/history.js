/**
 * DevLens Analysis History Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { escapeHtml, timeAgo, debounce } from '../utils.js';

let currentPage = 0;
const pageSize = 10;
let historyItems = [];

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('history');

  setupEventListeners();
  await loadHistory();
});

async function loadHistory() {
  const search = document.getElementById('history-search-input')?.value.trim() || '';
  const type = document.getElementById('history-type-filter')?.value || 'ALL';

  try {
    const data = await api.history.getAll({ search, type, page: currentPage, size: pageSize });
    historyItems = data.content || [];
    renderHistoryTable(historyItems);

    const info = document.getElementById('history-page-info');
    if (info) {
      info.textContent = `Showing ${historyItems.length > 0 ? currentPage * pageSize + 1 : 0} to ${currentPage * pageSize + historyItems.length} of ${data.totalElements} entries`;
    }
  } catch (err) {
    console.error('Failed to load history:', err);
    toast.error('Failed to load analysis history');
  }
}

function renderHistoryTable(items) {
  const tbody = document.getElementById('history-tbody');
  if (!tbody) return;

  if (items.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2.5rem;">No historical analyses found matching criteria.</td></tr>`;
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td>
        <div style="font-weight: 600; color: var(--text-primary); font-size: 13px;">${escapeHtml(item.title)}</div>
        <div style="font-size: 11px; color: var(--text-muted);">AST ID: #${item.id}</div>
      </td>
      <td>
        <span style="color: var(--text-secondary); font-size: 12.5px;">${escapeHtml(item.projectName)}</span>
      </td>
      <td>
        <span class="badge badge-neutral" style="font-size: 10px;">${item.type}</span>
      </td>
      <td>
        <span class="badge ${item.qualityScore >= 90 ? 'badge-success' : 'badge-primary'}">
          ${item.qualityScore}/100
        </span>
      </td>
      <td>
        <div style="font-size: 12px; color: var(--text-secondary); max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${escapeHtml(item.notes || 'No notes')}">
          ${escapeHtml(item.notes || '—')}
        </div>
      </td>
      <td style="font-family: var(--font-code); font-size: 11.5px; color: var(--text-muted);">
        ${timeAgo(item.createdAt)}
      </td>
      <td>
        <div style="display: flex; align-items: center; gap: 0.25rem;">
          <a href="editor.html?historyId=${item.id}" class="btn btn-sm btn-ghost" title="Re-open in Code Editor">
            <span class="material-symbols-outlined" style="font-size: 16px;">terminal</span>
          </a>
          <button class="btn btn-sm btn-ghost edit-notes-btn" data-id="${item.id}" data-notes="${escapeHtml(item.notes || '')}" title="Edit Viva Notes">
            <span class="material-symbols-outlined" style="font-size: 16px;">edit_note</span>
          </button>
          <button class="btn btn-sm btn-ghost delete-history-btn" data-id="${item.id}" title="Delete Record" style="color: var(--color-rose);">
            <span class="material-symbols-outlined" style="font-size: 16px;">delete</span>
          </button>
        </div>
      </td>
    </tr>
  `).join('');

  // Event handlers
  tbody.querySelectorAll('.edit-notes-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      openNotesModal(btn.dataset.id, btn.dataset.notes);
    });
  });

  tbody.querySelectorAll('.delete-history-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.id;
      if (confirm('Delete this historical analysis record?')) {
        await api.history.delete(id);
        toast.success('Record deleted from history');
        await loadHistory();
      }
    });
  });
}

function setupEventListeners() {
  const searchInput = document.getElementById('history-search-input');
  searchInput?.addEventListener('input', debounce(() => {
    currentPage = 0;
    loadHistory();
  }, 250));

  const typeFilter = document.getElementById('history-type-filter');
  typeFilter?.addEventListener('change', () => {
    currentPage = 0;
    loadHistory();
  });

  // Notes Modal Events
  const modal = document.getElementById('notes-modal');
  document.getElementById('close-notes-modal-btn')?.addEventListener('click', () => modal.classList.remove('open'));
  document.getElementById('cancel-notes-modal-btn')?.addEventListener('click', () => modal.classList.remove('open'));

  document.getElementById('notes-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('notes-item-id').value;
    const notes = document.getElementById('notes-textarea').value.trim();

    try {
      await api.history.updateNotes(id, notes);
      toast.success('Viva notes updated successfully');
      modal.classList.remove('open');
      await loadHistory();
    } catch (err) {
      toast.error('Failed to update notes');
    }
  });
}

function openNotesModal(id, notes) {
  document.getElementById('notes-item-id').value = id;
  document.getElementById('notes-textarea').value = notes || '';
  document.getElementById('notes-modal').classList.add('open');
  document.getElementById('notes-textarea').focus();
}
