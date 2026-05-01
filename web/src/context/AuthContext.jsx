/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';
import { authSession } from '../utils/auth/AuthSession';
import { authEventBus, AUTH_EVENTS } from '../utils/auth/AuthEventBus';
import { adaptAuthPayload } from '../utils/auth/AuthResponseAdapter';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => authSession.getUser());
  const [token, setToken] = useState(() => authSession.getToken());
  const [loading, setLoading] = useState(false);

  // On mount, if a token exists refresh it so roles are always current from DB
  useEffect(() => {
    const storedToken = authSession.getToken();
    if (!storedToken) return;

    api.post('/auth/refresh', null, {
      headers: { Authorization: `Bearer ${storedToken}` },
    }).then((res) => {
      const { user: userData, token: newToken } = adaptAuthPayload(res.data.data);
      if (userData) setUser(userData);
      if (newToken) setToken(newToken);
    }).catch(() => {
      // Token is expired or invalid — clear session and let the user log in again
      authSession.clearSession();
      setUser(null);
      setToken(null);
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
      // /auth/me returns { user: UserResponse } — extract the nested user directly
      const userData = res.data?.data?.user ?? null;
      setToken(jwt);
      if (userData) setUser(userData);
      return { success: true, user: userData };
    } catch {
      return { success: false, message: 'Failed to fetch user info after Google sign-in' };
    }
  };

  const refreshCurrentUser = async () => {
    if (!token) return { success: false, message: 'No active session' };

    try {
      const res = await api.get('/auth/me');
      // /auth/me returns { user: UserResponse } — extract the nested user directly
      const userData = res.data?.data?.user ?? null;
      if (userData) setUser(userData);
      return { success: true, user: userData };
    } catch {
      return { success: false, message: 'Unable to refresh current user' };
    }
  };

  const promoteToFacilitator = async () => {
    try {
      const res = await api.post('/auth/promote-to-facilitator');
      const { user: userData, token: newToken } = adaptAuthPayload(res.data.data);
      if (userData) setUser(userData);
      if (newToken) setToken(newToken);
      return { success: true, user: userData };
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Promotion failed';
      return { success: false, message: msg };
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
        promoteToFacilitator,
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
