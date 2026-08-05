import React, { useState } from 'react';
import { TransactionType } from '../../types/enums';

import { Modal } from '../../components/ui/Modal';








export const TransactionFormModal = ({ isOpen, investmentId, onClose, onSubmit }) => {
  const [type, setType] = useState(TransactionType.BUY);
  const [quantity, setQuantity] = useState('');
  const [price, setPrice] = useState('');
  const [txnDate, setTxnDate] = useState('');
  const [fees, setFees] = useState('0');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg('');

    // Basic validation
    const qtyNum = parseFloat(quantity);
    const priceNum = parseFloat(price);
    const feesNum = parseFloat(fees);

    if (isNaN(qtyNum) || isNaN(priceNum) || isNaN(feesNum)) {
      setErrorMsg('Please enter valid numeric values for quantity, price, and fees.');
      setIsSubmitting(false);
      return;
    }

    // Format date strictly to ISO string if possible, or just pass datetime-local string
    // datetime-local value is like "2024-03-12T10:30"
    let formattedDate = txnDate;
    if (txnDate && txnDate.length === 16) {// Missing seconds
      formattedDate = `${txnDate}:00`;
    }

    const req = {
      investmentId,
      type,
      quantity: qtyNum,
      price: priceNum,
      txnDate: formattedDate,
      fees: feesNum
    };

    const result = await onSubmit(req);

    setIsSubmitting(false);
    if (result.success) {
      // reset and close
      setType(TransactionType.BUY);
      setQuantity('');
      setPrice('');
      setTxnDate('');
      setFees('0');
      onClose();
    } else {
      setErrorMsg(result.error?.message || 'Failed to add transaction');
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Transaction">
            <form onSubmit={handleSubmit} className="space-y-4">
                {errorMsg &&
        <div className="p-3 text-sm text-loss bg-[#FFEAF1] rounded">
                        {errorMsg}
                    </div>
        }

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="type" className="block text-sm font-medium text-text-body mb-1">Transaction Type</label>
                        <select
              id="type"
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md bg-white focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink">
              
                            <option value={TransactionType.BUY}>Buy</option>
                            <option value={TransactionType.SELL}>Sell</option>
                        </select>
                    </div>
                    <div>
                        <label htmlFor="txnDate" className="block text-sm font-medium text-text-body mb-1">Date & Time</label>
                        <input
              id="txnDate"
              required
              type="datetime-local"
              value={txnDate}
              onChange={(e) => setTxnDate(e.target.value)}
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
            
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label htmlFor="quantity" className="block text-sm font-medium text-text-body mb-1">Quantity</label>
                        <input
              id="quantity"
              required
              type="number"
              step="0.000001"
              min="0"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="e.g. 10.5"
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
            
                    </div>
                    <div>
                        <label htmlFor="price" className="block text-sm font-medium text-text-body mb-1">Price per unit</label>
                        <input
              id="price"
              required
              type="number"
              step="0.01"
              min="0"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              placeholder="e.g. 150.25"
              className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
            
                    </div>
                </div>

                <div>
                    <label htmlFor="fees" className="block text-sm font-medium text-text-body mb-1">Fees / Commissions</label>
                    <input
            id="fees"
            required
            type="number"
            step="0.01"
            min="0"
            value={fees}
            onChange={(e) => setFees(e.target.value)}
            className="w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink" />
          
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
            
                        {isSubmitting ? 'Saving...' : 'Add Transaction'}
                    </button>
                </div>
            </form>
        </Modal>);

};