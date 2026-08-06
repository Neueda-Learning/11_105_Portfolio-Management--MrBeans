import React, { useState, useRef } from 'react';
import { InvestmentType } from '../../types/enums';
import { Modal } from '../../components/ui/Modal';
import { instrumentsApi } from '../../api/instruments';
import { Search, Loader2 } from 'lucide-react';

const inputClass = 'w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink';

const mapToInvestmentType = (type) => {
  switch (type) {
    case 'STOCK': return InvestmentType.STOCK;
    case 'BOND': return InvestmentType.BOND;
    case 'CASH': return InvestmentType.CASH;
    default: return InvestmentType.OTHER;
  }
};

export const InvestmentFormModal = ({ isOpen, onClose, onSubmit }) => {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);

  const [symbol, setSymbol] = useState('');
  const [name, setName] = useState('');
  const [type, setType] = useState(InvestmentType.STOCK);
  const [currency, setCurrency] = useState('USD');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const debounceRef = useRef(null);

  if (!isOpen) return null;

  const reset = () => {
    setQuery(''); setSymbol(''); setName(''); setType(InvestmentType.STOCK);
    setCurrency('USD'); setSuggestions([]); setShowDropdown(false); setErrorMsg('');
  };

  const handleQueryChange = (e) => {
    const val = e.target.value;
    setQuery(val);
    setSymbol(val.toUpperCase());
    setSuggestions([]);
    setShowDropdown(false);

    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!val.trim()) return;

    debounceRef.current = setTimeout(async () => {
      setIsSearching(true);
      try {
        const results = await instrumentsApi.search(val.trim());
        setSuggestions(results);
        setShowDropdown(results.length > 0);
      } catch {
        setSuggestions([]);
      } finally {
        setIsSearching(false);
      }
    }, 350);
  };

  const handleSelect = (item) => {
    setQuery(item.symbol);
    setSymbol(item.symbol);
    setName(item.name || item.symbol);
    setType(mapToInvestmentType(item.type));
    setCurrency(item.currency || 'USD');
    setShowDropdown(false);
    setSuggestions([]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg('');
    const result = await onSubmit({ symbol, name, type, currency });
    setIsSubmitting(false);
    if (result.success) { reset(); onClose(); }
    else setErrorMsg(result.error?.message || 'Failed to create investment');
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Investment">
      <form onSubmit={handleSubmit} className="space-y-4">
        {errorMsg && <div className="p-3 text-sm text-loss bg-[#FFEAF1] rounded">{errorMsg}</div>}

        {/* Live ticker search */}
        <div className="relative">
          <label className="block text-sm font-medium text-text-body mb-1">Search Symbol / Name</label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted pointer-events-none" />
            {isSearching && <Loader2 className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted animate-spin" />}
            <input
              type="text"
              value={query}
              onChange={handleQueryChange}
              onFocus={() => suggestions.length > 0 && setShowDropdown(true)}
              placeholder="e.g. Bitcoin, AAPL, Reliance..."
              className="w-full pl-9 pr-4 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink"
            />
          </div>
          {showDropdown && (
            <ul className="absolute z-50 w-full mt-1 bg-card border border-[#FFE6EE] rounded-md shadow-lg max-h-56 overflow-y-auto">
              {suggestions.map((item) => (
                <li key={item.symbol} onMouseDown={() => handleSelect(item)}
                  className="px-4 py-2 cursor-pointer hover:bg-card-alt flex items-center justify-between gap-2">
                  <div className="min-w-0">
                    <span className="font-semibold text-text-heading text-sm">{item.symbol}</span>
                    <span className="ml-2 text-xs text-text-muted truncate">{item.name}</span>
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    <span className="text-xs text-text-muted">{item.exchange}</span>
                    <span className="px-1.5 py-0.5 text-xs rounded bg-accent-blue text-[#2E6F99]">{item.type}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Symbol */}
        <div>
          <label className="block text-sm font-medium text-text-body mb-1">Symbol (Ticker)</label>
          <input required type="text" value={symbol}
            onChange={(e) => setSymbol(e.target.value.toUpperCase())}
            placeholder="e.g. BTC-USD" className={inputClass} />
        </div>

        {/* Name */}
        <div>
          <label className="block text-sm font-medium text-text-body mb-1">Name</label>
          <input required type="text" value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Bitcoin USD" className={inputClass} />
        </div>

        {/* Type + Currency */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Asset Class</label>
            <select value={type} onChange={(e) => setType(e.target.value)} className={inputClass}>
              {Object.values(InvestmentType).map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Currency</label>
            <input type="text" maxLength={10} value={currency}
              onChange={(e) => setCurrency(e.target.value.toUpperCase())}
              placeholder="USD" className={inputClass} />
          </div>
        </div>

        <div className="pt-2 flex justify-end space-x-3">
          <button type="button" onClick={() => { reset(); onClose(); }}
            className="px-5 py-3 text-[15px] font-semibold text-text-body bg-white border border-[#FFE6EE] rounded-md hover:bg-page">
            Cancel
          </button>
          <button type="submit" disabled={isSubmitting}
            className="px-5 py-3 text-[15px] font-semibold text-white bg-accent-pink rounded-md hover:bg-accent-pink-strong disabled:opacity-50">
            {isSubmitting ? 'Saving...' : 'Add Investment'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
