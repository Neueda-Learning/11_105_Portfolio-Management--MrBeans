import React, { useMemo, useState } from 'react';
import { usePortfolioSummary } from './hooks/usePortfolioSummary';
import { usePortfolioTrend } from './hooks/usePortfolioTrend';
import { SummaryCards } from './SummaryCards';
import { AllocationChart } from './AllocationChart';
import { TrendChart } from './TrendChart';

import { PerformanceScatter } from './PerformanceScatter';
import { InvestmentType } from '../../types/enums';

const TREND_RANGES = [
  { key: '7D', label: '7D', days: 7 },
  { key: '30D', label: '30D', days: 30 },
  { key: '90D', label: '90D', days: 90 },
  { key: '1Y', label: '1Y', days: 365 },
  { key: 'YTD', label: 'YTD' },
  { key: 'CUSTOM', label: 'Custom' }
];


export const DashboardPage = () => {
  const { summary, allocation, isLoading, error } = usePortfolioSummary();
  const [activeRange, setActiveRange] = useState('30D');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [selectedTypes, setSelectedTypes] = useState([]);
  const allTypes = Object.values(InvestmentType);

  const todayIso = new Date().toISOString().slice(0, 10);
  const yearStartIso = `${new Date().getFullYear()}-01-01`;

  const trendFilters = useMemo(() => {
    const base = {
      homeCurrency: 'INR',
      types: selectedTypes
    };

    if (activeRange === 'CUSTOM') {
      return {
        ...base,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        days: 30
      };
    }

    if (activeRange === 'YTD') {
      return {
        ...base,
        fromDate: yearStartIso,
        toDate: todayIso,
        days: 30
      };
    }

    const selectedRange = TREND_RANGES.find((range) => range.key === activeRange);
    return {
      ...base,
      days: selectedRange?.days || 30
    };
  }, [activeRange, fromDate, toDate, selectedTypes, todayIso, yearStartIso]);

  const { trend, isTrendLoading, trendError } = usePortfolioTrend(trendFilters);


  const mockScatterData = [
  { name: 'AAPL', risk: 15, return: 25 },
  { name: 'TSLA', risk: 40, return: 45 },
  { name: 'BND', risk: 5, return: -2 },
  { name: 'MSFT', risk: 12, return: 18 },
  { name: 'JNJ', risk: 8, return: -5 }];

  const toggleType = (type) => {
    setSelectedTypes((prev) =>
    prev.includes(type) ?
    prev.filter((value) => value !== type) :
    [...prev, type]
    );
  };

  const selectedRangeLabel = TREND_RANGES.find((range) => range.key === activeRange)?.label || '30D';

  const appliedBadges = [
  `Range: ${selectedRangeLabel}`,
  selectedTypes.length === 0 ?
  'Assets: All' :
  `Assets: ${selectedTypes.join(', ')}`];

  if (activeRange === 'CUSTOM' && fromDate && toDate) {
    appliedBadges.push(`Dates: ${fromDate} to ${toDate}`);
  }

  const clearFilters = () => {
    setActiveRange('30D');
    setFromDate('');
    setToDate('');
    setSelectedTypes([]);
  };


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
                <div className="h-[28rem] rounded-2xl border border-accent-pink/30 bg-gradient-to-br from-card via-card to-accent-pink/10 p-5 md:p-6 shadow-sm flex flex-col gap-4">
                  <div className="flex flex-col gap-3">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                      <div>
                        <h3 className="text-lg md:text-xl font-semibold text-text-heading">Portfolio Trend</h3>
                        <p className="text-xs text-text-muted">Daily portfolio value with fast filters</p>
                      </div>

                      <button
                        type="button"
                        onClick={clearFilters}
                        className="self-start sm:self-auto px-3 h-10 rounded-xl border border-accent-pink/40 text-sm text-text-body hover:bg-accent-pink/15 transition-colors">
                        Reset Filters
                      </button>
                    </div>

                    <div className="flex flex-wrap gap-2">
                      {TREND_RANGES.map((range) => {
                        const isActive = activeRange === range.key;
                        return (
                          <button
                            key={range.key}
                            type="button"
                            onClick={() => setActiveRange(range.key)}
                            className={`h-9 px-3 rounded-xl text-xs font-medium transition-all ${
                            isActive ?
                            'bg-text-heading text-text-onFill shadow-sm' :
                            'bg-white/70 border border-accent-pink/30 text-text-body hover:bg-accent-pink/15'
                            }`}>
                            {range.label}
                          </button>);

                      })}
                    </div>
                  </div>

                  {activeRange === 'CUSTOM' && (
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <label className="flex flex-col gap-1">
                        <span className="text-xs text-text-muted">From date</span>
                        <input
                          id="trend-from-date"
                          type="date"
                          max={toDate || todayIso}
                          value={fromDate}
                          onChange={(e) => setFromDate(e.target.value)}
                          className="h-10 px-3 rounded-xl border border-accent-pink/35 bg-white/80 text-sm text-text-body outline-none focus:ring-2 focus:ring-accent-pink/40" />
                      </label>

                      <label className="flex flex-col gap-1">
                        <span className="text-xs text-text-muted">To date</span>
                        <input
                          id="trend-to-date"
                          type="date"
                          min={fromDate || undefined}
                          max={todayIso}
                          value={toDate}
                          onChange={(e) => setToDate(e.target.value)}
                          className="h-10 px-3 rounded-xl border border-accent-pink/35 bg-white/80 text-sm text-text-body outline-none focus:ring-2 focus:ring-accent-pink/40" />
                      </label>
                    </div>
                  )}

                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => setSelectedTypes([])}
                      className={`h-9 px-3 rounded-full text-xs font-medium transition-colors ${
                      selectedTypes.length === 0 ?
                      'bg-accent-yellow text-text-heading border border-accent-yellow' :
                      'bg-white/70 border border-accent-blue text-text-body hover:bg-accent-blue/20'
                      }`}>
                      All Assets
                    </button>

                    {allTypes.map((type) => {
                      const isActive = selectedTypes.includes(type);
                      return (
                        <button
                          key={type}
                          type="button"
                          onClick={() => toggleType(type)}
                          className={`h-9 px-3 rounded-full text-xs font-medium transition-colors ${
                          isActive ?
                          'bg-accent-blue text-text-heading border border-accent-blue' :
                          'bg-white/70 border border-accent-blue/40 text-text-body hover:bg-accent-blue/20'
                          }`}>
                          {type}
                        </button>);

                    })}
                  </div>

                  <div className="flex flex-wrap gap-2">
                    {appliedBadges.map((badge) => (
                      <span key={badge} className="px-2.5 py-1 rounded-full bg-card-alt text-[11px] text-text-muted border border-accent-pink/20">
                        {badge}
                      </span>
                    ))}
                  </div>

                  <div className="flex-1 min-h-0">
                    {isTrendLoading ? (
                      <div className="h-full rounded-2xl border border-accent-pink/25 bg-white/60 p-4 animate-pulse">
                        <div className="h-3 w-24 rounded bg-accent-pink/35 mb-4" />
                        <div className="h-[calc(100%-1.75rem)] rounded-xl bg-gradient-to-br from-accent-blue/20 to-accent-pink/20" />
                      </div>
                    ) : trendError ? (
                      <div className="bg-accentRose/10 border border-accentRose rounded-md p-3 text-accentRose text-sm">
                        Failed to load trend: {trendError.message}
                      </div>
                    ) : (
                      <TrendChart data={trend} showCard={false} showTitle={false} />
                    )}
                  </div>
                </div>
            </div>

            <div className="grid grid-cols-1 gap-8">
                <PerformanceScatter data={mockScatterData} />
            </div>
        </div>);

};