import { useState, useEffect, useCallback } from 'react';
import { investmentsApi } from '../../../api/investments';
import { transactionsApi } from '../../../api/transactions';




export const useInvestmentDetail = (investmentId) => {
  const [investment, setInvestment] = useState(null);
  const [transactions, setTransactions] = useState([]);

  // MOCKED PnL state because backend currently does not expose a per-investment PnL endpoint.
  // Section 4.2 strictly forbids frontend calculation over transactions, so this must remain mocked 
  // until a backend endpoint is provided.
  const [mockedPnl] = useState({ realisedPnl: 1250.00, unrealisedPnl: -340.50 });

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    if (!investmentId) return;

    setIsLoading(true);
    try {
      const [invData, txnData] = await Promise.all([
      investmentsApi.getById(investmentId),
      transactionsApi.getByInvestment(investmentId)]
      );
      setInvestment(invData);
      setTransactions(txnData);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, [investmentId]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    investment,
    transactions,
    pnl: mockedPnl,
    isLoading,
    error,
    refresh: fetchData
  };
};