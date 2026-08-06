import { apiClient } from './client';


export const investmentsApi = {
  getAll: () => apiClient.fetch('/investments'),
  getById: (id) => apiClient.fetch(`/investments/${id}`),
  getPnl: (id, homeCurrency = 'INR') => apiClient.fetch(`/investments/${id}/pnl?homeCurrency=${homeCurrency}`),
  create: (data) => apiClient.fetch('/investments', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  delete: (id) => apiClient.fetch(`/investments/${id}`, {
    method: 'DELETE'
  })
};