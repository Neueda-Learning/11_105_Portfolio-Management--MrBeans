import { useState, useEffect, useCallback } from 'react';
import { dividendsApi } from '../../../api/dividends';

export const useDividends = (investmentId) => {
  const [dividends, setDividends] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDividends = useCallback(async () => {
    if (!investmentId) return;
    setIsLoading(true);
    try {
      const data = await dividendsApi.getByInvestment(investmentId);
      setDividends(data);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, [investmentId]);

  useEffect(() => {
    fetchDividends();
  }, [fetchDividends]);

  return { dividends, isLoading, error, refresh: fetchDividends };
};
