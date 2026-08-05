import React from 'react';
import { usePortfolioSummary } from './hooks/usePortfolioSummary';
import { SummaryCards } from './SummaryCards';
import { AllocationChart } from './AllocationChart';
import { TrendChart } from './TrendChart';

import { PerformanceScatter } from './PerformanceScatter';


export const DashboardPage = () => {
  const { summary, allocation, isLoading, error } = usePortfolioSummary();

  // Mock Data for charts that don't have backend endpoints yet
  const mockTrendData = [
  { date: 'Jan', value: 100000 },
  { date: 'Feb', value: 105000 },
  { date: 'Mar', value: 102000 },
  { date: 'Apr', value: 110000 },
  { date: 'May', value: 115000 },
  { date: 'Jun', value: 120000 }];


  const mockScatterData = [
  { name: 'AAPL', risk: 15, return: 25 },
  { name: 'TSLA', risk: 40, return: 45 },
  { name: 'BND', risk: 5, return: -2 },
  { name: 'MSFT', risk: 12, return: 18 },
  { name: 'JNJ', risk: 8, return: -5 }];


  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full min-h-[50vh]">
                <div className="text-neutral-500 animate-pulse">Loading dashboard...</div>
            </div>);

  }

  if (error) {
    return (
      <div className="bg-accentRose/10 border border-accentRose rounded-md p-4 text-accentRose">
                Failed to load dashboard: {error.message}
            </div>);

  }

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
            <SummaryCards summary={summary} />
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <AllocationChart data={allocation} />
                <TrendChart data={mockTrendData} />
            </div>

            <div className="grid grid-cols-1 gap-8">
                <PerformanceScatter data={mockScatterData} />
            </div>
        </div>);

};