import React from 'react';
import { TrendingUp, TrendingDown, PieChart } from 'lucide-react';
import { useSettingsStore } from '../../store/useSettingsStore';






export const PnlBreakdown = ({ realisedPnl, unrealisedPnl }) => {
  const { baseCurrency } = useSettingsStore();

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
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div className="bg-card rounded-xl p-8">
                <div className="flex items-center text-text-muted mb-2">
                    <PieChart className="w-6 h-6 mr-2" />
                    <h3 className="text-sm font-semibold text-text-body">Realised PnL</h3>
                </div>
                <div className={`text-4xl font-semibold flex items-center ${getPnLColor(realisedPnl)}`}>
                    {getPnLIcon(realisedPnl)}
                    <span>{formatCurrency(realisedPnl)}</span>
                </div>
            </div>

            <div className="bg-card rounded-xl p-8">
                <div className="flex items-center text-text-muted mb-2">
                    <PieChart className="w-6 h-6 mr-2" />
                    <h3 className="text-sm font-semibold text-text-body">Unrealised PnL</h3>
                </div>
                <div className={`text-4xl font-semibold flex items-center ${getPnLColor(unrealisedPnl)}`}>
                    {getPnLIcon(unrealisedPnl)}
                    <span>{formatCurrency(unrealisedPnl)}</span>
                </div>
            </div>
        </div>);

};