import { create } from 'zustand';

/**
 * Lets any page inject a React node into the TopBar's right side.
 * Use `setAction` on mount and `clearAction` on unmount (via useEffect return).
 */
export const useTopBarStore = create((set) => ({
  action: null,
  setAction: (action) => set({ action }),
  clearAction: () => set({ action: null }),
}));
