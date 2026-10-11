/**
 * DevLens Mock Users Module
 */
import { mockCurrentUser } from './authMock.js';

let currentUser = { ...mockCurrentUser };

export function mockGetMe() {
  return { ...currentUser };
}

export function mockUpdateMe({ name, defaultLevel, theme }) {
  if (name) currentUser.name = name;
  if (defaultLevel) currentUser.defaultLevel = defaultLevel;
  if (theme) currentUser.theme = theme;
  currentUser.updatedAt = new Date().toISOString();
  return { ...currentUser };
}

export function mockChangePassword({ currentPassword, newPassword }) {
  if (!currentPassword || !newPassword || newPassword.length < 6) {
    throw {
      status: 400,
      error: "Password validation failed",
      fields: {
        currentPassword: !currentPassword ? "Current password is required" : null,
        newPassword: (!newPassword || newPassword.length < 6) ? "New password must be at least 6 characters" : null
      }
    };
  }
  return {
    status: 200,
    message: "Password updated successfully"
  };
}
