import axios from 'axios';

export function createApiClient({ baseURL, defaultHeaders = {}, configureInterceptors }) {
  const client = axios.create({
    baseURL,
    headers: defaultHeaders,
  });

  if (configureInterceptors) {
    configureInterceptors(client);
  }

  return client;
}
