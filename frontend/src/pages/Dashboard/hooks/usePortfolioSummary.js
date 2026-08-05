import { useState, useEffect } from 'react';
import { dashboardApi } from '../../../api/dashboard';

import { ApiError } from '../../../api/client';

export const usePortfolioSummary = () => {
  const [summary, setSummary] = useState(null);
  const [allocation, setAllocation] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      setIsLoading(true);
      try {
        const [summaryData, allocationData] = await Promise.all([
        dashboardApi.getSummary(),
        dashboardApi.getAssetAllocation()]
        );

        if (isMounted) {
          setSummary(summaryData);
          setAllocation(allocationData);
          setError(null);
        }
      } catch (err) {
        if (isMounted) {
          setError(err);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchData();
    return () => {
      isMounted = false;
    };
  }, []);

  return { summary, allocation, isLoading, error };
};