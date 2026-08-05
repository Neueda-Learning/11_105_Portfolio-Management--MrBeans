import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useInvestmentDetail } from './hooks/useInvestmentDetail';
import { PnlBreakdown } from './PnlBreakdown';
import { TransactionHistoryTable } from './TransactionHistoryTable';
import { TransactionFormModal } from './TransactionFormModal';
import { ChevronLeft, Plus } from 'lucide-react';
import { transactionsApi } from '../../api/transactions';


export const InvestmentDetailPage = () => {
  const { id } = useParams();
  const { investment, transactions, pnl, isLoading, error, refresh } = useInvestmentDetail(id || '');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleAddTransaction = async (data) => {
    try {
      await transactionsApi.create(data);
      await refresh();
      return { success: true, error: null };
    } catch (err) {
      return { success: false, error: err };
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
                        <button
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center px-5 py-3 border border-transparent text-[15px] font-semibold rounded-md shadow-sm text-white bg-accent-pink hover:bg-accent-pink-strong focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-pink">
              
                            <Plus className="w-4 h-4 mr-2" /> Add Transaction
                        </button>
                    </div>
                </div>
            </div>

            <PnlBreakdown
        realisedPnl={pnl.realisedPnl}
        unrealisedPnl={pnl.unrealisedPnl} />
      

            <TransactionHistoryTable transactions={transactions} investmentCurrency={investment.currency} />

            <TransactionFormModal
        isOpen={isModalOpen}
        investmentId={investment.id}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleAddTransaction} />
      
        </div>);

};