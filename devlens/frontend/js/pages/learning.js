/**
 * DevLens Learning Mode Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { escapeHtml } from '../utils.js';

let learningData = null;
let currentStepIndex = 0;
let userAnswers = {};

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('learning');

  await loadLearningData();
});

async function loadLearningData() {
  try {
    learningData = await api.ai.learning();
    if (!learningData) return;

    renderConcept(learningData.concept);
    renderStep(0);
    renderQuiz(learningData.quiz);
    renderViva(learningData.vivaQuestions);
    setupStepperButtons();

  } catch (err) {
    console.error('Failed to load learning data:', err);
    toast.error('Failed to load learning curriculum');
  }
}

function renderConcept(concept) {
  if (!concept) return;
  const titleEl = document.getElementById('concept-title');
  if (titleEl) {
    titleEl.textContent = concept.title;
  }
  document.getElementById('concept-description').textContent = concept.description;
  document.getElementById('concept-difficulty').textContent = concept.difficulty || 'Beginner';
  document.getElementById('concept-category').textContent = concept.category || 'Algorithms';

  // Add AI Disclaimer Badge
  const headerCard = document.querySelector('.learning-hero-card') || document.getElementById('concept-title')?.parentElement;
  if (headerCard && !document.getElementById('learning-ai-badge')) {
    const badge = document.createElement('span');
    badge.id = 'learning-ai-badge';
    badge.className = 'badge badge-ai';
    badge.style.marginTop = '4px';
    badge.style.display = 'inline-flex';
    badge.style.alignItems = 'center';
    badge.style.gap = '4px';
    badge.innerHTML = `<span class="material-symbols-outlined" style="font-size: 11px;">auto_awesome</span> AI-generated, may contain mistakes`;
    headerCard.appendChild(badge);
  }
}

function renderStep(index) {
  if (!learningData?.steps) return;
  currentStepIndex = index;
  const steps = learningData.steps;
  const step = steps[index];

  document.getElementById('step-counter-badge').textContent = `Step ${index + 1} of ${steps.length}`;
  document.getElementById('step-display-title').textContent = step.title;
  document.getElementById('step-display-content').textContent = step.content;

  // Update Stepper Nodes
  const nodes = document.querySelectorAll('#stepper-nav-nodes .step-node');
  nodes.forEach((node, i) => {
    node.classList.remove('active', 'completed');
    if (i < index) node.classList.add('completed');
    else if (i === index) node.classList.add('active');
  });

  // Update Prev / Next Buttons
  const prevBtn = document.getElementById('step-prev-btn');
  const nextBtn = document.getElementById('step-next-btn');

  if (prevBtn) prevBtn.disabled = index === 0;
  if (nextBtn) {
    if (index === steps.length - 1) {
      nextBtn.innerHTML = `<span>Complete Topic</span><span class="material-symbols-outlined" style="font-size: 16px;">check</span>`;
    } else {
      nextBtn.innerHTML = `<span>Next Step</span><span class="material-symbols-outlined" style="font-size: 16px;">arrow_forward</span>`;
    }
  }
}

function setupStepperButtons() {
  document.getElementById('step-prev-btn')?.addEventListener('click', () => {
    if (currentStepIndex > 0) renderStep(currentStepIndex - 1);
  });

  document.getElementById('step-next-btn')?.addEventListener('click', () => {
    if (currentStepIndex < (learningData?.steps.length - 1)) {
      renderStep(currentStepIndex + 1);
    } else {
      toast.success('Congratulations! You completed the Bubble Sort walkthrough.');
    }
  });

  document.querySelectorAll('#stepper-nav-nodes .step-node').forEach((node, idx) => {
    node.addEventListener('click', () => renderStep(idx));
  });
}

function renderQuiz(quizList) {
  const container = document.getElementById('quiz-cards-container');
  if (!container || !quizList) return;

  container.innerHTML = quizList.map((q, qIndex) => `
    <div class="quiz-question-box" data-quiz-id="${q.id}" style="background: var(--bg-lowest); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 1.25rem;">
      <h4 style="font-size: 13.5px; font-weight: 600; color: var(--text-primary); margin-bottom: 0.85rem;">
        Question ${qIndex + 1}: ${escapeHtml(q.question)}
      </h4>
      <div class="quiz-options-group" data-qindex="${qIndex}">
        ${q.options.map((opt, optIndex) => `
          <div class="quiz-option" data-option-index="${optIndex}">
            <span style="width: 20px; height: 20px; border-radius: 50%; background: var(--bg-surface-high); display: flex; align-items: center; justify-content: center; font-family: var(--font-code); font-size: 11px;">
              ${String.fromCharCode(65 + optIndex)}
            </span>
            <span style="flex: 1;">${escapeHtml(opt)}</span>
          </div>
        `).join('')}
      </div>
      <div class="quiz-explanation" id="quiz-exp-${qIndex}" style="display: none; margin-top: 0.75rem; padding: 0.75rem; border-radius: var(--radius-md); font-size: 12.5px; line-height: 1.5;"></div>
    </div>
  `).join('');

  // Handle option selection
  container.querySelectorAll('.quiz-options-group').forEach(group => {
    const qIndex = Number(group.dataset.qindex);
    const quiz = quizList[qIndex];

    group.querySelectorAll('.quiz-option').forEach(optionEl => {
      optionEl.addEventListener('click', () => {
        if (userAnswers[qIndex] !== undefined) return; // Already answered

        const selectedIndex = Number(optionEl.dataset.optionIndex);
        userAnswers[qIndex] = selectedIndex;

        const isCorrect = selectedIndex === quiz.answerIndex;
        if (isCorrect) {
          optionEl.classList.add('correct');
        } else {
          optionEl.classList.add('incorrect');
          // Highlight correct one
          group.querySelector(`[data-option-index="${quiz.answerIndex}"]`)?.classList.add('correct');
        }

        // Show Explanation
        const expBox = document.getElementById(`quiz-exp-${qIndex}`);
        if (expBox) {
          expBox.style.display = 'block';
          expBox.style.background = isCorrect ? 'var(--color-emerald-subtle)' : 'var(--color-rose-subtle)';
          expBox.style.color = isCorrect ? 'var(--color-emerald)' : 'var(--color-rose)';
          expBox.style.border = `1px solid ${isCorrect ? 'rgba(34,197,94,0.3)' : 'rgba(239,68,68,0.3)'}`;
          expBox.innerHTML = `<strong>${isCorrect ? 'Correct!' : 'Incorrect.'}</strong> ${escapeHtml(quiz.explanation)}`;
        }

        updateQuizScore(quizList);
      });
    });
  });
}

function updateQuizScore(quizList) {
  let score = 0;
  Object.keys(userAnswers).forEach(qIndex => {
    if (userAnswers[qIndex] === quizList[qIndex].answerIndex) {
      score++;
    }
  });

  const badge = document.getElementById('quiz-score-badge');
  if (badge) badge.textContent = `Score: ${score} / ${quizList.length}`;
}

function renderViva(vivaList) {
  const container = document.getElementById('viva-accordion-container');
  if (!container || !vivaList) return;

  container.innerHTML = vivaList.map((v, idx) => `
    <div class="viva-item ${idx === 0 ? 'open' : ''}">
      <div class="viva-header">
        <span style="display: flex; align-items: center; gap: 0.5rem; font-size: 13.5px;">
          <span class="material-symbols-outlined" style="font-size: 18px; color: var(--color-primary-light);">help</span>
          <span>${escapeHtml(v.question)}</span>
        </span>
        <span class="material-symbols-outlined viva-arrow" style="font-size: 18px; color: var(--text-muted); transition: transform 0.2s ease;">
          expand_more
        </span>
      </div>
      <div class="viva-body">
        <div style="font-family: var(--font-code); font-size: 11px; text-transform: uppercase; color: var(--color-secondary); margin-bottom: 0.25rem;">Model Viva Answer:</div>
        <p style="margin: 0; color: var(--text-primary); font-size: 13px; line-height: 1.6;">${escapeHtml(v.answer)}</p>
      </div>
    </div>
  `).join('');

  // Toggle Viva Accordions
  container.querySelectorAll('.viva-item').forEach(item => {
    item.querySelector('.viva-header').addEventListener('click', () => {
      item.classList.toggle('open');
      const arrow = item.querySelector('.viva-arrow');
      if (arrow) {
        arrow.style.transform = item.classList.contains('open') ? 'rotate(180deg)' : 'rotate(0deg)';
      }
    });
  });
}
