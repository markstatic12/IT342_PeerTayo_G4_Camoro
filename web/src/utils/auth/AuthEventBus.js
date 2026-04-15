export const AUTH_EVENTS = {
  UNAUTHORIZED: 'auth:unauthorized',
  LOGGED_OUT: 'auth:loggedOut',
};

const listeners = new Map();

export const authEventBus = {
  on(event, callback) {
    if (!listeners.has(event)) {
      listeners.set(event, new Set());
    }
    listeners.get(event).add(callback);

    return () => {
      listeners.get(event)?.delete(callback);
    };
  },
  emit(event, payload) {
    listeners.get(event)?.forEach((callback) => callback(payload));
  },
};
