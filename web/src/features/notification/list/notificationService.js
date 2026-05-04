import api from '../../../core/api/axios';

const basePath = '/notifications';

export async function listNotifications() {
  const response = await api.get(basePath);
  return response.data?.data?.notifications ?? [];
}
