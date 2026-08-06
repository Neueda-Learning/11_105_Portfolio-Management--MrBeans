import React, { useState, useEffect } from 'react';
import { TransactionType } from '../../types/enums';
import { Modal } from '../../components/ui/Modal';
import { investmentsApi } from '../../api/investments';





export const TransactionFormModal = ({ isOpen, investmentId, symbol, currency, onClose, onSubmit }) => {
  const [type, setType] = useState(TransactionType.BUY);
  const [quantity, setQuantity] = useState('');
  const [price, setPrice] = useState('');
  const [txnDate, setTxnDate] = useState('');
  const [fees, setFees] = useState('0');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [livePrice, setLivePrice] = useState(null);
  const [isPriceFetching, setIsPriceFetching] = useState(false);

  // ISO datetime-local string for "right now" — used as the max and default date
  const nowLocal = () => {
    const d = new Date();
    d.setSeconds(0, 0);
    return d.toISOString().slice(0, 16);
  };

  // Reset form + auto-fetch current price whenever the modal opens
  useEffect(() => {
    if (!isOpen) return;
    const now = nowLocal();
    setType(TransactionType.BUY);
    setQuantity('');
    setPrice('');
    setTxnDate(now);
    setFees('0');
    setErrorMsg('');
    setLivePrice(null);

    setIsPriceFetching(true);
    investmentsApi.getCurrentPrice(investmentId)
      .then((data) => {
        if (data.price != null) {
          setLivePrice(data.price);
          setPrice(String(data.price));
        }
      })
      .catch(() => { /* no snapshot available — user fills manually */ })
      .finally(() => setIsPriceFetching(false));
  }, [isOpen, investmentId]);

  if (!isOpen) return null;

  const qty = parseFloat(quantity) || 0;
  const priceVal = parseFloat(price) || 0;
  const total = qty * priceVal;
  const ccy = currency || 'USD';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    const qtyNum = parseFloat(quantity);
    const priceNum = parseFloat(price);
    const feesNum = parseFloat(fees) || 0;

    if (!qtyNum || qtyNum <= 0) {
      setErrorMsg('Quantity must be a positive number.');
      return;
    }
    if (!priceNum || priceNum <= 0) {
      setErrorMsg('Price must be a positive number.');
      return;
    }
    if (!txnDate) {
      setErrorMsg('Please select a transaction date.');
      return;
    }
    if (new Date(txnDate) > new Date()) {
      setErrorMsg('Transaction date cannot be in the future.');
      return;
    }

    setIsSubmitting(true);
    const formattedDate = txnDate.length === 16 ? `${txnDate}:00` : txnDate;
    const result = await onSubmit({
      investmentId,
      type,
      quantity: qtyNum,
      price: priceNum,
      txnDate: formattedDate,
      fees: feesNum,
    });
    setIsSubmitting(false);
    if (result.success) {
      onClose();
    } else {
      setErrorMsg(result.error?.message || 'Failed to add transaction.');
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`${type === TransactionType.BUY ? 'Buy' : 'Sell'} ${symbol || 'Investment'}`}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {errorMsg && (
          <div className="p-3 text-sm text-loss bg-[#FFEAF1] rounded">{errorMsg}</div>
        )}

        {/* Type + Date */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Transaction Type</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md bg-white focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink">
              <option value={TransactionType.BUY}>Buy</option>
              <option value={TransactionType.SELL}>Sell</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Date &amp; Time</label>
            <input
              required
              type="datetime-local"
              max={nowLocal()}
              value={txnDate}
              onChange={(e) => setTxnDate(e.target.value)}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
          </div>
        </div>

        {/* Quantity + Price */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Quantity</label>
            <input
              required
              type="number"
              step="0.000001"
              min="0.000001"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="e.g. 10"
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-body mb-1 flex items-center gap-2">
              Price per unit
              {isPriceFetching && <span className="text-xs text-text-muted animate-pulse">fetching…</span>}
              {livePrice != null && !isPriceFetching && (
                <span className="text-xs text-gain font-normal">Live: {ccy} {Number(livePrice).toLocaleString()}</span>
              )}
            </label>
            <input
              required
              type="number"
              step="0.0001"
              min="0.0001"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              placeholder={isPriceFetching ? 'Fetching live price…' : 'e.g. 150.25'}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
          </div>
        </div>

        {/* Fees */}
        <div>
          <label className="block text-sm font-medium text-text-body mb-1">Fees / Commissions</label>
          <input
            type="number"
            step="0.01"
            min="0"
            value={fees}
            onChange={(e) => setFees(e.target.value)}
            className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
        </div>

        {/* Live total calculation — always visible */}
        <div className={`rounded-md px-4 py-3 border transition-colors ${
          qty > 0 && priceVal > 0
            ? 'bg-gradient-to-r from-accent-pink/10 to-accent-blue/10 border-accent-pink/30'
            : 'bg-card-alt border-transparent'
        }`}>
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-text-muted uppercase tracking-wider">
              {type === 'BUY' ? 'Total Cost' : 'Total Proceeds'}
            </span>
            {qty > 0 && priceVal > 0 ? (
              <div className="text-right">
                <span className="text-xs text-text-muted">
                  {qty.toLocaleString()} × {ccy}&nbsp;{priceVal.toLocaleString(undefined, { maximumFractionDigits: 4 })}
                </span>
                <div className="text-xl font-bold text-text-heading">
                  {ccy}&nbsp;{total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </div>
              </div>
            ) : (
              <span className="text-sm text-text-muted italic">Enter quantity &amp; price above</span>
            )}
          </div>
          {qty > 0 && priceVal > 0 && parseFloat(fees) > 0 && (
            <div className="mt-1 pt-1 border-t border-accent-pink/20 flex justify-between text-xs text-text-muted">
              <span>After fees ({ccy}&nbsp;{parseFloat(fees).toLocaleString(undefined, { minimumFractionDigits: 2 })})</span>
              <span className="font-semibold">
                {ccy}&nbsp;{(total + parseFloat(fees)).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </div>
          )}
        </div>

        <div className="pt-2 flex justify-end space-x-3">
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
            {isSubmitting ? 'Saving…' : (type === TransactionType.BUY ? 'Buy' : 'Sell')}
          </button>
        </div>
      </form>
    </Modal>
  );
};
