import { useState, useEffect, useCallback } from 'react';
import { investmentsApi } from '../../../api/investments';
import { transactionsApi } from '../../../api/transactions';









export const useHistory = () => {
  const [history, setHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchHistory = useCallback(async () => {
    setIsLoading(true);
    try {
      // 1. Fetch all investments
      const investments = await investmentsApi.getAll();

      // 2. Fetch transactions for all investments in parallel
      const transactionPromises = investments.map(async (inv) => {
        const txns = await transactionsApi.getByInvestment(inv.id);
        // 3. Map investment details onto each transaction row for the unified view
        return txns.map((txn) => ({
          ...txn,
          symbol: inv.symbol,
          investmentName: inv.name,
          currency: inv.currency
        }));
      });

      const results = await Promise.all(transactionPromises);

      // 4. Flatten and sort chronologically (newest first)
      const allTxns = results.flat().sort((a, b) => {
        const dateA = a.txnDate ? new Date(a.txnDate).getTime() : 0;
        const dateB = b.txnDate ? new Date(b.txnDate).getTime() : 0;
        return dateB - dateA;
      });

      setHistory(allTxns);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  return { history, isLoading, error, refresh: fetchHistory };
};