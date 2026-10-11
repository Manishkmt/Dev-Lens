/**
 * DevLens Mock Auth Module
 */

export const mockCurrentUser = {
  id: 1,
  name: "Alex Chen",
  email: "alex.chen@university.edu",
  role: "USER",
  defaultLevel: "INTERMEDIATE",
  theme: "dark",
  createdAt: "2026-09-01T10:00:00Z"
};

export const mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IkFsZXggQ2hlbiIsImVtYWlsIjoiYWxleC5jaGVuQHVuaXZlcnNpdHkuZWR1Iiwicm9sZSI6IlVTRVIifQ.mockSignatureDevLens";

export function mockLogin(email, password) {
  if (!email || !password) {
    throw {
      status: 400,
      error: "Validation failed",
      fields: {
        email: !email ? "Email is required" : null,
        password: !password ? "Password is required" : null
      }
    };
  }

  // Any valid email format succeeds in mock mode
  const user = {
    ...mockCurrentUser,
    email: email,
    name: email.split('@')[0].replace('.', ' ').replace(/\b\w/g, l => l.toUpperCase()) || "Alex Chen"
  };

  return {
    token: mockToken,
    user
  };
}

export function mockRegister(name, email, password) {
  if (!name || !email || !password || password.length < 6) {
    throw {
      status: 400,
      error: "Validation failed",
      fields: {
        name: !name ? "Name is required" : null,
        email: !email ? "Valid email is required" : null,
        password: (!password || password.length < 6) ? "Password must be at least 6 characters" : null
      }
    };
  }

  const user = {
    id: Math.floor(Math.random() * 1000) + 10,
    name,
    email,
    role: "USER",
    defaultLevel: "INTERMEDIATE",
    theme: "dark",
    createdAt: new Date().toISOString()
  };

  return {
    token: mockToken,
    user
  };
}
