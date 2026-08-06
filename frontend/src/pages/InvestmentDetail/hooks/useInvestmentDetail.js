import { useState, useEffect, useCallback } from 'react';
import { investmentsApi } from '../../../api/investments';
import { transactionsApi } from '../../../api/transactions';
import { useSettingsStore } from '../../../store/useSettingsStore';




export const useInvestmentDetail = (investmentId) => {
  const [investment, setInvestment] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [pnl, setPnl] = useState({ realisedPnl: 0, unrealisedPnl: 0 });
  const baseCurrency = useSettingsStore((s) => s.baseCurrency);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    if (!investmentId) return;

    setIsLoading(true);
    try {
      const [invData, txnData, pnlData] = await Promise.all([
      investmentsApi.getById(investmentId),
      transactionsApi.getByInvestment(investmentId),
      investmentsApi.getPnl(investmentId, baseCurrency)]
      );
      setInvestment(invData);
      setTransactions(txnData);
      setPnl(pnlData);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, [investmentId, baseCurrency]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    investment,
    transactions,
    pnl,
    isLoading,
    error,
    refresh: fetchData
  };
};