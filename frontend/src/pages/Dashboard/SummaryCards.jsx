import React from 'react';

import { TrendingUp, TrendingDown, PieChart, Wallet, BadgeDollarSign } from 'lucide-react';
import { useSettingsStore } from '../../store/useSettingsStore';





export const SummaryCards = ({ summary }) => {
  const { baseCurrency } = useSettingsStore();

  if (!summary) return null;

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: baseCurrency }).format(value);
  };

  const getPnLColor = (value) => {
    if (value > 0) return 'text-gain';
    if (value < 0) return 'text-loss';
    return 'text-text-muted';
  };

  const getPnLIcon = (value) => {
    if (value >= 0) return <TrendingUp className="w-4 h-4 mr-1 text-gain" />;
    return <TrendingDown className="w-4 h-4 mr-1 text-loss" />;
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-accent-pink text-white rounded-xl p-8">
                <div className="flex items-center text-white/90 mb-2">
                    <Wallet className="w-6 h-6 mr-2" />
                    <h3 className="text-sm font-semibold">Total Value</h3>
                </div>
                <div className="text-4xl font-semibold">
                    {formatCurrency(summary.totalValue)}
                </div>
                <div className="text-sm text-white/80 mt-2">
                    Invested: {formatCurrency(summary.totalCostBasis)}
                </div>
            </div>

            <div className="bg-card rounded-xl p-8">
                <div className="flex items-center text-text-muted mb-2">
                    <PieChart className="w-6 h-6 mr-2" />
                    <h3 className="text-sm font-semibold" data-testid="label-realised-pnl">Realised PnL</h3>
                </div>
                <div className={`text-4xl font-semibold flex items-center ${getPnLColor(summary.totalRealisedPnl)}`}>
                    {getPnLIcon(summary.totalRealisedPnl)}
                    <span data-testid="value-realised-pnl">{formatCurrency(summary.totalRealisedPnl)}</span>
                </div>
            </div>

            <div className="bg-card rounded-xl p-8">
                <div className="flex items-center text-text-muted mb-2">
                    <PieChart className="w-6 h-6 mr-2" />
                    <h3 className="text-sm font-semibold" data-testid="label-unrealised-pnl">Unrealised PnL</h3>
                </div>
                <div className={`text-4xl font-semibold flex items-center ${getPnLColor(summary.totalUnrealisedPnl)}`}>
                    {getPnLIcon(summary.totalUnrealisedPnl)}
                    <span data-testid="value-unrealised-pnl">{formatCurrency(summary.totalUnrealisedPnl)}</span>
                </div>
            </div>

            <div className="bg-card rounded-xl p-8">
                <div className="flex items-center text-text-muted mb-2">
                    <BadgeDollarSign className="w-6 h-6 mr-2" />
                    <h3 className="text-sm font-semibold">Dividend Income</h3>
                </div>
                <div className="text-4xl font-semibold text-gain">
                    {formatCurrency(summary.dividendIncomeThisYear ?? 0)}
                </div>
                <div className="text-sm text-text-muted mt-2">Net received this year</div>
            </div>
        </div>);

};