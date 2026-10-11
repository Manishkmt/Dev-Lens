/**
 * DevLens Code Editor & Deep Semantic Analysis Suite
 * Integrates CodeMirror 5, Mermaid.js, and multi-tabbed AST inspection
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { copyToClipboard, downloadFile, escapeHtml } from '../utils.js';

let editor = null;
let currentLevel = 'INTERMEDIATE';
let currentLanguage = 'java';
let currentAnalysisData = null;
let currentZoom = 1;
let currentLineMark = null;

// Initial Code Samples
const codeSamples = {
  "BubbleSort.java": `package com.algorithms;

/**
 * Bubble Sort algorithm implementation with early termination optimization.
 */
public class BubbleSort {
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // Swap adjacent elements
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break; // Optimized early exit
        }
    }
}`,
  "QuickSort.java": `package com.algorithms;

public class QuickSort {
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }
}`,
  "BinarySearchTree.java": `package com.structures;

public class BinarySearchTree {
    static class Node {
        int key;
        Node left, right;
        public Node(int item) { key = item; }
    }
    Node root;

    public void insert(int key) {
        root = insertRec(root, key);
    }

    private Node insertRec(Node root, int key) {
        if (root == null) return new Node(key);
        if (key < root.key) root.left = insertRec(root.left, key);
        else if (key > root.key) root.right = insertRec(root.right, key);
        return root;
    }
}`
};

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('editor');

  initCodeMirror();
  initMermaid();
  setupToolbarEvents();
  setupAnalysisTabEvents();
  setupFlowchartControls();

  // Run initial analysis automatically on load
  await runAnalysis();
});

function initCodeMirror() {
  const textarea = document.getElementById('codemirror-editor');
  if (!textarea) return;

  editor = CodeMirror.fromTextArea(textarea, {
    lineNumbers: true,
    mode: 'text/x-java',
    theme: 'material-darker',
    lineWrapping: false,
    indentUnit: 4,
    tabSize: 4,
    smartIndent: true,
    styleActiveLine: true,
    matchBrackets: true
  });

  // Set initial content
  editor.setValue(codeSamples["BubbleSort.java"]);

  // Track Cursor Position
  editor.on('cursorActivity', () => {
    const pos = editor.getCursor();
    const cursorEl = document.getElementById('cursor-pos-label');
    if (cursorEl) cursorEl.textContent = `Ln ${pos.line + 1}, Col ${pos.ch + 1}`;
  });

  // Track Line & Char Count
  editor.on('change', () => {
    const lineCount = editor.lineCount();
    const charCount = editor.getValue().length;
    document.getElementById('line-count-label').textContent = `${lineCount} lines`;
    document.getElementById('char-count-label').textContent = `${charCount} chars`;
  });
}

function initMermaid() {
  if (typeof mermaid !== 'undefined') {
    mermaid.initialize({
      startOnLoad: false,
      theme: 'dark',
      themeVariables: {
        darkMode: true,
        background: '#0B1326',
        primaryColor: '#1E293B',
        primaryTextColor: '#F8FAFC',
        primaryBorderColor: '#6366F1',
        lineColor: '#5DE6FF',
        secondaryColor: '#131B2E',
        tertiaryColor: '#171F33'
      }
    });
  }
}

function setupToolbarEvents() {
  // File Tabs Switching
  const tabsContainer = document.getElementById('editor-file-tabs');
  tabsContainer?.querySelectorAll('.file-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      tabsContainer.querySelectorAll('.file-tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      const filename = tab.dataset.filename;
      if (codeSamples[filename]) {
        editor.setValue(codeSamples[filename]);
        document.getElementById('active-file-label').textContent = `src/algorithms/${filename}`;
        runAnalysis();
      }
    });
  });

  // Language Selector
  const langSelect = document.getElementById('editor-lang-select');
  langSelect?.addEventListener('change', (e) => {
    currentLanguage = e.target.value;
    const modeMap = {
      java: 'text/x-java',
      python: 'text/x-python',
      cpp: 'text/x-c++src',
      c: 'text/x-csrc',
      javascript: 'text/javascript'
    };
    editor.setOption('mode', modeMap[currentLanguage] || 'text/x-java');
    document.getElementById('footer-lang-label').textContent = langSelect.options[langSelect.selectedIndex].text;
  });

  // Detail Level Pills
  const levelContainer = document.getElementById('level-pill-selector');
  levelContainer?.querySelectorAll('.level-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      levelContainer.querySelectorAll('.level-btn').forEach(b => {
        b.classList.remove('active', 'btn-secondary');
        b.classList.add('btn-ghost');
        b.style.color = 'var(--text-secondary)';
      });
      btn.classList.add('active', 'btn-secondary');
      btn.classList.remove('btn-ghost');
      btn.style.color = 'var(--color-secondary)';
      currentLevel = btn.dataset.level;
      toast.info(`Detail level: ${currentLevel}`);
      runAnalysis();
    });
  });

  // Analyze Button (and ⌘↵ / Ctrl+Enter shortcut)
  document.getElementById('analyze-code-btn')?.addEventListener('click', runAnalysis);
  document.addEventListener('keydown', (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key === 'Enter') {
      e.preventDefault();
      runAnalysis();
    }
  });

  // Save Analysis Button
  document.getElementById('save-analysis-btn')?.addEventListener('click', async () => {
    try {
      const saved = await api.analyze.save({
        type: 'COMPREHENSIVE',
        language: currentLanguage,
        code: editor.getValue(),
        notes: `Saved analysis at ${new Date().toLocaleTimeString()}`
      });
      toast.success(`Analysis saved to history (ID: #${saved.id})`);
    } catch (err) {
      toast.error('Failed to save analysis');
    }
  });

  // Upload Code File
  const fileUpload = document.getElementById('code-file-upload');
  fileUpload?.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (evt) => {
        editor.setValue(evt.target.result);
        document.getElementById('active-file-label').textContent = file.name;
        toast.success(`Uploaded ${file.name}`);
        runAnalysis();
      };
      reader.readAsText(file);
    }
  });

  // Format Code
  document.getElementById('format-code-btn')?.addEventListener('click', () => {
    const totalLines = editor.lineCount();
    for (let i = 0; i < totalLines; i++) {
      editor.indentLine(i);
    }
    toast.success('Code formatted');
  });

  // Clear Code
  document.getElementById('clear-code-btn')?.addEventListener('click', () => {
    if (confirm('Clear editor contents?')) {
      editor.setValue('');
      toast.info('Editor cleared');
    }
  });

  // Copy Code
  document.getElementById('copy-code-btn')?.addEventListener('click', async () => {
    await copyToClipboard(editor.getValue());
    toast.success('Code copied to clipboard');
  });

  // Copy Javadoc
  document.getElementById('copy-javadoc-btn')?.addEventListener('click', async () => {
    const javadoc = document.getElementById('docs-javadoc-container')?.textContent;
    if (javadoc) {
      await copyToClipboard(javadoc);
      toast.success('Javadoc copied to clipboard');
    }
  });
}

