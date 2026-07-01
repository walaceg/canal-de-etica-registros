import { apiClient } from '../../../shared/api/apiClient.js';

export async function getRegistros(params = {}) {
  const response = await apiClient.get('/api/registros', { params });
  return response.data.data;
}

export async function buscarPorId(id) {
  const response = await apiClient.get(`/api/registros/${id}`);
  return response.data.data;
}

export async function visualizarAnexo(registroId, anexoId) {
  const response = await apiClient.get(`/api/registros/${registroId}/anexos/${anexoId}`, {
    responseType: 'blob',
  });

  return {
    blob: response.data,
    contentDisposition: response.headers['content-disposition'],
    contentType: response.headers['content-type'],
  };
}
