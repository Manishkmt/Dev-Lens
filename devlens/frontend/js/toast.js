/**
 * DevLens Toast Notification System
 */

function getOrCreateToastContainer() {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  return container;
}

export const toast = {
  show(message, type = 'info', duration = 3500) {
    const container = getOrCreateToastContainer();
    const toastEl = document.createElement('div');
    toastEl.className = `toast toast-${type} animate-fade-in`;

    const iconMap = {
      success: 'check_circle',
      error: 'error',
      warning: 'warning',
      info: 'info'
    };

    toastEl.innerHTML = `
      <span class="material-symbols-outlined toast-icon">${iconMap[type] || 'info'}</span>
      <div class="toast-message">${message}</div>
      <button class="toast-close" aria-label="Close notification">
        <span class="material-symbols-outlined" style="font-size: 16px;">close</span>
      </button>
    `;

    const closeBtn = toastEl.querySelector('.toast-close');
    closeBtn.addEventListener('click', () => {
      toastEl.remove();
    });

    container.appendChild(toastEl);

    if (duration > 0) {
      setTimeout(() => {
        toastEl.style.opacity = '0';
        toastEl.style.transform = 'translateY(8px)';
        toastEl.style.transition = 'all 0.25s ease';
        setTimeout(() => toastEl.remove(), 250);
      }, duration);
    }
  },

  success(msg, dur) { this.show(msg, 'success', dur); },
  error(msg, dur) { this.show(msg, 'error', dur); },
  warning(msg, dur) { this.show(msg, 'warning', dur); },
  info(msg, dur) { this.show(msg, 'info', dur); }
};
