import React from 'react';
import { Area, AreaChart, CartesianGrid, Line, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { useSettingsStore } from '../../store/useSettingsStore';

const formatCurrencyCompact = (value, currency = 'USD') => {
  if (typeof value !== 'number') return `0`;
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value);
};

const formatDateLabel = (value) => {
  if (!value) {
    return '';
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const TrendTooltip = ({ active, payload, label }) => {
  const baseCurrency = useSettingsStore((s) => s.baseCurrency) || 'USD';
  const fmt = (v) => formatCurrencyCompact(v, baseCurrency);
  if (!active || !payload || payload.length === 0) {
    return null;
  }

  const portfolioValue = payload.find((p) => p.dataKey === 'portfolioValue')?.value;
  const investedAmount = payload.find((p) => p.dataKey === 'investedAmount')?.value;

  return (
    <div className="rounded-xl border border-accent-pink/25 bg-white/95 px-3 py-2 shadow-md">
      <div className="text-[11px] text-text-muted">{formatDateLabel(label)}</div>
      {portfolioValue != null && (
        <div className="text-sm font-semibold text-text-heading">
          Total Wealth: {fmt(portfolioValue)}
        </div>
      )}
      {investedAmount != null && (
        <div className="text-xs text-text-muted">
          Invested: {fmt(investedAmount)}
        </div>
      )}
    </div>
  );
};

export const TrendChart = ({ data, showCard = true, showTitle = true }) => {
  const baseCurrency = useSettingsStore((s) => s.baseCurrency) || 'USD';
  const fmt = (v) => formatCurrencyCompact(v, baseCurrency);
  // Determine overall trend to strictly map line color to gain/loss token
  const isGain = data.length > 1 && data[data.length - 1].portfolioValue >= data[0].portfolioValue;
  const lineColor = isGain ? '#8FE365' : '#F0645A'; // gain or loss
  const startColor = isGain ? '#BDEFA3' : '#F7BBB7';
  const investedColor = '#ADB5BD'; // neutral gray for invested line

  const chartContent = (
    <>
      {showTitle && <h3 className="text-lg font-medium text-text-heading mb-4">Portfolio Trend</h3>}
      <div className="flex-1 rounded-2xl bg-white/55 border border-accent-pink/15 p-2 md:p-3">
        {data.length === 0 ? (
          <div className="h-full flex items-center justify-center text-sm text-text-muted">
            No trend data available for this filter.
          </div>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ top: 10, right: 8, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="trendArea" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor={startColor} stopOpacity={0.38} />
                  <stop offset="100%" stopColor={startColor} stopOpacity={0.04} />
                </linearGradient>
              </defs>

              <CartesianGrid strokeDasharray="3 4" vertical={false} stroke="#EED6DF" />
              <XAxis
                dataKey="date"
                tickFormatter={formatDateLabel}
                axisLine={false}
                tickLine={false}
                minTickGap={18}
                tick={{ fill: '#8A7B85', fontSize: 11 }}
              />

              <YAxis
                axisLine={false}
                tickLine={false}
                width={62}
                tick={{ fill: '#8A7B85', fontSize: 11 }}
                tickFormatter={fmt}
              />

              <Tooltip content={<TrendTooltip />} />
              <Legend
                formatter={(value) =>
                  value === 'portfolioValue' ? 'Total Wealth (incl. gains)' : 'Invested (cost basis)'
                }
                wrapperStyle={{ fontSize: '11px', color: '#8A7B85', paddingTop: '4px' }}
              />

              {/* Gradient area under the wealth line — legendType=none to avoid duplicate legend entry */}
              <Area type="monotone" dataKey="portfolioValue" stroke="none" fill="url(#trendArea)" legendType="none" />
              {/* Dashed gray line: cost basis of remaining holdings */}
              <Line
                type="monotone"
                dataKey="investedAmount"
                stroke={investedColor}
                strokeWidth={1.5}
                strokeDasharray="5 4"
                dot={false}
                activeDot={{ r: 4, fill: investedColor, stroke: '#fff', strokeWidth: 2 }}
              />
              {/* Solid colored line: total wealth = market value + realised PnL */}
              <Line
                type="monotone"
                dataKey="portfolioValue"
                stroke={lineColor}
                strokeWidth={2.8}
                dot={false}
                activeDot={{ r: 5, fill: lineColor, stroke: '#fff', strokeWidth: 2 }}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </div>
    </>
  );

  if (!showCard) {
    return <div className="h-full flex flex-col">{chartContent}</div>;
  }

  return (
    <div className="bg-card rounded-lg p-6 h-96 flex flex-col">
      {chartContent}
    </div>
  );
};