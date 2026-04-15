import api from '../../api/axios';

const basePath = '/evaluations';

export async function listEvaluations() {
  const response = await api.get(basePath);
  return response.data?.data ?? [];
}

export async function getEvaluation(id) {
  const response = await api.get(`${basePath}/${id}`);
  return response.data?.data;
}

export async function createEvaluation(payload) {
  const response = await api.post(basePath, payload);
  return response.data?.data;
}

export async function updateEvaluation(id, payload) {
  const response = await api.put(`${basePath}/${id}`, payload);
  return response.data?.data;
}

export async function deleteEvaluation(id) {
  await api.delete(`${basePath}/${id}`);
}
