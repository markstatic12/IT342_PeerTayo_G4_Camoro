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
