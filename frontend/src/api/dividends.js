import { apiClient } from './client';


export const dividendsApi = {
  getByInvestment: (investmentId) => apiClient.fetch(`/investments/${investmentId}/dividends`),
  create: (data) => apiClient.fetch('/dividends', {
    method: 'POST',
    body: JSON.stringify(data)
  })
};