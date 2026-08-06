import { apiClient } from './client';

export const instrumentsApi = {
  search: (q) => apiClient.fetch(`/instruments/search?q=${encodeURIComponent(q)}`),
};
