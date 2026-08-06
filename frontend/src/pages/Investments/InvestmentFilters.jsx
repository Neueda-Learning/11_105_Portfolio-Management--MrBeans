import React from 'react';
import { InvestmentType } from '../../types/enums';
import { Search } from 'lucide-react';

const todayIso = new Date().toISOString().slice(0, 10);

export const InvestmentFilters = ({
  filterType,
  onTypeChange,
  filterCurrency,
  onCurrencyChange,
  pendingFrom,
  pendingTo,
  onPendingFromChange,
  onPendingToChange,
  onApplyDateFilter,
  currencyOptions,
  onClearFilters,
  hasActiveDateFilter,
}) => {
  return (
    <div className="bg-card p-5 rounded-xl mb-4 border border-neutral-100">
      <div className="flex flex-wrap items-center gap-4">

        {/* Asset class */}
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-[13px] font-semibold text-text-muted uppercase tracking-wider">Class:</span>
          <button
            onClick={() => onTypeChange('ALL')}
            className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
              filterType === 'ALL'
                ? 'bg-accent-pink-strong text-white'
                : 'bg-card-alt text-text-muted hover:bg-accent-pink/20'}`}>
            All
          </button>
          {Object.values(InvestmentType).map((type) => (
            <button
              key={type}
              onClick={() => onTypeChange(type)}
              className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
                filterType === type
                  ? 'bg-accent-pink-strong text-white'
                  : 'bg-card-alt text-text-muted hover:bg-accent-pink/20'}`}>
              {type}
            </button>
          ))}
        </div>

        {/* Currency */}
        <div className="flex items-center gap-2">
          <label htmlFor="currencyFilter" className="text-[13px] font-semibold text-text-muted uppercase tracking-wider">Currency:</label>
          <select
            id="currencyFilter"
            value={filterCurrency}
            onChange={(e) => onCurrencyChange(e.target.value)}
            className="px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-body border border-neutral-200 focus:outline-none focus:ring-2 focus:ring-accent-pink/40">
            <option value="ALL">All</option>
            {currencyOptions.map((currency) => (
              <option key={currency} value={currency}>{currency}</option>
            ))}
          </select>
        </div>

        {/* Date range (staged) */}
        <div className="flex items-center gap-2 flex-wrap">
          <span className="text-[13px] font-semibold text-text-muted uppercase tracking-wider">Date Added:</span>
          <input
            id="fromDateFilter"
            type="date"
            value={pendingFrom}
            max={pendingTo || todayIso}
            onChange={(e) => onPendingFromChange(e.target.value)}
            className={`px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-body border focus:outline-none focus:ring-2 focus:ring-accent-pink/40 ${
              hasActiveDateFilter ? 'border-accent-pink/50' : 'border-neutral-200'}`}
          />
          <span className="text-text-muted text-xs">to</span>
          <input
            id="toDateFilter"
            type="date"
            value={pendingTo}
            min={pendingFrom || undefined}
            max={todayIso}
            onChange={(e) => onPendingToChange(e.target.value)}
            className={`px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-body border focus:outline-none focus:ring-2 focus:ring-accent-pink/40 ${
              hasActiveDateFilter ? 'border-accent-pink/50' : 'border-neutral-200'}`}
          />
          <button
            onClick={onApplyDateFilter}
            className="flex items-center gap-1.5 px-3 py-1.5 text-sm rounded-md bg-accent-pink text-white hover:bg-accent-pink-strong transition-colors">
            <Search className="w-3.5 h-3.5" />
            Filter
          </button>
        </div>

        {/* Clear */}
        <button
          onClick={onClearFilters}
          className="px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-muted hover:bg-neutral-200 transition-colors">
          Clear All
        </button>
      </div>
    </div>
  );
};