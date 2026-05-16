import { createApiClient } from './apiClientFactory';
import { authSession } from '../../features/auth/shared/AuthSession';
import { authEventBus, AUTH_EVENTS } from '../../features/auth/shared/AuthEventBus';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const api = createApiClient({
  baseURL: API_BASE_URL,
  defaultHeaders: { 'Content-Type': 'application/json' },
  configureInterceptors: (client) => {
    client.interceptors.request.use((config) => {
      const token = authSession.getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          const refreshToken = authSession.getRefreshToken();

          if (refreshToken) {
            try {
              // Attempt to refresh token
              const resp = await client.post('/auth/refresh-silent', { refreshToken });
              const { token, refreshToken: newRefreshToken, user } = resp.data.data;
              
              authSession.setSession({ user, token, refreshToken: newRefreshToken });
              
              // Retry original request with new token
              originalRequest.headers.Authorization = `Bearer ${token}`;
              return client(originalRequest);
            } catch (refreshError) {
              // Refresh failed (both tokens expired)
              authSession.clearSession();
              authEventBus.emit(AUTH_EVENTS.UNAUTHORIZED, { reason: 'session-expired' });
              return Promise.reject(refreshError);
            }
          } else {
            // No refresh token available
            authSession.clearSession();
            authEventBus.emit(AUTH_EVENTS.UNAUTHORIZED, { reason: 'token-expired-or-invalid' });
          }
        }
        return Promise.reject(error);
      }
    );
  },
});

export default api;
