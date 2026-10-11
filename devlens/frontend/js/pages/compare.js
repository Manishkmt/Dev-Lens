/**
 * DevLens Code Compare Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { escapeHtml } from '../utils.js';

let editorA = null;
let editorB = null;

const presets = {
  bubble_vs_opt: {
    titleA: "BubbleSortStandard.java",
    titleB: "BubbleSortOptimized.java",
    codeA: `public class BubbleSortStandard {
    public static void sort(int[] arr) {
        int n = arr.length;
        // Always executes all passes without checking if already sorted
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}`,
    codeB: `public class BubbleSortOptimized {
    public static void sort(int[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            // Adaptive early exit breaks out in O(n) time
            if (!swapped) break;
        }
    }
}`
  },
  bubble_vs_quick: {
    titleA: "BubbleSortOptimized.java",
    titleB: "QuickSortLomuto.java",
    codeA: `public class BubbleSortOptimized {
    public static void sort(int[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }
}`,
    codeB: `public class QuickSortLomuto {
    public static void sort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            sort(arr, low, pi - 1);
            sort(arr, pi + 1, high);
        }
    }
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                int temp = arr[++i]; arr[i] = arr[j]; arr[j] = temp;
            }
        }
        int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
        return i + 1;
    }
}`
  }
};

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('compare');

  initDualEditors();
  setupEventListeners();

  await executeComparison();
});

function initDualEditors() {
  const taA = document.getElementById('editor-variant-a');
  const taB = document.getElementById('editor-variant-b');

  if (taA && typeof CodeMirror !== 'undefined') {
    editorA = CodeMirror.fromTextArea(taA, {
      lineNumbers: true,
      mode: 'text/x-java',
      theme: 'material-darker',
      indentUnit: 4,
      tabSize: 4
    });
  }

  if (taB && typeof CodeMirror !== 'undefined') {
    editorB = CodeMirror.fromTextArea(taB, {
      lineNumbers: true,
      mode: 'text/x-java',
      theme: 'material-darker',
      indentUnit: 4,
      tabSize: 4
    });
  }

  loadPreset('bubble_vs_opt');
}

function loadPreset(key) {
  const preset = presets[key];
  if (!preset) return;

  document.getElementById('variant-a-title').textContent = preset.titleA;
  document.getElementById('variant-b-title').textContent = preset.titleB;

  if (editorA) editorA.setValue(preset.codeA);
  if (editorB) editorB.setValue(preset.codeB);
}

function setupEventListeners() {
  const presetSelect = document.getElementById('compare-presets-select');
  presetSelect?.addEventListener('change', (e) => {
    loadPreset(e.target.value);
    executeComparison();
  });

  document.getElementById('run-compare-btn')?.addEventListener('click', executeComparison);
}

async function executeComparison() {
  const btn = document.getElementById('run-compare-btn');
  if (btn) btn.disabled = true;

  try {
    const codeA = editorA ? editorA.getValue() : '';
    const codeB = editorB ? editorB.getValue() : '';

    const result = await api.ai.compare({ language: 'java', codeA, codeB });

    renderVerdict(result.verdict);
    renderMetrics(result.metrics);
    toast.success('Differential analysis calculated');
  } catch (err) {
    console.error('Comparison error:', err);
    toast.error('Failed to compute algorithm comparison');
  } finally {
    if (btn) btn.disabled = false;
  }
}

function renderVerdict(verdict) {
  if (!verdict) return;
  document.getElementById('verdict-winner').textContent = verdict.winner;
  document.getElementById('verdict-summary').textContent = verdict.summary;
  document.getElementById('verdict-recom').textContent = verdict.recommendation;
}

function renderMetrics(metrics) {
  const tbody = document.getElementById('compare-metrics-tbody');
  if (!tbody || !metrics) return;

  tbody.innerHTML = metrics.map(m => `
    <tr>
      <td style="font-weight: 500; color: var(--text-primary); font-size: 13px;">${escapeHtml(m.metric)}</td>
      <td style="font-family: var(--font-code); color: var(--color-amber); font-size: 12px;">${escapeHtml(m.variantA)}</td>
      <td style="font-family: var(--font-code); color: var(--color-emerald); font-size: 12px; font-weight: 600;">${escapeHtml(m.variantB)}</td>
    </tr>
  `).join('');
}
