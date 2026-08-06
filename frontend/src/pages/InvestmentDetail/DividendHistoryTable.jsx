import React from 'react';
import { Trash2 } from 'lucide-react';
import { DividendMode } from '../../types/enums';

const fmt = (date) =>
  date ? new Intl.DateTimeFormat('en-US', { month: 'short', day: '2-digit', year: 'numeric' }).format(new Date(date)) : 'â€”';

const num = (v, dec = 2) => v != null ? Number(v).toLocaleString(undefined, { minimumFractionDigits: dec, maximumFractionDigits: dec }) : 'â€”';

export const DividendHistoryTable = ({ dividends, onDelete }) => {
  return (
    <div className="bg-card rounded-lg overflow-hidden mt-8">
      <div className="px-6 py-4 bg-card-alt">
        <h3 className="text-lg font-medium text-text-heading">Dividend History</h3>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-card-alt">
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Payment Date</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Ex-Date</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Mode</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Gross Amount</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Per Share</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Withholding Tax</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Net</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">CCY</th>
              <th className="px-6 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200">
            {dividends.length === 0 ? (
              <tr><td colSpan={9} className="px-6 py-12 text-center text-text-muted">No dividends recorded for this investment.</td></tr>
            ) : (
              dividends.map((div) => {
                const net = Number(div.amount) - Number(div.withholdingTax || 0);
                return (
                  <tr key={div.id} className="hover:bg-card-alt transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">{fmt(div.paymentDate)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">{fmt(div.exDate)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${div.mode === DividendMode.ACCUMULATIVE ? 'bg-accent-blue text-[#2E6F99]' : 'bg-accent-plum text-[#7A4F99]'}`}>
                        {div.mode}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading text-right">{num(div.amount)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted text-right">{num(div.dividendPerShare, 4)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-loss text-right">{num(div.withholdingTax)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gain text-right">{num(net)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted text-right">{div.currency}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <button onClick={() => onDelete(div.id)} className="text-text-muted hover:text-loss transition-colors" title="Delete dividend">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
