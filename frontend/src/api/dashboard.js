import { apiClient } from './client';

export const dashboardApi = {
  getSummary: (homeCurrency = 'INR') =>
    apiClient.fetch(`/dashboard/summary?homeCurrency=${homeCurrency}`),
  getAssetAllocation: (homeCurrency = 'INR') =>
    apiClient.fetch(`/dashboard/allocation?homeCurrency=${homeCurrency}`),
  getTrend: (homeCurrency = 'INR', days = 30) =>
    apiClient.fetch(`/dashboard/trend?homeCurrency=${homeCurrency}&days=${days}`),
  getPerformance: (homeCurrency = 'INR') =>
    apiClient.fetch(`/dashboard/performance?homeCurrency=${homeCurrency}`),
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
  },
};