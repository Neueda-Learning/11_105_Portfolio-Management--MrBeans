import { create } from 'zustand';
import { InvestmentType } from '../types/enums';









export const useFilterStore = create((set) => ({
  dateRange: { start: null, end: null },
  assetTypeFilter: null,
  setDateRange: (start, end) => set({ dateRange: { start, end } }),
  setAssetTypeFilter: (type) => set({ assetTypeFilter: type }),
  clearFilters: () => set({ dateRange: { start: null, end: null }, assetTypeFilter: null })
}));