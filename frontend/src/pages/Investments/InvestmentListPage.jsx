import React, { useState } from 'react';
import { useInvestments } from './hooks/useInvestments';
import { InvestmentFilters } from './InvestmentFilters';
import { InvestmentFormModal } from './InvestmentFormModal';
import { InvestmentType } from '../../types/enums';
import { Plus, ChevronRight, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';

export const InvestmentListPage = () => {
  const { investments, isLoading, error, createInvestment, deleteInvestment } = useInvestments();
  const [filterType, setFilterType] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const filteredInvestments = filterType === 'ALL' ?
  investments :
  investments.filter((inv) => inv.type === filterType);

  if (isLoading) {
    return <div className="text-neutral-500 animate-pulse flex justify-center p-12">Loading investments...</div>;
  }

  if (error) {
    return <div className="text-accentRose bg-accentRose/10 p-4 rounded-md">Error: {error.message}</div>;
  }

  return (
    <div className="animate-in fade-in duration-500">
            <div className="flex justify-between items-center mb-6">
                <InvestmentFilters filterType={filterType} onFilterChange={setFilterType} />
                <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center px-5 py-3 bg-accent-pink text-white text-[15px] font-semibold rounded-md hover:bg-accent-pink-strong transition-colors">
          
                    <Plus className="w-4 h-4 mr-2" />
                    Add Investment
                </button>
            </div>

            <div className="bg-card rounded-lg overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-card-alt">
                            <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Symbol</th>
                            <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Name</th>
                            <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Class</th>
                            <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Currency</th>
                            <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-neutral-200">
                        {filteredInvestments.length === 0 ?
            <tr>
                                <td colSpan={5} className="px-6 py-12 text-center text-text-muted">
                                    No investments found. Add one to get started!
                                </td>
                            </tr> :

            filteredInvestments.map((inv) => {
              let badgeClass = "bg-card-alt text-text-muted";
              if (inv.type === InvestmentType.STOCK) badgeClass = "bg-accent-blue text-[#2E6F99]";else
              if (inv.type === InvestmentType.BOND) badgeClass = "bg-accent-yellow text-[#8A6A0A]";else
              if (inv.type === InvestmentType.CASH) badgeClass = "bg-[#EAFBDD] text-gain-text";else
              if (inv.type === InvestmentType.OTHER) badgeClass = "bg-accent-plum text-[#7A4F99]";

              return (
                <tr key={inv.id} className="hover:bg-card-alt transition-colors group">
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-text-heading group-hover:text-accent-pink-strong">
                                        {inv.symbol}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">
                                        {inv.name}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${badgeClass}`}>
                                            {inv.type}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">
                                        {inv.currency}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium flex justify-end space-x-2">
                                        <button
                      onClick={() => {
                        if (confirm('Are you sure you want to delete this investment?')) {
                          deleteInvestment(inv.id);
                        }
                      }}
                      className="text-text-muted hover:text-loss p-1 transition-colors"
                      title="Delete">
                      
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                        <Link
                      to={`/investments/${inv.id}`}
                      className="text-accent-pink hover:text-accent-pink-strong flex items-center p-1">
                      
                                            View <ChevronRight className="w-4 h-4 ml-1" />
                                        </Link>
                                    </td>
                                </tr>);

            })
            }
                    </tbody>
                </table>
            </div>

            <InvestmentFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={createInvestment} />
      
        </div>);

};