import { apiClient } from './client';


export const dashboardApi = {
  getSummary: () => apiClient.fetch('/dashboard/summary'),
  getAssetAllocation: () => apiClient.fetch('/dashboard/allocation')
};