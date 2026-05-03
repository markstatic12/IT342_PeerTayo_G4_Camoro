import api from '../../../core/api/axios';

const basePath = '/notifications';

export async function markNotificationAsRead(id) {
  const response = await api.put(`${basePath}/${id}/read`);
  return response.data?.data;
}
