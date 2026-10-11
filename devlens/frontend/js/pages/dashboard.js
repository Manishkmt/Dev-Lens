/**
 * DevLens Dashboard Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { renderActivityChart, renderDistributionChart } from '../charts.js';
import { formatDate, timeAgo } from '../utils.js';

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('dashboard');

  const user = auth.getUser();
  if (user && user.name) {
    const heading = document.getElementById('welcome-heading');
    if (heading) heading.textContent = `Welcome back, ${user.name.split(' ')[0]} 👋`;
  }

  await loadDashboardData();
});

async function loadDashboardData() {
  try {
    // 1. Load Stats
    const stats = await api.dashboard.getStats();
    if (stats) {
      document.getElementById('stat-total-analyses').textContent = stats.totalAnalyses;
      document.getElementById('stat-analyses-growth').textContent = stats.analysesGrowth;
      document.getElementById('stat-bugs-fixed').textContent = stats.bugsDetectedAndFixed;
      document.getElementById('stat-high-severity').textContent = `${stats.highSeverityResolved} high severity resolved`;
      document.getElementById('stat-quality-score').textContent = stats.avgQualityScore;
      document.getElementById('stat-quality-tier').textContent = stats.qualityTier;
      document.getElementById('stat-active-projects').textContent = stats.activeProjects;
      document.getElementById('welcome-subtext').textContent = `All systems normal. ${stats.activeProjects} projects indexed, ${stats.totalAnalyses} total analyses completed across active workspaces.`;
    }

    // 2. Load Chart Data
    const chartData = await api.dashboard.getChartData();
    if (chartData) {
      renderActivityChart(
        'analyses-activity-chart',
        chartData.activity.labels,
        chartData.activity.analysesCount,
        chartData.activity.bugsFound
      );

      renderDistributionChart(
        'analyses-distribution-chart',
        chartData.distribution.labels,
        chartData.distribution.data
      );
    }

    // 3. Load Recent Analyses
    const historyData = await api.history.getAll({ page: 0, size: 5 });
    renderRecentAnalyses(historyData.content || []);

  } catch (error) {
    console.error('Failed to load dashboard data:', error);
  }
}

function renderRecentAnalyses(items) {
  const tbody = document.getElementById('recent-analyses-tbody');
  if (!tbody) return;

  if (items.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No analysis records found. Run an analysis in the Code Editor!</td></tr>`;
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td>
        <div style="font-weight: 600; color: var(--text-primary); font-size: 13px;">${item.title}</div>
        <div style="font-size: 11px; color: var(--text-muted);">${item.notes || 'Automated AST run'}</div>
      </td>
      <td>
        <span style="color: var(--text-secondary); font-size: 12.5px;">${item.projectName}</span>
      </td>
      <td>
        <span class="lang-badge ${item.language || 'java'}">${(item.language || 'java').toUpperCase()}</span>
      </td>
      <td>
        <span class="badge ${item.qualityScore >= 90 ? 'badge-success' : item.qualityScore >= 80 ? 'badge-primary' : 'badge-warning'}">
          ${item.qualityScore}/100
        </span>
      </td>
      <td>
        ${item.issuesCount > 0 
          ? `<span class="badge badge-warning">${item.issuesCount} Issues</span>` 
          : `<span class="badge badge-success">Clean AST</span>`}
      </td>
      <td style="font-family: var(--font-code); font-size: 11.5px; color: var(--text-muted);">
        ${timeAgo(item.createdAt)}
      </td>
      <td>
        <a href="editor.html?historyId=${item.id}" class="btn btn-sm btn-ghost" title="Open in Code Editor">
          <span class="material-symbols-outlined" style="font-size: 16px;">terminal</span>
        </a>
      </td>
    </tr>
  `).join('');
}
