const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || 'http://localhost:8080';

const providerStrategies = {
  google: {
    key: 'google',
    getLoginUrl: () => `${BACKEND_BASE_URL}/oauth2/authorization/google`,
  },
};

export function createOAuthProvider(providerName) {
  const strategy = providerStrategies[providerName];
  if (!strategy) {
    throw new Error(`Unsupported OAuth provider: ${providerName}`);
  }
  return {
    key: strategy.key,
    loginUrl: strategy.getLoginUrl(),
  };
}
