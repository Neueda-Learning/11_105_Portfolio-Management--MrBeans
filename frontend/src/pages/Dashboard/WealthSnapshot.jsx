import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { ANALOGIES, fmtAnalogy } from '../Analogies/analogiesData';
import { Sparkles, ChevronRight } from 'lucide-react';
import { useSettingsStore } from '../../store/useSettingsStore';

/**
 * WealthSnapshot — compact dashboard widget.
 * Shows what the user can buy TODAY with their total profit, plus the next milestone.
 * Placed on the dashboard between SummaryCards and the charts grid.
 *
 * Props:
 *   summary — the full DashboardSummary object (may be null while loading)
 */
export const WealthSnapshot = ({ summary }) => {
  const baseCurrency = useSettingsStore((s) => s.baseCurrency) || 'USD';
  const profit = useMemo(() => {
    if (!summary) return 0;
    return (summary.totalRealisedPnl ?? 0) + (summary.totalUnrealisedPnl ?? 0);
  }, [summary]);

  const { best, bestCount, next, pct } = useMemo(() => {
    const best      = [...ANALOGIES].reverse().find((a) => profit >= a.value) ?? null;
    const next      = ANALOGIES.find((a) => profit < a.value) ?? null;
    const bestCount = best ? Math.floor(profit / best.value) : 0;
    const pct       = next ? Math.min(100, (profit / next.value) * 100) : 100;
    return { best, bestCount, next, pct };
  }, [profit]);

  // Don't render before data is ready
  if (!summary) return null;

  const profitFmt = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: baseCurrency,
    maximumFractionDigits: 0,
  }).format(Math.abs(profit));

  const isPositive = profit >= 0;

  return (
    <div className="rounded-xl border border-neutral-100 bg-card overflow-hidden">
      {/* Header strip */}
      <div className="flex items-center justify-between px-5 py-3 bg-gradient-to-r from-accent-pink/10 to-[#bfdbfe]/20 border-b border-neutral-100">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-accent-pink" />
          <span className="text-sm font-semibold text-text-heading">What your profit can buy</span>
        </div>
        <Link
          to="/analogies"
          className="flex items-center gap-0.5 text-xs text-text-muted hover:text-accent-pink transition-colors">
          All milestones <ChevronRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      <div className="flex flex-col sm:flex-row items-stretch divide-y sm:divide-y-0 sm:divide-x divide-neutral-100">

        {/* Left: total profit */}
        <div className="flex flex-col justify-center px-6 py-4 min-w-[160px]">
          <p className="text-xs text-text-muted uppercase tracking-wider mb-0.5">Total Profit</p>
          <p className={`text-2xl font-extrabold ${isPositive ? 'text-gain-text' : 'text-loss'}`}>
            {isPositive ? '+' : '−'}{profitFmt}
          </p>
        </div>

        {/* Middle: what you can buy now */}
        <div className="flex flex-col justify-center px-6 py-4 flex-1">
          {best ? (
            <>
              <p className="text-xs text-text-muted uppercase tracking-wider mb-1">You can afford right now</p>
              <div className="flex items-center gap-2">
                <span className="text-3xl leading-none">{best.emoji}</span>
                <div>
                  <p className="text-sm font-bold text-text-heading leading-tight">{best.name}</p>
                  <p className="text-xs text-text-muted">{best.desc}</p>
                </div>
                {bestCount > 1 && (
                  <span className="ml-2 px-2 py-0.5 rounded-full bg-accent-pink/15 text-accent-pink-strong text-xs font-bold">
                    ×{bestCount.toLocaleString()}
                  </span>
                )}
              </div>
            </>
          ) : (
            <p className="text-sm text-text-muted italic">Keep growing — your first milestone is close!</p>
          )}
        </div>

        {/* Right: next milestone + progress bar */}
        {next && (
          <div className="flex flex-col justify-center px-6 py-4 min-w-[220px]">
            <p className="text-xs text-text-muted uppercase tracking-wider mb-1">Next milestone</p>
            <div className="flex items-center gap-2 mb-2">
              <span className="text-2xl leading-none">{next.emoji}</span>
              <div>
                <p className="text-sm font-semibold text-text-heading leading-tight">{next.name}</p>
                <p className="text-xs text-text-muted">{fmtAnalogy(next.value)}</p>
              </div>
            </div>
            <div className="w-full h-2 rounded-full bg-neutral-200 overflow-hidden">
              <div
                className="h-full rounded-full bg-gradient-to-r from-accent-pink to-[#60a5fa] transition-all duration-700"
                style={{ width: `${pct}%` }}
              />
            </div>
            <p className="text-right text-[11px] text-text-muted mt-0.5">{pct.toFixed(1)}%</p>
          </div>
        )}
      </div>
    </div>
  );
};
