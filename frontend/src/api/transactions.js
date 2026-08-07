import { apiClient } from './client';


export const transactionsApi = {
  getByInvestment: (investmentId) => apiClient.fetch(`/investments/${investmentId}/transactions`),
  create: (data, homeCurrency = 'INR') => apiClient.fetch(`/investments/${data.investmentId}/transactions?homeCurrency=${homeCurrency}`, {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  delete: (id) => apiClient.fetch(`/transactions/${id}`, {
    method: 'DELETE'
  })
};