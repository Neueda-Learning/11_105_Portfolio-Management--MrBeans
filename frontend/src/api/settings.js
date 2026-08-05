import { apiClient } from './client';





export const settingsApi = {
  getSettings: async () => {
    return { baseCurrency: 'USD' };
  },
  updateSettings: async (data) => {
    // Mock API call delay to simulate persistence
    return new Promise((resolve) => setTimeout(() => resolve(data), 500));
  }
};