import { useState, useEffect, useCallback } from 'react';
import { investmentsApi } from '../../../api/investments';



export const useInvestments = () => {
  const [investments, setInvestments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreating, setIsCreating] = useState(false);

  const fetchInvestments = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await investmentsApi.getAll();
      setInvestments(data);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchInvestments();
  }, [fetchInvestments]);

  const createInvestment = async (data) => {
    setIsCreating(true);
    try {
      const newInv = await investmentsApi.create(data);
      setInvestments((prev) => [...prev, newInv]);
      return { success: true, error: null };
    } catch (err) {
      return { success: false, error: err };
    } finally {
      setIsCreating(false);
    }
  };

  const deleteInvestment = async (id) => {
    try {
      await investmentsApi.delete(id);
      setInvestments((prev) => prev.filter((inv) => inv.id !== id));
      return { success: true };
    } catch (err) {
      return { success: false, error: err };
    }
  };

  return {
    investments,
    isLoading,
    error,
    isCreating,
    createInvestment,
    deleteInvestment,
    refresh: fetchInvestments
  };
};