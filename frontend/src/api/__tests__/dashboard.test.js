import { describe, it, expect, vi, beforeEach } from 'vitest';
import { dashboardApi } from '../dashboard';

describe('Dashboard API', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  it('getSummary() should strictly map realised and unrealised PnL separately', async () => {
    const mockSummary = {
      totalValue: 150000,
      totalCostBasis: 100000,
      totalRealisedPnl: 10000,
      totalUnrealisedPnl: 40000
    };

    global.fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockSummary
    });

    const result = await dashboardApi.getSummary();

    expect(global.fetch).toHaveBeenCalledWith('/api/dashboard/summary', expect.any(Object));

    // Strict mapping check per PRD
    expect(result).toHaveProperty('totalRealisedPnl', 10000);
    expect(result).toHaveProperty('totalUnrealisedPnl', 40000);
    expect(result.totalPnl).toBeUndefined(); // Must not exist
  });
});