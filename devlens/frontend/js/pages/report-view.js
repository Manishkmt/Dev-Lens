/**
 * DevLens Report View Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { formatDate, escapeHtml } from '../utils.js';

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('reports');

  const urlParams = new URLSearchParams(window.location.search);
  const reportId = urlParams.get('id') || 1;

  document.getElementById('print-report-btn')?.addEventListener('click', () => {
    window.print();
  });

  await loadReport(reportId);
});

async function loadReport(id) {
  try {
    const r = await api.reports.getById(id);
    if (!r) return;

    document.getElementById('doc-title').textContent = r.title;
    document.getElementById('doc-project').textContent = `Project: ${r.projectName}`;
    document.getElementById('doc-score').textContent = `Score: ${r.overallScore} / 100`;
    document.getElementById('doc-date').textContent = formatDate(r.createdAt);
    document.getElementById('doc-summary').textContent = r.summary || '';
    document.getElementById('doc-author').textContent = r.author || 'Alex Chen';

    if (r.metrics) {
      document.getElementById('metric-readability').textContent = `${r.metrics.readability || 90}%`;
      document.getElementById('metric-maintainability').textContent = `${r.metrics.maintainability || 80}%`;
      document.getElementById('metric-reliability').textContent = `${r.metrics.reliability || 75}%`;
      document.getElementById('metric-practices').textContent = `${r.metrics.bestPractices || 85}%`;
    }

    const tbody = document.getElementById('doc-issues-tbody');
    if (tbody && r.issues) {
      tbody.innerHTML = r.issues.map(iss => `
        <tr>
          <td style="font-family: var(--font-code); font-size: 12px; color: var(--color-primary-light);">Line ${iss.line}</td>
          <td>
            <span class="badge ${iss.type === 'WARNING' ? 'badge-warning' : 'badge-neutral'}">
              ${iss.type}
            </span>
          </td>
          <td style="font-size: 13px; color: var(--text-primary);">${escapeHtml(iss.title)}</td>
        </tr>
      `).join('');
    }

  } catch (err) {
    console.error('Failed to load report:', err);
    toast.error('Failed to load report document');
  }
}
