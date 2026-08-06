import React, { useState, useEffect } from 'react';
import { useInvestments } from './hooks/useInvestments';
import { InvestmentFilters } from './InvestmentFilters';
import { InvestmentFormModal } from './InvestmentFormModal';
import { InvestmentType } from '../../types/enums';
import { Plus, ChevronRight, Trash2, ChevronLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTopBarStore } from '../../store/useTopBarStore';

const DEFAULT_CURRENCY_OPTIONS = ['EUR', 'GBP', 'JPY', 'CAD', 'AUD'];
const PAGE_SIZE = 10;

const fmt = (iso) => {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
};

export const InvestmentListPage = () => {
  const { investments, isLoading, error, createInvestment, deleteInvestment } = useInvestments();
  const [filterType, setFilterType] = useState('ALL');
  const [filterCurrency, setFilterCurrency] = useState('ALL');
  // Applied date values — used for filtering
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  // Pending date values — shown in the inputs before the user clicks "Filter"
  const [pendingFrom, setPendingFrom] = useState('');
  const [pendingTo, setPendingTo] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const { setAction, clearAction } = useTopBarStore();

  // Inject "Add Investment" button into the TopBar, right-aligned next to the title
  useEffect(() => {
    setAction(
      <button
        onClick={() => setIsModalOpen(true)}
        className="flex items-center gap-2 px-5 py-2 bg-accent-pink text-white text-sm font-semibold rounded-md hover:bg-accent-pink-strong transition-colors shadow-sm">
        <Plus className="w-4 h-4" />
        Add Investment
      </button>
    );
    return clearAction;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Type / currency filter — apply immediately
  const handleTypeChange = (v) => { setFilterType(v); setCurrentPage(1); };
  const handleCurrencyChange = (v) => { setFilterCurrency(v); setCurrentPage(1); };
  // Date filter — apply only when the user clicks "Filter"
  const applyDateFilter = () => { setFromDate(pendingFrom); setToDate(pendingTo); setCurrentPage(1); };
  const clearFilters = () => {
    setFilterType('ALL'); setFilterCurrency('ALL');
    setFromDate(''); setToDate('');
    setPendingFrom(''); setPendingTo('');
    setCurrentPage(1);
  };

  const currencyOptions = [...new Set([
    ...DEFAULT_CURRENCY_OPTIONS,
    ...investments.map((inv) => inv.currency).filter(Boolean)
  ])].sort();

  const filteredInvestments = investments.filter((inv) => {
    const typeMatch = filterType === 'ALL' || inv.type === filterType;
    const currencyMatch = filterCurrency === 'ALL' || inv.currency === filterCurrency;
    if (!typeMatch || !currencyMatch) return false;
    if (!fromDate && !toDate) return true;
    if (!inv.createdAt) return false;
    // UTC date string comparison: "YYYY-MM-DD" ≤ "YYYY-MM-DD" (lexicographic = chronological)
    const invDate = inv.createdAt.slice(0, 10);
    if (fromDate && invDate < fromDate) return false;
    if (toDate && invDate > toDate) return false;
    return true;
  });

  const totalPages = Math.max(1, Math.ceil(filteredInvestments.length / PAGE_SIZE));
  const safePage = Math.min(currentPage, totalPages);
  const pageStart = (safePage - 1) * PAGE_SIZE;
  const paginatedInvestments = filteredInvestments.slice(pageStart, pageStart + PAGE_SIZE);

  const isFiltered = filterType !== 'ALL' || filterCurrency !== 'ALL' || fromDate || toDate;

  if (isLoading) {
    return <div className="text-neutral-500 animate-pulse flex justify-center p-12">Loading investments...</div>;
  }

  if (error) {
    return <div className="text-accentRose bg-accentRose/10 p-4 rounded-md">Error: {error.message}</div>;
  }

  return (
    <div className="animate-in fade-in duration-500">
      {/* Filters */}
      <InvestmentFilters
        filterType={filterType}
        onTypeChange={handleTypeChange}
        filterCurrency={filterCurrency}
        onCurrencyChange={handleCurrencyChange}
        pendingFrom={pendingFrom}
        pendingTo={pendingTo}
        onPendingFromChange={setPendingFrom}
        onPendingToChange={setPendingTo}
        onApplyDateFilter={applyDateFilter}
        hasActiveDateFilter={!!(fromDate || toDate)}
        currencyOptions={currencyOptions}
        onClearFilters={clearFilters} />

      {/* Filter result count */}
      {isFiltered && (
        <p className="text-sm text-text-muted mb-3">
          Showing <span className="font-semibold text-text-body">{filteredInvestments.length}</span> of {investments.length} investments
        </p>
      )}

      <div className="bg-card rounded-lg overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-card-alt">
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Symbol</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Name</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Class</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Currency</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider">Date Added</th>
              <th className="px-6 py-3 text-xs font-medium text-text-muted uppercase tracking-wider text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200">
            {paginatedInvestments.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-6 py-12 text-center text-text-muted">
                  {isFiltered ? 'No investments match the current filters.' : 'No investments found. Add one to get started!'}
                </td>
              </tr>
            ) : (
              paginatedInvestments.map((inv) => {
                let badgeClass = "bg-card-alt text-text-muted";
                if (inv.type === InvestmentType.STOCK) badgeClass = "bg-accent-blue text-[#2E6F99]";
                else if (inv.type === InvestmentType.BOND) badgeClass = "bg-accent-yellow text-[#8A6A0A]";
                else if (inv.type === InvestmentType.CASH) badgeClass = "bg-[#EAFBDD] text-gain-text";
                else if (inv.type === InvestmentType.OTHER) badgeClass = "bg-accent-plum text-[#7A4F99]";

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
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">
                                        {fmt(inv.createdAt)}
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
            )}
                    </tbody>
                </table>
            </div>

      {/* Pagination */}
      {filteredInvestments.length > PAGE_SIZE && (
        <div className="flex items-center justify-between mt-4 px-1">
          <span className="text-sm text-text-muted">
            Showing {pageStart + 1}–{Math.min(pageStart + PAGE_SIZE, filteredInvestments.length)} of {filteredInvestments.length}
          </span>
          <div className="flex items-center gap-2">
            <button
              disabled={safePage === 1}
              onClick={() => setCurrentPage(safePage - 1)}
              className="p-1.5 rounded-md bg-card-alt text-text-muted disabled:opacity-30 hover:bg-accent-pink/20 transition-colors">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-sm text-text-body px-1">{safePage} / {totalPages}</span>
            <button
              disabled={safePage === totalPages}
              onClick={() => setCurrentPage(safePage + 1)}
              className="p-1.5 rounded-md bg-card-alt text-text-muted disabled:opacity-30 hover:bg-accent-pink/20 transition-colors">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      <InvestmentFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={createInvestment} />
    </div>
  );

};