function setupAnalysisTabEvents() {
  const tabsBar = document.getElementById('analysis-tabs-bar');
  tabsBar?.querySelectorAll('.analysis-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      tabsBar.querySelectorAll('.analysis-tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      const tabName = btn.dataset.tab;
      document.querySelectorAll('.analysis-tab-panel').forEach(panel => {
        panel.classList.remove('active');
      });

      const targetPanel = document.getElementById(`tab-panel-${tabName}`);
      if (targetPanel) targetPanel.classList.add('active');

      if (tabName === 'flowchart') {
        renderMermaidFlowchart();
      }
    });
  });

  // Loop multiplier card click highlight
  document.getElementById('loop-multiplier-card')?.addEventListener('click', () => {
    highlightEditorLines(6, 17, 'cm-line-highlight-focus');
    toast.info('Highlighted nested loop bounds in editor');
  });
}

function setupFlowchartControls() {
  const diagramWrap = document.getElementById('mermaid-diagram-container');

  document.getElementById('flowchart-zoom-in')?.addEventListener('click', () => {
    currentZoom = Math.min(currentZoom + 0.2, 2.5);
    if (diagramWrap) diagramWrap.style.transform = `scale(${currentZoom})`;
  });

  document.getElementById('flowchart-zoom-out')?.addEventListener('click', () => {
    currentZoom = Math.max(currentZoom - 0.2, 0.5);
    if (diagramWrap) diagramWrap.style.transform = `scale(${currentZoom})`;
  });

  document.getElementById('flowchart-zoom-reset')?.addEventListener('click', () => {
    currentZoom = 1;
    if (diagramWrap) diagramWrap.style.transform = `scale(1)`;
  });

  document.getElementById('export-flowchart-svg-btn')?.addEventListener('click', () => {
    const svgEl = document.querySelector('#mermaid-diagram-container svg');
    if (svgEl) {
      const svgData = new XMLSerializer().serializeToString(svgEl);
      downloadFile('DevLens-Flowchart.svg', svgData, 'image/svg+xml');
      toast.success('Flowchart exported as SVG');
    } else {
      toast.error('No rendered flowchart available to export');
    }
  });
}

