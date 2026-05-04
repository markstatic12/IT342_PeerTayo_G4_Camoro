import api from '../../../core/api/axios';

const basePath = '/evaluations';

export async function getEvaluationResults(id) {
  const response = await api.get(`${basePath}/${id}/results`);
  return response.data?.data;
}

export async function getMyResults() {
  const response = await api.get(`${basePath}/my-results`);
  return response.data?.data?.results;
}

export async function archiveMyResult(evaluationId) {
  const response = await api.post(`${basePath}/my-results/${evaluationId}/archive`);
  return response.data?.data;
}

export async function unarchiveMyResult(evaluationId) {
  const response = await api.post(`${basePath}/my-results/${evaluationId}/unarchive`);
  return response.data?.data;
}
