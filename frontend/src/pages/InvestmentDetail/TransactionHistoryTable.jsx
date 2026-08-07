import React from 'react';

import { TransactionType } from '../../types/enums';






export const TransactionHistoryTable = ({ transactions, investmentCurrency = 'USD', homeCurrency = 'INR' }) => {
    const formatHomeValue = (txn) => {
        const fxRate = Number(txn.fxRateToHome);
        if (!Number.isFinite(fxRate) || fxRate <= 0) {
            return '—';
        }

        const total = Number(txn.quantity || 0) * Number(txn.price || 0) * fxRate;
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: homeCurrency }).format(total);
    };

  return (
    <div className="bg-card rounded-lg overflow-hidden">
            <div className="px-6 py-4 bg-card-alt">
                <h3 className="text-lg font-medium text-text-heading">Transaction History</h3>
            </div>
            <table className="w-full text-left border-collapse">
                <thead>
                    <tr className="bg-card-alt">
                        <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Date</th>
                        <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Type</th>
                        <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Quantity</th>
                        <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Price</th>
                                                <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">FX Rate</th>
                                                <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Home Value</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200">
                    {transactions.length === 0 ?
          <tr>
                                                        <td colSpan={6} className="px-6 py-12 text-center text-text-muted">
                                No transactions found for this investment.
                            </td>
                        </tr> :

          transactions.map((txn) =>
          <tr key={txn.id} className="hover:bg-card-alt transition-colors">
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">
                                    {txn.txnDate ? new Intl.DateTimeFormat('en-US', { month: 'short', day: '2-digit', year: 'numeric' }).format(new Date(txn.txnDate)) : 'N/A'}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
              txn.type === TransactionType.BUY ? 'bg-accent-blue text-[#2E6F99]' : 'bg-accent-plum text-[#7A4F99]'}`
              }>
                                        {txn.type}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading text-right">
                                    {txn.quantity.toLocaleString(undefined, { maximumFractionDigits: 4 })}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading text-right">
                                    {new Intl.NumberFormat('en-US', { style: 'currency', currency: investmentCurrency }).format(txn.price)}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading text-right">
                                    {txn.fxRateToHome != null ? Number(txn.fxRateToHome).toLocaleString(undefined, { maximumFractionDigits: 8 }) : '—'}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading text-right">
                                    {formatHomeValue(txn)}
                                </td>
                            </tr>
          )
          }
                </tbody>
            </table>
        </div>);

};