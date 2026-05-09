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

export async function archiveEvaluation(evaluation) {
  const d = new Date(evaluation.deadline);
  const pad = (n) => String(n).padStart(2, '0');
  const deadline = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
  const response = await api.put(`${basePath}/${evaluation.id}`, {
    title: evaluation.title,
    description: evaluation.description,
    deadline,
    status: 'ARCHIVED',
  });
  return response.data?.data?.evaluation;
}

export async function getEvaluationParticipants(id) {
  const response = await api.get(`${basePath}/${id}/participants`);
  return response.data?.data;
}

export async function deleteEvaluation(id) {
  await api.delete(`${basePath}/${id}`);
}
