import api from '../../../core/api/axios';

const basePath = '/evaluations';

export async function listPendingEvaluations() {
  const response = await api.get(`${basePath}/pending`);
  return response.data?.data?.evaluations ?? [];
}

export async function submitEvaluation(id, payload) {
  const response = await api.post(`${basePath}/${id}/submit`, payload);
  return response.data?.data;
}

export async function getSubmittedSummary() {
  const response = await api.get(`${basePath}/submitted/summary`);
  return response.data?.data;
}
