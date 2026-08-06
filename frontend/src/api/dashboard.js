import { apiClient } from './client';


export const dashboardApi = {
  getSummary: () => apiClient.fetch('/dashboard/summary'),
  getAssetAllocation: () => apiClient.fetch('/dashboard/allocation'),
  getFilteredTrend: ({ homeCurrency = 'INR', fromDate, toDate, types = [], days = 30 } = {}) => {
    const params = new URLSearchParams();
    params.set('homeCurrency', homeCurrency);

    if (fromDate && toDate) {
      params.set('fromDate', fromDate);
      params.set('toDate', toDate);
    } else {
      params.set('days', String(days));
    }

    if (types.length > 0) {
      params.set('types', types.join(','));
    }

    return apiClient.fetch(`/dashboard/trend/filter?${params.toString()}`);
  }
};