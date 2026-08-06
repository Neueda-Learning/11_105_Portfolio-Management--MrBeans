import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useInvestmentDetail } from './hooks/useInvestmentDetail';
import { useDividends } from './hooks/useDividends';
import { PnlBreakdown } from './PnlBreakdown';
import { TransactionHistoryTable } from './TransactionHistoryTable';
import { TransactionFormModal } from './TransactionFormModal';
import { DividendHistoryTable } from './DividendHistoryTable';
import { DividendFormModal } from './DividendFormModal';
import { ChevronLeft, Plus } from 'lucide-react';
import { transactionsApi } from '../../api/transactions';
import { dividendsApi } from '../../api/dividends';


export const InvestmentDetailPage = () => {
  const { id } = useParams();
  const { investment, transactions, pnl, isLoading, error, refresh } = useInvestmentDetail(id || '');
  const { dividends, refresh: refreshDividends } = useDividends(id || '');

  const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
  const [isDividendModalOpen, setIsDividendModalOpen] = useState(false);

  const handleAddTransaction = async (data) => {
    try {
      await transactionsApi.create(data);
      await refresh();
      return { success: true, error: null };
    } catch (err) {
      return { success: false, error: err };
    }
  };

  const handleAddDividend = async (data) => {
    try {
      await dividendsApi.create(id, data);
      await refreshDividends();
      return { success: true, error: null };
    } catch (err) {
      return { success: false, error: err };
    }
  };

  const handleDeleteDividend = async (dividendId) => {
    try {
      await dividendsApi.delete(id, dividendId);
      await refreshDividends();
    } catch (err) {
      console.error('Failed to delete dividend', err);
    }
  };

  if (isLoading) {
    return <div className="text-neutral-500 animate-pulse flex justify-center p-12">Loading details...</div>;
  }

  if (error || !investment) {
    return <div className="text-accentRose bg-accentRose/10 p-4 rounded-md">Error: {error?.message || 'Investment not found'}</div>;
  }

  return (
    <div className="animate-in fade-in duration-500">
      <div className="mb-6">
        <Link to="/investments" className="inline-flex items-center text-sm font-medium text-text-muted hover:text-accent-pink-strong transition-colors mb-4">
          <ChevronLeft className="w-4 h-4 mr-1" /> Back to Investments
        </Link>
        <div className="flex justify-between items-end">
          <div>
            <h2 className="text-4xl font-bold text-text-heading">{investment.symbol}</h2>
            <p className="text-lg text-text-muted mt-1">{investment.name}</p>
          </div>
          <div className="text-right">
            <span className="px-3 py-1 inline-flex text-sm font-semibold rounded-full bg-card-alt text-text-muted mb-2">
              {investment.type}
            </span>
            <p className="text-sm text-text-muted mb-2">Trading in {investment.currency}</p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setIsDividendModalOpen(true)}
                className="inline-flex items-center px-5 py-3 border border-accent-pink text-[15px] font-semibold rounded-md text-accent-pink hover:bg-[#FFF0F5] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-pink"
              >
                <Plus className="w-4 h-4 mr-2" /> Add Dividend
              </button>
              <button
                onClick={() => setIsTransactionModalOpen(true)}
                className="inline-flex items-center px-5 py-3 border border-transparent text-[15px] font-semibold rounded-md shadow-sm text-white bg-accent-pink hover:bg-accent-pink-strong focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-pink"
              >
                <Plus className="w-4 h-4 mr-2" /> Add Transaction
              </button>
            </div>
          </div>
        </div>
      </div>

      <PnlBreakdown realisedPnl={pnl.realisedPnl} unrealisedPnl={pnl.unrealisedPnl} />

      <TransactionHistoryTable transactions={transactions} investmentCurrency={investment.currency} />

      <DividendHistoryTable dividends={dividends} onDelete={handleDeleteDividend} />

      <TransactionFormModal
        isOpen={isTransactionModalOpen}
        investmentId={investment.id}
        symbol={investment.symbol}
        currency={investment.currency}
        onClose={() => setIsTransactionModalOpen(false)}
        onSubmit={handleAddTransaction}
      />

      <DividendFormModal
        isOpen={isDividendModalOpen}
        investmentId={investment.id}
        investmentCurrency={investment.currency}
        onClose={() => setIsDividendModalOpen(false)}
        onSubmit={handleAddDividend}
      />
    </div>
  );
};