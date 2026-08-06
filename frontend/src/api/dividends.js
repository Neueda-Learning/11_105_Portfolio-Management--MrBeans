import { apiClient } from './client';

export const dividendsApi = {
  getByInvestment: (investmentId) =>
    apiClient.fetch(`/investments/${investmentId}/dividends`),

  create: (investmentId, data) =>
    apiClient.fetch(`/investments/${investmentId}/dividends`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  delete: (investmentId, dividendId) =>
    apiClient.fetch(`/investments/${investmentId}/dividends/${dividendId}`, {
      method: 'DELETE',
    }),

  simulate: (investmentId, data) =>
    apiClient.fetch(`/investments/${investmentId}/dividends/simulate`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};