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

export async function getCompletedForms({ archived } = {}) {
  const params = archived === undefined ? undefined : { archived };
  const response = await api.get(`${basePath}/completed`, params ? { params } : undefined);
  return response.data?.data?.completed;
}

export async function archiveCompletedForm(evaluationId) {
  const response = await api.post(`${basePath}/completed/${evaluationId}/archive`);
  return response.data?.data;
}

export async function unarchiveCompletedForm(evaluationId) {
  const response = await api.post(`${basePath}/completed/${evaluationId}/unarchive`);
  return response.data?.data;
}

export async function deleteCompletedForm(evaluationId) {
  const response = await api.delete(`${basePath}/completed/${evaluationId}`);
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
