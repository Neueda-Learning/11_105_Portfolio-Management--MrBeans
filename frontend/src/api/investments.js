import { apiClient } from './client';


export const investmentsApi = {
  getAll: () => apiClient.fetch('/investments'),
  getById: (id) => apiClient.fetch(`/investments/${id}`),
  create: (data) => apiClient.fetch('/investments', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  delete: (id) => apiClient.fetch(`/investments/${id}`, {
    method: 'DELETE'
  })
};