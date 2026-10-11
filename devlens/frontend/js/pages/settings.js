/**
 * DevLens Settings Page Logic
 */

import { auth } from '../auth.js';
import { initLayout } from '../layout.js';
import { theme } from '../theme.js';
import { api } from '../api.js';
import { toast } from '../toast.js';

document.addEventListener('DOMContentLoaded', async () => {
  if (!auth.guard()) return;
  initLayout('settings');

  setupSettingsForms();
  await loadUserData();
});

async function loadUserData() {
  try {
    const user = await api.users.getMe();
    if (user) {
      document.getElementById('settings-name').value = user.name || '';
      document.getElementById('settings-email').value = user.email || '';
      document.getElementById('settings-level').value = user.defaultLevel || 'INTERMEDIATE';
      document.getElementById('settings-theme').value = user.theme || theme.getTheme();
    }
  } catch (err) {
    console.error('Failed to load user settings:', err);
  }
}

function setupSettingsForms() {
  const profileForm = document.getElementById('profile-form');
  const passwordForm = document.getElementById('password-form');

  profileForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('settings-name').value.trim();
    const defaultLevel = document.getElementById('settings-level').value;
    const selectedTheme = document.getElementById('settings-theme').value;

    if (!name) {
      toast.error('Name cannot be empty');
      return;
    }

    try {
      const updated = await api.users.updateMe({ name, defaultLevel, theme: selectedTheme });
      // Update local storage session
      const currentUser = auth.getUser() || {};
      auth.setSession(auth.getToken(), { ...currentUser, ...updated });

      // Apply theme
      theme.setTheme(selectedTheme);

      toast.success('Profile and preferences updated!');
    } catch (err) {
      toast.error('Failed to update profile');
    }
  });

  passwordForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (!currentPassword || !newPassword) {
      toast.error('Please fill all password fields');
      return;
    }

    if (newPassword.length < 6) {
      toast.error('New password must be at least 6 characters');
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error('New password and confirmation do not match');
      return;
    }

    try {
      await api.users.changePassword({ currentPassword, newPassword });
      passwordForm.reset();
      toast.success('Password updated successfully!');
    } catch (err) {
      toast.error(err.error || 'Failed to update password');
    }
  });
}
