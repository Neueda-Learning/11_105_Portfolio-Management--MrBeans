import { describe, it, expect, vi, beforeEach } from 'vitest';
import { investmentsApi } from '../investments';
import { InvestmentType } from '../../types/enums';
import { ApiError } from '../client';

describe('Investments API', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  it('getAll() should fetch investments successfully', async () => {
    const mockData = [
    { id: '1', symbol: 'AAPL', name: 'Apple', type: InvestmentType.STOCK, currency: 'USD', createdAt: '', updatedAt: '' }];


    global.fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockData
    });

    const result = await investmentsApi.getAll();

    expect(global.fetch).toHaveBeenCalledWith('/api/investments', expect.any(Object));
    expect(result).toEqual(mockData);
  });

  it('should properly throw an ApiError on failure', async () => {
    global.fetch.mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      json: async () => ({ message: 'Validation failed' })
    });

    await expect(investmentsApi.getAll()).rejects.toThrow(ApiError);
    await expect(investmentsApi.getAll()).rejects.toMatchObject({
      status: 400,
      message: 'Validation failed'
    });
  });

  it('create() should pass the correct POST body', async () => {
    const newInv = {
      symbol: 'MSFT',
      name: 'Microsoft',
      type: InvestmentType.STOCK,
      currency: 'USD'
    };

    global.fetch.mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => ({ id: '2', ...newInv })
    });

    await investmentsApi.create(newInv);

    expect(global.fetch).toHaveBeenCalledWith('/api/investments', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify(newInv)
    }));
  });
});