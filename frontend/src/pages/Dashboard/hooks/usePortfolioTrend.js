import { useEffect, useState } from 'react';
import { dashboardApi } from '../../../api/dashboard';

export const usePortfolioTrend = (filters) => {
  const [trend, setTrend] = useState([]);
  const [isTrendLoading, setIsTrendLoading] = useState(true);
  const [trendError, setTrendError] = useState(null);
  const fromDate = filters?.fromDate || '';
  const toDate = filters?.toDate || '';
  const days = filters?.days || 30;
  const homeCurrency = filters?.homeCurrency || 'INR';
  const types = filters?.types || [];
  const typesKey = types.join(',');

  useEffect(() => {
    let isMounted = true;

    const fetchTrend = async () => {
      setIsTrendLoading(true);
      const hasFrom = Boolean(fromDate);
      const hasTo = Boolean(toDate);

      if ((hasFrom && !hasTo) || (!hasFrom && hasTo)) {
        if (isMounted) {
          setTrend([]);
          setTrendError(new Error('Please provide both From and To dates.'));
          setIsTrendLoading(false);
        }
        return;
      }

      if (hasFrom && hasTo && fromDate > toDate) {
        if (isMounted) {
          setTrend([]);
          setTrendError(new Error('From date must be earlier than or equal to To date.'));
          setIsTrendLoading(false);
        }
        return;
      }

      try {
        const data = await dashboardApi.getFilteredTrend({
          fromDate: fromDate || undefined,
          toDate: toDate || undefined,
          days,
          homeCurrency,
          types
        });
        const mapped = data.map((row) => ({
          date: row.date,
          portfolioValue: row.portfolioValue,
          investedAmount: row.investedAmount,
        }));

        if (isMounted) {
          setTrend(mapped);
          setTrendError(null);
        }
      } catch (err) {
        if (isMounted) {
          setTrendError(err);
        }
      } finally {
        if (isMounted) {
          setIsTrendLoading(false);
        }
      }
    };

    const debounceId = setTimeout(fetchTrend, 300);

    return () => {
      isMounted = false;
      clearTimeout(debounceId);
    };
  }, [fromDate, toDate, days, homeCurrency, typesKey]);

  return { trend, isTrendLoading, trendError };
};