async function runAnalysis() {
  const code = editor.getValue();
  const loading = document.getElementById('analysis-loading-state');
  const analyzeBtn = document.getElementById('analyze-code-btn');

  if (loading) loading.style.display = 'block';
  if (analyzeBtn) analyzeBtn.disabled = true;

  try {
    currentAnalysisData = await api.analyze.all({
      language: currentLanguage,
      code,
      level: currentLevel
    });

    populateExplanationTab(currentAnalysisData.explanation);
    populateErrorsTab(currentAnalysisData.errors);
    populateComplexityTab(currentAnalysisData.complexity);
    populateReviewTab(currentAnalysisData.review);
    populateOptimizationTab(currentAnalysisData.optimization);
    populateDocsTab(currentAnalysisData.docs);

    // If flowchart tab is currently active, render it
    if (document.getElementById('tab-panel-flowchart')?.classList.contains('active')) {
      renderMermaidFlowchart();
    }

    // Auto-save snapshot in the background per user decision
    try {
      const activeFile = document.getElementById('active-file-label')?.textContent || 'Java Algorithm';
      await api.analyze.save({
        type: 'COMPREHENSIVE',
        language: currentLanguage,
        code,
        resultJson: currentAnalysisData,
        qualityScore: currentAnalysisData.review?.overallScore || 85,
        notes: `Auto-saved run for ${activeFile}`
      });
    } catch (saveErr) {
      console.warn('Auto-save background record note:', saveErr);
    }

    toast.success('Analysis updated with latest AST telemetry');
  } catch (err) {
    console.error('Analysis error:', err);
    toast.error(err.error || 'Analysis failed. Invalid syntax.');
  } finally {
    if (loading) loading.style.display = 'none';
    if (analyzeBtn) analyzeBtn.disabled = false;
  }
}

function populateExplanationTab(data) {
  if (!data) return;
  document.getElementById('exp-summary').textContent = data.summary;
  document.getElementById('exp-confidence').textContent = `Confidence: ${data.confidence}`;
  document.getElementById('exp-signature').textContent = data.signature;

  const stepsList = document.getElementById('exp-steps-list');
  if (stepsList && data.stepByStep) {
    stepsList.innerHTML = data.stepByStep.map(s => `
      <div style="display: flex; gap: 0.6rem; font-size: 12.5px; align-items: flex-start;">
        <span style="font-family: var(--font-code); background: var(--bg-surface-high); color: var(--color-primary-light); width: 20px; height: 20px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 11px;">
          ${s.step}
        </span>
        <span style="color: var(--text-secondary); line-height: 1.5;">${escapeHtml(s.text)}</span>
      </div>
    `).join('');
  }
}

function populateErrorsTab(data) {
  if (!data) return;
  const badge = document.getElementById('errors-count-badge');
  if (badge) badge.textContent = data.issuesCount;

  const title = document.getElementById('errors-banner-title');
  if (title) title.textContent = `${data.issuesCount} Issues Detected`;

  const container = document.getElementById('issues-container');
  if (!container) return;

  container.innerHTML = data.issues.map(issue => `
    <article class="issue-card" data-line="${issue.line}">
      <div class="issue-card-header">
        <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;">
          <span class="badge ${issue.severityClass}">
            <span style="width: 5px; height: 5px; border-radius: 50%; background: currentColor;"></span>
            <span>${issue.type}: ${escapeHtml(issue.title)}</span>
          </span>
          <span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted); background: var(--bg-surface-high); padding: 1px 6px; border-radius: var(--radius-xs);">
            Line ${issue.line}
          </span>
        </div>
        <button class="btn btn-sm btn-ghost highlight-line-btn" data-line="${issue.line}" title="Highlight in Editor">
          <span class="material-symbols-outlined" style="font-size: 15px;">center_focus_strong</span>
        </button>
      </div>

      <p style="font-size: 12.5px; color: var(--text-primary); line-height: 1.5; margin: 0;">
        ${escapeHtml(issue.explanation)}
      </p>

      <div class="issue-why-callout">
        <strong style="color: var(--color-secondary); display: block; margin-bottom: 2px;">Why this happens:</strong>
        <span>${escapeHtml(issue.whyItHappens)}</span>
      </div>

      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.25rem;">
        <span style="font-size: 11.5px; color: var(--text-muted);">
          Fix: <code style="font-family: var(--font-code); color: var(--color-emerald);">${escapeHtml(issue.suggestedFix)}</code>
        </span>
        <button class="btn btn-sm btn-primary apply-fix-btn" data-line="${issue.line}" data-fix="${escapeHtml(issue.fixCode)}" style="height: 26px; padding: 0 8px; font-size: 11px;">
          <span>Apply Fix</span>
        </button>
      </div>
    </article>
  `).join('');

  // Event handlers for issue cards
  container.querySelectorAll('.issue-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.apply-fix-btn')) return;
      const line = Number(card.dataset.line);
      highlightEditorLines(line - 1, line, 'cm-line-highlight-hazard');
    });
  });

  // Apply Fix Button Action
  container.querySelectorAll('.apply-fix-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const line = Number(btn.dataset.line) - 1; // 0-indexed in CodeMirror
      const fixCode = btn.dataset.fix;

      if (line >= 0 && line < editor.lineCount()) {
        editor.replaceRange(fixCode + '\n', { line, ch: 0 }, { line: line + 1, ch: 0 });
        toast.success(`Applied fix to Line ${line + 1}!`);
        highlightEditorLines(line, line + 1, 'cm-line-highlight-focus');
        setTimeout(() => runAnalysis(), 500);
      }
    });
  });
}

