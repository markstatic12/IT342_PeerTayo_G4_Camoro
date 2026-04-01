import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
  }, [user]);

  const register = async (firstName, lastName, email, password) => {
    setLoading(true);
    try {
      const res = await api.post('/auth/register', { firstName, lastName, email, password });
      const { user: userData, token: jwt } = res.data.data;
      setUser(userData);
      setToken(jwt);
      return { success: true };
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Registration failed';
      return { success: false, message: msg };
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await api.post('/auth/login', { email, password });
      const { user: userData, token: jwt } = res.data.data;
      setUser(userData);
      setToken(jwt);
      return { success: true };
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Invalid email or password';
      return { success: false, message: msg };
    } finally {
      setLoading(false);
    }
  };

  const loginWithToken = async (jwt) => {
    try {
      const res = await api.get('/auth/me', {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      const userData = res.data.data;
      setToken(jwt);
      setUser(userData);
      return { success: true, user: userData };
    } catch (err) {
      return { success: false, message: 'Failed to fetch user info after Google sign-in' };
    }
  };

  const logout = async () => {
    try {
      // Tell the server to blacklist the current token immediately
      await api.post('/auth/logout');
    } catch {
      // Token may already be expired or server unreachable — still clear locally
    } finally {
      setUser(null);
      setToken(null);
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, loginWithToken, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
