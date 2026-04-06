import { createApiClient } from './apiClientFactory';
import { authSession } from '../features/auth/patterns/AuthSessionFacade';
import { authEventBus, AUTH_EVENTS } from '../features/auth/patterns/AuthEventBus';

const api = createApiClient({
  baseURL: 'http://localhost:8080/api/v1',
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
      (error) => {
        if (error.response?.status === 401) {
          authSession.clearSession();
          authEventBus.emit(AUTH_EVENTS.UNAUTHORIZED, { reason: 'token-expired-or-invalid' });
        }
        return Promise.reject(error);
      }
    );
  },
});

export default api;