function populateComplexityTab(data) {
  if (!data) return;
  document.getElementById('comp-time-worst').textContent = data.timeComplexity.worst;
  document.getElementById('comp-time-best').textContent = data.timeComplexity.best;
  document.getElementById('comp-space').textContent = data.spaceComplexity.auxiliary;
}

function populateReviewTab(data) {
  if (!data) return;
  const recsContainer = document.getElementById('review-recs-list');
  if (recsContainer && data.recommendations) {
    recsContainer.innerHTML = data.recommendations.map(r => `
      <div style="background: var(--bg-lowest); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 0.625rem; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <div style="display: flex; align-items: center; gap: 0.35rem; margin-bottom: 2px;">
            <span class="badge ${r.priority === 'HIGH' ? 'badge-error' : r.priority === 'MEDIUM' ? 'badge-warning' : 'badge-neutral'}" style="font-size: 9px; padding: 1px 5px;">
              ${r.priority}
            </span>
            <strong style="font-size: 12.5px; color: var(--text-primary);">${escapeHtml(r.title)}</strong>
          </div>
          <p style="font-size: 11.5px; color: var(--text-secondary); margin: 0;">${escapeHtml(r.description)}</p>
        </div>
        <span style="font-family: var(--font-code); font-size: 11px; color: var(--text-muted);">Line ${r.line}</span>
      </div>
    `).join('');
  }
}

function populateOptimizationTab(data) {
  if (!data) return;
  document.getElementById('opt-jit-badge').textContent = data.jitStatus;
  document.getElementById('opt-title').textContent = data.suggestionTitle;
  document.getElementById('opt-desc').textContent = data.description;
  document.getElementById('opt-code-snippet').textContent = data.refactoredSnippet;
}

function populateDocsTab(data) {
  if (!data) return;
  const docsEl = document.getElementById('docs-javadoc-container');
  if (docsEl) docsEl.textContent = data.javadoc;
}

function renderMermaidFlowchart() {
  const container = document.getElementById('mermaid-diagram-container');
  if (!container || typeof mermaid === 'undefined') return;

  const rawCode = currentAnalysisData?.flowchart?.mermaid || `graph TD
    A([Start]) --> B[No Flowchart Generated] --> C([End])`;

  container.innerHTML = `<div class="mermaid">${rawCode}</div>`;
  mermaid.run({ nodes: container.querySelectorAll('.mermaid') });
}

function highlightEditorLines(startLine, endLine, className) {
  if (!editor) return;

  // Clear previous line mark
  if (currentLineMark) {
    currentLineMark.clear();
    currentLineMark = null;
  }

  // Scroll smoothly to target line
  editor.scrollIntoView({ line: startLine, ch: 0 }, 150);

  // Apply line highlight
  for (let l = startLine; l <= endLine && l < editor.lineCount(); l++) {
    editor.addLineClass(l, 'background', className);
  }

  // Remove highlight after 3.5 seconds
  setTimeout(() => {
    for (let l = startLine; l <= endLine && l < editor.lineCount(); l++) {
      editor.removeLineClass(l, 'background', className);
    }
  }, 3500);
}
