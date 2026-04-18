/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';
import { authSession } from '../utils/auth/AuthSession';
import { authEventBus, AUTH_EVENTS } from '../utils/auth/AuthEventBus';
import { adaptAuthPayload, adaptUserPayload } from '../utils/auth/AuthResponseAdapter';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => authSession.getUser());
  const [token, setToken] = useState(() => authSession.getToken());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    authSession.setToken(token);
  }, [token]);

  useEffect(() => {
    authSession.setUser(user);
  }, [user]);

  useEffect(() => {
    const unsubscribeUnauthorized = authEventBus.on(AUTH_EVENTS.UNAUTHORIZED, () => {
      setUser(null);
      setToken(null);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    });

    return () => {
      unsubscribeUnauthorized();
    };
  }, []);

  const register = async (firstName, lastName, email, password) => {
    setLoading(true);
    try {
      const res = await api.post('/auth/register', { firstName, lastName, email, password });
      const { user: userData, token: jwt } = adaptAuthPayload(res.data.data);
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
      const { user: userData, token: jwt } = adaptAuthPayload(res.data.data);
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
      const { user: userData } = adaptUserPayload(res.data.data);
      setToken(jwt);
      setUser(userData);
      return { success: true, user: userData };
    } catch {
      return { success: false, message: 'Failed to fetch user info after Google sign-in' };
    }
  };

  const refreshCurrentUser = async () => {
    if (!token) return { success: false, message: 'No active session' };

    try {
      const res = await api.get('/auth/me');
      const { user: userData } = adaptUserPayload(res.data.data);
      setUser(userData);
      return { success: true, user: userData };
    } catch {
      return { success: false, message: 'Unable to refresh current user' };
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
      authSession.clearSession();
      authEventBus.emit(AUTH_EVENTS.LOGGED_OUT);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        loginWithToken,
        refreshCurrentUser,
        isAuthenticated: !!token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
