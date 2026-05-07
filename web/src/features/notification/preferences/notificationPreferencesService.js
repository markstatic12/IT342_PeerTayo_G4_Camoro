import api from '../../../core/api/axios';

const basePath = '/notifications/preferences';

export async function getNotificationPreferences() {
  const res = await api.get(basePath);
  return res.data?.data ?? null;
}

export async function updateNotificationPreferences(payload) {
  const res = await api.put(basePath, payload);
  return res.data?.data ?? null;
}
