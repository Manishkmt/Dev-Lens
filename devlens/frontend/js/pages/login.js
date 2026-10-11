/**
 * DevLens Auth Page Logic
 * Handles Sign In / Register tabs, validation, and session creation
 */

import { api } from '../api.js';
import { auth } from '../auth.js';
import { toast } from '../toast.js';

let isRegisterMode = false;

document.addEventListener('DOMContentLoaded', () => {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('logout') === 'true') {
    auth.clearSession();
  } else if (auth.isAuthenticated()) {
    // If already authenticated, redirect to dashboard
    window.location.replace('dashboard.html');
    return;
  }

  const tabSignIn = document.getElementById('tab-signin');
  const tabRegister = document.getElementById('tab-register');
  const toggleModeLink = document.getElementById('toggle-mode-link');
  const togglePasswordBtn = document.getElementById('toggle-password-btn');
  const passwordInput = document.getElementById('password-input');
  const eyeIcon = document.getElementById('eye-icon');
  const form = document.getElementById('auth-form');

  // Check URL query param for ?mode=register
  if (urlParams.get('mode') === 'register') {
    setMode(true);
  }

  // Tab Toggle Events
  tabSignIn.addEventListener('click', () => setMode(false));
  tabRegister.addEventListener('click', () => setMode(true));
  toggleModeLink.addEventListener('click', (e) => {
    e.preventDefault();
    setMode(!isRegisterMode);
  });

  // Password Visibility Toggle
  togglePasswordBtn.addEventListener('click', () => {
    if (passwordInput.type === 'password') {
      passwordInput.type = 'text';
      eyeIcon.textContent = 'visibility_off';
    } else {
      passwordInput.type = 'password';
      eyeIcon.textContent = 'visibility';
    }
  });

  // Form Submission
  form.addEventListener('submit', handleFormSubmit);

  // Social Auth Buttons Mock Click
  document.getElementById('social-github-btn')?.addEventListener('click', () => {
    toast.info('Authenticating via GitHub OAuth mock...');
    setTimeout(() => {
      auth.setSession("mock_github_jwt", {
        id: 99,
        name: "GitHub Student",
        email: "student@github.dev",
        role: "USER"
      });
      window.location.replace('dashboard.html');
    }, 600);
  });

  document.getElementById('social-google-btn')?.addEventListener('click', () => {
    toast.info('Authenticating via Google OAuth mock...');
    setTimeout(() => {
      auth.setSession("mock_google_jwt", {
        id: 98,
        name: "Google Student",
        email: "student@gmail.com",
        role: "USER"
      });
      window.location.replace('dashboard.html');
    }, 600);
  });
});

function setMode(register) {
  isRegisterMode = register;
  const tabSignIn = document.getElementById('tab-signin');
  const tabRegister = document.getElementById('tab-register');
  const groupName = document.getElementById('group-name');
  const signinHelpers = document.getElementById('signin-helpers');
  const submitBtnLabel = document.getElementById('submit-btn-label');
  const footerText = document.getElementById('footer-text');
  const toggleModeLink = document.getElementById('toggle-mode-link');
  const alertContainer = document.getElementById('auth-alert');

  if (alertContainer) alertContainer.style.display = 'none';
  clearFieldErrors();

  if (isRegisterMode) {
    tabRegister.classList.add('active');
    tabSignIn.classList.remove('active');
    groupName.style.display = 'flex';
    signinHelpers.style.display = 'none';
    submitBtnLabel.textContent = 'Create Free Account';
    footerText.textContent = 'Already have an account?';
    toggleModeLink.textContent = 'Sign in here';
    document.title = 'DevLens - Register Free Account';
  } else {
    tabSignIn.classList.add('active');
    tabRegister.classList.remove('active');
    groupName.style.display = 'none';
    signinHelpers.style.display = 'flex';
    submitBtnLabel.textContent = 'Sign in to DevLens';
    footerText.textContent = "Don't have an account?";
    toggleModeLink.textContent = 'Sign up for free (Student Pro trial)';
    document.title = 'DevLens - Sign In';
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();
  clearFieldErrors();

  const nameInput = document.getElementById('name-input');
  const emailInput = document.getElementById('email-input');
  const passwordInput = document.getElementById('password-input');
  const submitBtn = document.getElementById('submit-btn');

  const email = emailInput.value.trim();
  const password = passwordInput.value;
  const name = nameInput.value.trim();

  let hasError = false;

  // Client Validation
  if (isRegisterMode && !name) {
    showFieldError('name-error', 'Please enter your full name');
    hasError = true;
  }

  if (!email) {
    showFieldError('email-error', 'Please enter your email address');
    hasError = true;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showFieldError('email-error', 'Please enter a valid email format');
    hasError = true;
  }

  if (!password) {
    showFieldError('password-error', 'Please enter your password');
    hasError = true;
  } else if (password.length < 6) {
    showFieldError('password-error', 'Password must be at least 6 characters');
    hasError = true;
  }

  if (hasError) return;

  submitBtn.disabled = true;
  submitBtn.innerHTML = `
    <span class="animate-spin material-symbols-outlined" style="font-size: 18px;">progress_activity</span>
    <span>Authenticating...</span>
  `;

  try {
    let result;
    if (isRegisterMode) {
      result = await api.auth.register(name, email, password);
      toast.success('Account created successfully! Welcome to DevLens.');
    } else {
      result = await api.auth.login(email, password);
      toast.success(`Welcome back, ${result.user.name || 'Developer'}!`);
    }

    // Save session
    auth.setSession(result.token, result.user);

    // Redirect to dashboard
    setTimeout(() => {
      window.location.replace('dashboard.html');
    }, 450);

  } catch (error) {
    console.error('Auth error:', error);
    const alertBox = document.getElementById('auth-alert');
    if (alertBox) {
      alertBox.textContent = error.error || 'Authentication failed. Please check your credentials.';
      alertBox.style.display = 'block';
    }

    if (error.fields) {
      if (error.fields.name) showFieldError('name-error', error.fields.name);
      if (error.fields.email) showFieldError('email-error', error.fields.email);
      if (error.fields.password) showFieldError('password-error', error.fields.password);
    }

    toast.error(error.error || 'Authentication failed');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = `
      <span id="submit-btn-label">${isRegisterMode ? 'Create Free Account' : 'Sign in to DevLens'}</span>
      <span class="material-symbols-outlined" style="font-size: 18px;">arrow_forward</span>
    `;
  }
}

function showFieldError(elementId, message) {
  const el = document.getElementById(elementId);
  if (el) {
    el.textContent = message;
    el.style.display = 'block';
  }
}

function clearFieldErrors() {
  document.querySelectorAll('.field-error').forEach(el => {
    el.textContent = '';
    el.style.display = 'none';
  });
  const alertBox = document.getElementById('auth-alert');
  if (alertBox) alertBox.style.display = 'none';
}
