import React from 'react';
import { InvestmentType } from '../../types/enums';

export const InvestmentFilters = ({
  filterType,
  onTypeChange,
  filterCurrency,
  onCurrencyChange,
  fromDate,
  toDate,
  onFromDateChange,
  onToDateChange,
  currencyOptions,
  onClearFilters
}) => {
  return (
    <div className="bg-card p-6 rounded-xl mb-6">
            <div className="flex flex-wrap items-center gap-4">
                <div className="flex flex-wrap items-center gap-2">
                    <span className="text-[15px] font-semibold text-text-body">Asset Class:</span>
                <button
          onClick={() => onTypeChange('ALL')}
          className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
          filterType === 'ALL' ?
          'bg-accent-pink-strong text-text-onFill' :
          'bg-card-alt text-text-muted hover:bg-accent-pink hover:text-text-onFill'}`
          }>
          
                    All
                </button>
                {Object.values(InvestmentType).map((type) =>
        <button
          key={type}
          onClick={() => onTypeChange(type)}
          className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
          filterType === type ?
          'bg-accent-pink-strong text-text-onFill' :
          'bg-card-alt text-text-muted hover:bg-accent-pink hover:text-text-onFill'}`
          }>
          
                        {type}
                    </button>
        )}
                </div>

                <div className="flex items-center gap-2">
                    <label htmlFor="currencyFilter" className="text-[15px] font-semibold text-text-body">Currency:</label>
                    <select
          id="currencyFilter"
          value={filterCurrency}
          onChange={(e) => onCurrencyChange(e.target.value)}
          className="px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-body border border-neutral-200 focus:outline-none focus:ring-2 focus:ring-accent-pink/40">
          <option value="ALL">All</option>
          {currencyOptions.map((currency) =>
          <option key={currency} value={currency}>{currency}</option>
          )}
        </select>
                </div>

                <div className="flex items-center gap-2">
                    <label htmlFor="fromDateFilter" className="text-[15px] font-semibold text-text-body">From:</label>
                    <input
          id="fromDateFilter"
          type="date"
          value={fromDate}
          onChange={(e) => onFromDateChange(e.target.value)}
          className="px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-body border border-neutral-200 focus:outline-none focus:ring-2 focus:ring-accent-pink/40"
        />
                </div>

                <div className="flex items-center gap-2">
                    <label htmlFor="toDateFilter" className="text-[15px] font-semibold text-text-body">To:</label>
                    <input
          id="toDateFilter"
          type="date"
          value={toDate}
          onChange={(e) => onToDateChange(e.target.value)}
          className="px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-body border border-neutral-200 focus:outline-none focus:ring-2 focus:ring-accent-pink/40"
        />
                </div>

                <button
          onClick={onClearFilters}
          className="px-3 py-1.5 text-sm rounded-md bg-card-alt text-text-muted hover:bg-neutral-200 transition-colors">
          Clear
        </button>
            </div>
        </div>);

};