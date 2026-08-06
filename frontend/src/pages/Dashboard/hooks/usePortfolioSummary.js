import { useState, useEffect, useCallback, useRef } from 'react';
import { dashboardApi } from '../../../api/dashboard';
import { useSettingsStore } from '../../../store/useSettingsStore';

export const usePortfolioSummary = () => {
  const baseCurrency = useSettingsStore((s) => s.baseCurrency);
  const [summary, setSummary] = useState(null);
  const [allocation, setAllocation] = useState([]);
  const [trend, setTrend] = useState([]);
  const [performance, setPerformance] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState(null);
  // Track whether we have shown data at least once so visibilitychange
  // refreshes don't replace the dashboard with a blank loading spinner.
  const hasDataRef = useRef(false);

  const fetchData = useCallback(async () => {
    if (!hasDataRef.current) {
      setIsLoading(true);
    } else {
      setIsRefreshing(true);
    }
    try {
      const [summaryData, allocationData, trendData, perfData] = await Promise.all([
        dashboardApi.getSummary(baseCurrency),
        dashboardApi.getAssetAllocation(baseCurrency),
        dashboardApi.getTrend(baseCurrency, 30),
        dashboardApi.getPerformance(baseCurrency),
      ]);
      setSummary(summaryData);
      setAllocation(allocationData);
      setTrend(trendData);
      setPerformance(perfData);
      setError(null);
      hasDataRef.current = true;
    } catch (err) {
      setError(err);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [baseCurrency]);

  // Fetch on mount and whenever baseCurrency changes
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Re-fetch when the user navigates back to this tab / window (silently)
  useEffect(() => {
    const onVisible = () => { if (document.visibilityState === 'visible') fetchData(); };
    document.addEventListener('visibilitychange', onVisible);
    return () => document.removeEventListener('visibilitychange', onVisible);
  }, [fetchData]);

  return { summary, allocation, trend, performance, isLoading, isRefreshing, error, refetch: fetchData };
};