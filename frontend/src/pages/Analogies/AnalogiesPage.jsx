import React, { useMemo } from 'react';
import { usePortfolioSummary } from '../Dashboard/hooks/usePortfolioSummary';
import { ANALOGIES, fmtAnalogy } from './analogiesData';

const fmt = fmtAnalogy;

/* ── Sub-components ─────────────────────────────────────────────────────── */

const HeroStat = ({ profit }) => {
  const best = [...ANALOGIES].reverse().find((a) => profit >= a.value);
  const next = ANALOGIES.find((a) => profit < a.value);
  const pct  = next ? Math.min(100, (profit / next.value) * 100) : 100;

  return (
    <div className="rounded-2xl bg-gradient-to-br from-accent-pink via-[#f9a8d4] to-[#93c5fd] p-px mb-8 shadow-lg">
      <div className="rounded-2xl bg-page px-8 py-6">
        <p className="text-sm font-semibold text-text-muted uppercase tracking-widest mb-1">Your Total Profit</p>
        <p className="text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-accent-pink to-[#3b82f6]">
          {profit >= 0 ? '+' : ''}{new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(profit)}
        </p>

        {best && (
          <p className="mt-3 text-base text-text-body">
            That's enough to buy&nbsp;
            <span className="font-bold text-accent-pink-strong">{Math.floor(profit / best.value).toLocaleString()}×</span>
            &nbsp;{best.emoji}&nbsp;<span className="font-semibold">{best.name}</span>!
          </p>
        )}

        {next && (
          <div className="mt-4">
            <div className="flex justify-between text-xs text-text-muted mb-1">
              <span>Progress to next: {next.emoji} {next.name} ({fmt(next.value)})</span>
              <span>{pct.toFixed(1)}%</span>
            </div>
            <div className="w-full h-2.5 rounded-full bg-neutral-200 overflow-hidden">
              <div
                className="h-full rounded-full bg-gradient-to-r from-accent-pink to-[#60a5fa] transition-all duration-700"
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const AnalogyCard = ({ analogy, profit, index }) => {
  const unlocked = profit >= analogy.value;
  const isNext   = !unlocked && profit < analogy.value && ANALOGIES.find((a) => profit < a.value) === analogy;
  const count    = unlocked ? Math.floor(profit / analogy.value) : 0;

  const pinkGrad   = 'from-[#fff0f5] to-[#fce7f3]';
  const blueGrad   = 'from-[#eff6ff] to-[#dbeafe]';
  const borderPink = 'border-accent-pink/40';
  const borderBlue = 'border-[#93c5fd]/60';

  const grad   = analogy.color === 'pink' ? pinkGrad   : blueGrad;
  const border = analogy.color === 'pink' ? borderPink : borderBlue;

  return (
    <div
      className={`relative rounded-2xl border p-5 transition-all duration-300 flex flex-col gap-2
        ${unlocked
          ? `bg-gradient-to-br ${grad} ${border} shadow-md`
          : isNext
          ? `bg-gradient-to-br ${grad} ${border} shadow-md ring-2 ring-accent-pink/50 animate-pulse`
          : 'bg-card border-neutral-100 opacity-50'
        }`}
    >
      {/* Status badge */}
      {unlocked && (
        <span className="absolute top-3 right-3 text-xs font-bold px-2 py-0.5 rounded-full bg-gain text-gain-text">
          ✓ Unlocked
        </span>
      )}
      {isNext && (
        <span className="absolute top-3 right-3 text-xs font-bold px-2 py-0.5 rounded-full bg-accent-pink text-white">
          🎯 Next Goal
        </span>
      )}
      {!unlocked && !isNext && (
        <span className="absolute top-3 right-3 text-xs text-text-muted">🔒</span>
      )}

      {/* Emoji */}
      <div className="text-5xl leading-none">{analogy.emoji}</div>

      {/* Info */}
      <div>
        <p className="font-bold text-text-heading text-base">{analogy.name}</p>
        <p className="text-xs text-text-muted mt-0.5">{analogy.desc}</p>
      </div>

      {/* Price */}
      <p className={`text-lg font-extrabold ${analogy.color === 'pink' ? 'text-accent-pink-strong' : 'text-[#2563eb]'}`}>
        {fmt(analogy.value)}
      </p>

      {/* Count */}
      {unlocked && count > 0 && (
        <p className="text-xs text-text-muted">
          You could buy <span className="font-bold text-text-body">{count.toLocaleString()}</span> of these
        </p>
      )}

      {/* Progress for next */}
      {isNext && (
        <div>
          <div className="w-full h-1.5 rounded-full bg-white/60 overflow-hidden mt-1">
            <div
              className="h-full rounded-full bg-gradient-to-r from-accent-pink to-[#60a5fa]"
              style={{ width: `${Math.min(100, (profit / analogy.value) * 100).toFixed(1)}%` }}
            />
          </div>
          <p className="text-xs text-text-muted mt-1">
            {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(analogy.value - profit)} more to go
          </p>
        </div>
      )}
    </div>
  );
};

/* ── Page ────────────────────────────────────────────────────────────────── */

export const AnalogiesPage = () => {
  const { summary, isLoading } = usePortfolioSummary();

  const profit = useMemo(() => {
    if (!summary) return 0;
    return (Number(summary.totalRealisedPnl) || 0) + (Number(summary.totalUnrealisedPnl) || 0);
  }, [summary]);

  if (isLoading && !summary) {
    return <div className="text-neutral-500 animate-pulse flex justify-center p-12">Loading your wealth data…</div>;
  }

  const unlockedCount = ANALOGIES.filter((a) => profit >= a.value).length;

  return (
    <div className="animate-in fade-in duration-500 max-w-5xl mx-auto">
      {/* Hero stat */}
      <HeroStat profit={profit} />

      {/* Progress summary */}
      <div className="flex items-center gap-3 mb-6">
        <div className="flex-1 h-px bg-neutral-200" />
        <span className="text-sm font-semibold text-text-muted px-2">
          {unlockedCount} / {ANALOGIES.length} milestones unlocked
        </span>
        <div className="flex-1 h-px bg-neutral-200" />
      </div>

      {/* Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {ANALOGIES.map((analogy, i) => (
          <AnalogyCard key={analogy.name} analogy={analogy} profit={profit} index={i} />
        ))}
      </div>

      <p className="text-center text-xs text-text-muted mt-8">
        Based on your combined unrealised + realised profit in your base currency.
        Prices are illustrative USD approximations.
      </p>
    </div>
  );
};
