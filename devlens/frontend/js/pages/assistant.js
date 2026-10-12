/**
 * DevLens AI Code Assistant Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { api } from '../api.js';
import { toast } from '../toast.js';
import { copyToClipboard, escapeHtml } from '../utils.js';

let sessions = [];
let currentSessionId = 'session-101';

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('assistant');

  setupChatEvents();
  await loadSessions();
});

async function loadSessions() {
  try {
    sessions = await api.ai.getChatSessions();
    renderSessionsList();

    if (sessions.length > 0) {
      currentSessionId = sessions[0].id;
      loadSessionDetail(currentSessionId);
    }
  } catch (err) {
    console.error('Failed to load chat sessions:', err);
    toast.error('Failed to load chat history');
  }
}

function renderSessionsList() {
  const container = document.getElementById('sessions-list-container');
  if (!container) return;

  container.innerHTML = sessions.map(s => `
    <div class="nav-link ${s.id === currentSessionId ? 'active' : ''} session-item" data-id="${s.id}" style="font-size: 12.5px; cursor: pointer; padding: 0.5rem 0.65rem;">
      <span class="material-symbols-outlined" style="font-size: 16px;">chat_bubble_outline</span>
      <span style="flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${escapeHtml(s.title)}</span>
    </div>
  `).join('');

  container.querySelectorAll('.session-item').forEach(el => {
    el.addEventListener('click', () => {
      currentSessionId = el.dataset.id;
      renderSessionsList();
      loadSessionDetail(currentSessionId);
    });
  });
}

async function loadSessionDetail(sessionId) {
  try {
    const session = await api.ai.getChatSessionDetail(sessionId);
    document.getElementById('active-session-title').textContent = session.title;
    renderMessages(session.messages || []);
  } catch (err) {
    console.error('Failed to load session detail:', err);
  }
}

function renderMessages(messages) {
  const stream = document.getElementById('chat-messages-stream');
  if (!stream) return;

  stream.innerHTML = messages.map(msg => {
    const isUser = msg.sender === 'user';
    return `
      <div class="chat-message-bubble ${isUser ? 'user-msg' : 'ai-msg'} animate-fade-in">
        <div style="width: 28px; height: 28px; border-radius: 50%; background: ${isUser ? 'var(--color-primary)' : 'var(--bg-surface-high)'}; display: flex; align-items: center; justify-content: center; flex-shrink: 0; color: #FFF; font-size: 12px; font-weight: 600;">
          ${isUser ? 'U' : '<span class="material-symbols-outlined" style="font-size: 16px; color: var(--color-secondary);">smart_toy</span>'}
        </div>
        <div class="chat-message-content">
          ${formatChatMessageText(msg.text)}
        </div>
      </div>
    `;
  }).join('');

  // Attach code block copy buttons
  stream.querySelectorAll('.copy-chat-code-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const code = btn.closest('.chat-code-block-embed')?.querySelector('code')?.textContent;
      if (code) {
        await copyToClipboard(code);
        toast.success('Code copied to clipboard');
      }
    });
  });

  // Scroll stream to bottom
  stream.scrollTop = stream.scrollHeight;
}

function formatChatMessageText(text) {
  if (!text) return '';

  // Parse triple backtick code blocks ```java ... ```
  const codeBlockRegex = /```([a-zA-Z]*)\n([\s\S]*?)```/g;
  let formatted = text.replace(codeBlockRegex, (match, lang, code) => {
    return `
      <div class="chat-code-block-embed">
        <div class="chat-code-block-header">
          <span>${lang ? lang.toUpperCase() : 'CODE'}</span>
          <button type="button" class="btn btn-sm btn-ghost copy-chat-code-btn" style="height: 20px; padding: 0 4px; font-size: 10px;">
            <span class="material-symbols-outlined" style="font-size: 13px;">content_copy</span>
            <span>Copy</span>
          </button>
        </div>
        <pre style="margin: 0; padding: 0.75rem; overflow-x: auto; font-family: var(--font-code); font-size: 12px; color: var(--text-primary); line-height: 1.5;"><code>${escapeHtml(code.trim())}</code></pre>
      </div>
    `;
  });

  // Parse single backticks `code`
  formatted = formatted.replace(/`([^`]+)`/g, '<code style="font-family: var(--font-code); background: var(--bg-lowest); color: var(--color-secondary); padding: 1px 4px; border-radius: 3px; font-size: 12px;">$1</code>');

  // Convert newlines to breaks where appropriate
  formatted = formatted.replace(/\n\n/g, '<br><br>');

  return formatted;
}

function setupChatEvents() {
  const form = document.getElementById('chat-form');
  const textarea = document.getElementById('chat-input-textarea');

  form?.addEventListener('submit', async (e) => {
    e.preventDefault();
    await sendMessage();
  });

  // Auto-resize and submit on Enter without Shift
  textarea?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  // Quick prompt chips click
  document.querySelectorAll('.prompt-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      if (textarea) {
        textarea.value = chip.dataset.prompt;
        sendMessage();
      }
    });
  });

  // New Chat Button
  document.getElementById('new-chat-btn')?.addEventListener('click', () => {
    const newSession = {
      id: `session-${Date.now()}`,
      title: "New AI Conversation",
      updatedAt: new Date().toISOString(),
      messages: [
        {
          id: `msg-${Date.now()}`,
          sender: "ai",
          text: "Hello! I am DevLens Code Intelligence. How can I assist you with your algorithms, syntax hazards, or viva prep today?",
          timestamp: new Date().toISOString()
        }
      ]
    };
    sessions.unshift(newSession);
    currentSessionId = newSession.id;
    renderSessionsList();
    loadSessionDetail(currentSessionId);
    toast.info('Started new conversation');
  });

  // Clear chat button
  document.getElementById('clear-chat-btn')?.addEventListener('click', () => {
    const currentSession = sessions.find(s => s.id === currentSessionId);
    if (currentSession) {
      currentSession.messages = [];
      renderMessages([]);
      toast.info('Cleared conversation messages');
    }
  });
}

async function sendMessage() {
  const textarea = document.getElementById('chat-input-textarea');
  const message = textarea.value.trim();
  if (!message) return;

  textarea.value = '';

  const stream = document.getElementById('chat-messages-stream');
  const typingIndicator = document.getElementById('typing-indicator');

  // Append user bubble immediately
  const userMsgEl = document.createElement('div');
  userMsgEl.className = 'chat-message-bubble user-msg animate-fade-in';
  userMsgEl.innerHTML = `
    <div style="width: 28px; height: 28px; border-radius: 50%; background: var(--color-primary); display: flex; align-items: center; justify-content: center; flex-shrink: 0; color: #FFF; font-size: 12px; font-weight: 600;">
      U
    </div>
    <div class="chat-message-content">${escapeHtml(message)}</div>
  `;
  stream.appendChild(userMsgEl);
  stream.scrollTop = stream.scrollHeight;

  // Show typing indicator
  if (typingIndicator) typingIndicator.style.display = 'flex';

  try {
    const res = await api.ai.chat({
      sessionId: currentSessionId,
      message,
      includeContext: true
    });

    if (typingIndicator) typingIndicator.style.display = 'none';

    // Append AI bubble
    const aiMsgEl = document.createElement('div');
    aiMsgEl.className = 'chat-message-bubble ai-msg animate-fade-in';
    aiMsgEl.innerHTML = `
      <div style="width: 28px; height: 28px; border-radius: 50%; background: var(--bg-surface-high); display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
        <span class="material-symbols-outlined" style="font-size: 16px; color: var(--color-secondary);">smart_toy</span>
      </div>
      <div class="chat-message-content">
        ${formatChatMessageText(res.reply)}
        <div style="font-size: 10.5px; color: var(--text-muted); margin-top: 6px; display: flex; align-items: center; gap: 3px;">
          <span class="material-symbols-outlined" style="font-size: 12px; color: var(--color-secondary);">auto_awesome</span>
          <span>AI-generated, may contain mistakes</span>
        </div>
      </div>
    `;
    stream.appendChild(aiMsgEl);

    // Attach copy button listeners to the new block
    aiMsgEl.querySelectorAll('.copy-chat-code-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const code = btn.closest('.chat-code-block-embed')?.querySelector('code')?.textContent;
        if (code) {
          await copyToClipboard(code);
          toast.success('Code copied to clipboard');
        }
      });
    });

    stream.scrollTop = stream.scrollHeight;
  } catch (err) {
    if (typingIndicator) typingIndicator.style.display = 'none';
    toast.error('Failed to get response from AI');
  }
}
