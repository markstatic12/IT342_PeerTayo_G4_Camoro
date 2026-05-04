import api from '../../../core/api/axios';

const basePath = '/evaluations';

export async function listPendingEvaluations({ archived = false } = {}) {
  const response = await api.get(`${basePath}/pending`, { params: { archived } });
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

export async function archivePendingEvaluation(evaluationId) {
  const response = await api.post(`${basePath}/pending/${evaluationId}/archive`);
  return response.data?.data;
}

export async function unarchivePendingEvaluation(evaluationId) {
  const response = await api.post(`${basePath}/pending/${evaluationId}/unarchive`);
  return response.data?.data;
}
