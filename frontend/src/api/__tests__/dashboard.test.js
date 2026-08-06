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

  it('getFilteredTrend() should call the dedicated filtered trend API with query params', async () => {
    const mockTrend = [
      { date: '2026-01-01', portfolioValue: 1000 },
      { date: '2026-01-02', portfolioValue: 1100 }
    ];

    global.fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockTrend
    });

    const result = await dashboardApi.getFilteredTrend({
      fromDate: '2026-01-01',
      toDate: '2026-01-02',
      types: ['STOCK', 'BOND']
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/dashboard/trend/filter?'),
      expect.any(Object)
    );
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('fromDate=2026-01-01'),
      expect.any(Object)
    );
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('toDate=2026-01-02'),
      expect.any(Object)
    );
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('types=STOCK%2CBOND'),
      expect.any(Object)
    );
    expect(result).toEqual(mockTrend);
  });
});