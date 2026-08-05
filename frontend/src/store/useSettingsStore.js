import { create } from 'zustand';
import { persist } from 'zustand/middleware';






export const useSettingsStore = create()(
  persist(
    (set) => ({
      baseCurrency: 'USD',
      setBaseCurrency: (currency) => set({ baseCurrency: currency })
    }),
    { name: 'poma-settings' }
  )
);