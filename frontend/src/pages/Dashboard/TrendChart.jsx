import React from 'react';
import { Area, AreaChart, CartesianGrid, Line, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

const formatCurrencyCompact = (value) => {
  if (typeof value !== 'number') {
    return '$0';
  }

  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    notation: 'compact',
    maximumFractionDigits: 1
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
  if (!active || !payload || payload.length === 0) {
    return null;
  }

  const value = payload[0]?.value ?? 0;

  return (
    <div className="rounded-xl border border-accent-pink/25 bg-white/95 px-3 py-2 shadow-md">
      <div className="text-[11px] text-text-muted">{formatDateLabel(label)}</div>
      <div className="text-sm font-semibold text-text-heading">{formatCurrencyCompact(value)}</div>
    </div>
  );
};

export const TrendChart = ({ data, showCard = true, showTitle = true }) => {
  // Determine overall trend to strictly map line color to gain/loss token
  const isGain = data.length > 1 && data[data.length - 1].value >= data[0].value;
  const lineColor = isGain ? '#8FE365' : '#F0645A'; // gain or loss
  const startColor = isGain ? '#BDEFA3' : '#F7BBB7';

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
                tickFormatter={formatCurrencyCompact}
              />

              <Tooltip content={<TrendTooltip />} />

              <Area type="monotone" dataKey="value" stroke="none" fill="url(#trendArea)" />
              <Line
                type="monotone"
                dataKey="value"
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
        </div>);

};