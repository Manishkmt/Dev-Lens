/**
 * DevLens Chart.js Integration
 * Visualizes telemetry using Chart.js configured with Stitch design tokens
 */

export function renderActivityChart(canvasId, labels, analysesData, bugsData) {
  const ctx = document.getElementById(canvasId);
  if (!ctx || typeof Chart === 'undefined') return null;

  const isLight = document.documentElement.classList.contains('light');
  const textColor = isLight ? '#475569' : '#94A3B8';
  const gridColor = isLight ? 'rgba(226, 232, 240, 0.8)' : 'rgba(51, 65, 85, 0.4)';

  return new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [
        {
          label: 'Total Analyses',
          data: analysesData,
          borderColor: '#6366F1',
          backgroundColor: isLight ? 'rgba(99, 102, 241, 0.12)' : 'rgba(99, 102, 241, 0.25)',
          fill: true,
          tension: 0.35,
          borderWidth: 2,
          pointBackgroundColor: '#6366F1',
          pointBorderColor: isLight ? '#FFFFFF' : '#0B1326',
          pointRadius: 4,
          pointHoverRadius: 6
        },
        {
          label: 'Bugs Resolved',
          data: bugsData,
          borderColor: '#22D3EE',
          backgroundColor: isLight ? 'rgba(34, 211, 238, 0.08)' : 'rgba(34, 211, 238, 0.15)',
          fill: true,
          tension: 0.35,
          borderWidth: 2,
          pointBackgroundColor: '#22D3EE',
          pointBorderColor: isLight ? '#FFFFFF' : '#0B1326',
          pointRadius: 4,
          pointHoverRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false
      },
      plugins: {
        legend: {
          display: true,
          position: 'top',
          align: 'end',
          labels: {
            boxWidth: 12,
            boxHeight: 12,
            usePointStyle: true,
            color: textColor,
            font: { family: 'Inter', size: 12 }
          }
        },
        tooltip: {
          backgroundColor: isLight ? '#FFFFFF' : '#1E293B',
          titleColor: isLight ? '#0F172A' : '#F8FAFC',
          bodyColor: isLight ? '#475569' : '#94A3B8',
          borderColor: isLight ? '#E2E8F0' : '#334155',
          borderWidth: 1,
          padding: 10,
          boxPadding: 4,
          usePointStyle: true,
          titleFont: { family: 'Inter', size: 12, weight: '600' },
          bodyFont: { family: 'JetBrains Mono', size: 11 }
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: textColor, font: { family: 'Inter', size: 11 } }
        },
        y: {
          grid: { color: gridColor },
          ticks: { color: textColor, font: { family: 'JetBrains Mono', size: 11 } }
        }
      }
    }
  });
}

export function renderDistributionChart(canvasId, labels, data) {
  const ctx = document.getElementById(canvasId);
  if (!ctx || typeof Chart === 'undefined') return null;

  const isLight = document.documentElement.classList.contains('light');
  const textColor = isLight ? '#475569' : '#94A3B8';

  return new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [
        {
          data: data,
          backgroundColor: [
            '#6366F1', // Indigo
            '#22D3EE', // Cyan
            '#10B981', // Emerald
            '#F59E0B'  // Amber
          ],
          borderColor: isLight ? '#FFFFFF' : '#1E293B',
          borderWidth: 2,
          hoverOffset: 4
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '72%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            boxWidth: 10,
            boxHeight: 10,
            usePointStyle: true,
            color: textColor,
            font: { family: 'Inter', size: 11 },
            padding: 14
          }
        },
        tooltip: {
          backgroundColor: isLight ? '#FFFFFF' : '#1E293B',
          titleColor: isLight ? '#0F172A' : '#F8FAFC',
          bodyColor: isLight ? '#475569' : '#94A3B8',
          borderColor: isLight ? '#E2E8F0' : '#334155',
          borderWidth: 1,
          padding: 8,
          bodyFont: { family: 'JetBrains Mono', size: 11 }
        }
      }
    }
  });
}
