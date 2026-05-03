const TOKEN_KEY = 'token';
const USER_KEY = 'user';

const parseUser = (rawUser) => {
  if (!rawUser) return null;
  try {
    return JSON.parse(rawUser);
  } catch {
    return null;
  }
};

export const authSession = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },
  getUser() {
    return parseUser(localStorage.getItem(USER_KEY));
  },
  setToken(token) {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
      return;
    }
    localStorage.removeItem(TOKEN_KEY);
  },
  setUser(user) {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      return;
    }
    localStorage.removeItem(USER_KEY);
  },
  setSession({ user, token }) {
    this.setUser(user);
    this.setToken(token);
  },
  clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};
