import React, { useState } from 'react';
import { InvestmentType } from '../../types/enums';

import { Modal } from '../../components/ui/Modal';







export const InvestmentFormModal = ({ isOpen, onClose, onSubmit }) => {
  const [symbol, setSymbol] = useState('');
  const [name, setName] = useState('');
  const [type, setType] = useState(InvestmentType.STOCK);
  const [currency, setCurrency] = useState('USD');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg('');

    const req = { symbol, name, type, currency };
    const result = await onSubmit(req);

    setIsSubmitting(false);
    if (result.success) {
      // reset and close
      setSymbol('');
      setName('');
      setType(InvestmentType.STOCK);
      setCurrency('USD');
      onClose();
    } else {
      setErrorMsg(result.error?.message || 'Failed to create investment');
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Investment">
            <form onSubmit={handleSubmit} className="space-y-4">
                {errorMsg &&
        <div className="p-3 text-sm text-loss bg-[#FFEAF1] rounded">
                        {errorMsg}
                    </div>
        }
                
                <div>
                    <label htmlFor="symbol" className="block text-sm font-medium text-text-body mb-1">Symbol (Ticker)</label>
                    <input
            id="symbol"
            required
            type="text"
            value={symbol}
            onChange={(e) => setSymbol(e.target.value.toUpperCase())}
            placeholder="e.g. AAPL"
            className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
          
                </div>

                <div>
                    <label htmlFor="name" className="block text-sm font-medium text-text-body mb-1">Name</label>
                    <input
            id="name"
            required
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Apple Inc."
            className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
          
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="type" className="block text-sm font-medium text-text-body mb-1">Asset Class</label>
                        <select
              id="type"
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md bg-white focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink">
              
                            {Object.values(InvestmentType).map((t) =>
              <option key={t} value={t}>{t}</option>
              )}
                        </select>
                    </div>
                    <div>
                        <label htmlFor="currency" className="block text-sm font-medium text-text-body mb-1">Currency</label>
                        <select
              id="currency"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md bg-white focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink">
              
                            <option value="USD">USD</option>
                            <option value="EUR">EUR</option>
                            <option value="GBP">GBP</option>
                            <option value="JPY">JPY</option>
                        </select>
                    </div>
                </div>

                <div className="pt-4 flex justify-end space-x-3">
                    <button
            type="button"
            onClick={onClose}
            className="px-5 py-3 text-[15px] font-semibold text-text-body bg-white border border-[#FFE6EE] rounded-md hover:bg-page focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-pink">
            
                        Cancel
                    </button>
                    <button
            type="submit"
            disabled={isSubmitting}
            className="px-5 py-3 text-[15px] font-semibold text-white bg-accent-pink rounded-md hover:bg-accent-pink-strong focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-pink disabled:opacity-50">
            
                        {isSubmitting ? 'Saving...' : 'Add Investment'}
                    </button>
                </div>
            </form>
        </Modal>);

};