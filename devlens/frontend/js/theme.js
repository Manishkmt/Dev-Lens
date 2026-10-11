/**
 * DevLens Theme Manager
 * Dark & Light mode toggle with localStorage persistence
 */

export const theme = {
  init() {
    const savedTheme = localStorage.getItem('devlens_theme') || 'dark';
    this.setTheme(savedTheme);
  },

  getTheme() {
    return localStorage.getItem('devlens_theme') || 'dark';
  },

  setTheme(themeName) {
    const root = document.documentElement;
    if (themeName === 'light') {
      root.classList.remove('dark');
      root.classList.add('light');
    } else {
      root.classList.remove('light');
      root.classList.add('dark');
    }
    localStorage.setItem('devlens_theme', themeName);

    // Update any theme toggle buttons on the page
    document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
      const icon = btn.querySelector('.material-symbols-outlined');
      if (icon) {
        icon.textContent = themeName === 'light' ? 'dark_mode' : 'light_mode';
      }
      btn.setAttribute('title', themeName === 'light' ? 'Switch to Dark Mode' : 'Switch to Light Mode');
    });
  },

  toggle() {
    const current = this.getTheme();
    const next = current === 'light' ? 'dark' : 'light';
    this.setTheme(next);
    return next;
  }
};

// Auto-initialize when script loads
theme.init();
