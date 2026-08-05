import React from 'react';
import { InvestmentType } from '../../types/enums';






export const InvestmentFilters = ({ filterType, onFilterChange }) => {
  return (
    <div className="flex items-center space-x-4 bg-card p-6 rounded-xl mb-6">
            <span className="text-[15px] font-semibold text-text-body">Filter by Asset Class:</span>
            <div className="flex space-x-2">
                <button
          onClick={() => onFilterChange('ALL')}
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
          onClick={() => onFilterChange(type)}
          className={`px-3 py-1.5 text-sm rounded-md transition-colors ${
          filterType === type ?
          'bg-accent-pink-strong text-text-onFill' :
          'bg-card-alt text-text-muted hover:bg-accent-pink hover:text-text-onFill'}`
          }>
          
                        {type}
                    </button>
        )}
            </div>
        </div>);

};