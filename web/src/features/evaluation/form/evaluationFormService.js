import api from '../../../core/api/axios';

const basePath = '/evaluations';

export async function listCreatedEvaluations() {
  const response = await api.get(`${basePath}/created`);
  return response.data?.data?.evaluations ?? [];
}

export async function createEvaluation(payload) {
  const response = await api.post(basePath, payload);
  return response.data?.data?.evaluation;
}

export async function updateEvaluation(id, payload) {
  const response = await api.put(`${basePath}/${id}`, payload);
  return response.data?.data?.evaluation;
}

export async function deleteEvaluation(id) {
  await api.delete(`${basePath}/${id}`);
}
