import api from '../../api/axios';

const basePath = '/users';

export async function listUsers() {
  const response = await api.get(basePath);
  return response.data?.data?.users ?? [];
}

export async function searchUsers(query) {
  const response = await api.get(basePath, {
    params: { q: query ?? '' },
  });
  return response.data?.data?.users ?? [];
}
