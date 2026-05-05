import api from '../../core/api/axios';

const basePath = '/settings';

export async function getProfile() {
  const res = await api.get(`${basePath}/profile`);
  return res.data?.data?.user ?? null;
}

export async function updateProfile(payload) {
  const res = await api.put(`${basePath}/profile`, payload);
  return res.data?.data ?? null;
}

export async function changePassword(payload) {
  const res = await api.put(`${basePath}/password`, payload);
  return res.data?.data ?? null;
}
