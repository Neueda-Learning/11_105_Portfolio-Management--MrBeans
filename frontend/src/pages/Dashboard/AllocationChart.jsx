import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { useSettingsStore } from '../../store/useSettingsStore';

const TYPE_COLORS = {
  STOCK: '#C9E4F6',
  BOND:  '#FFE59A',
  CASH:  '#8FE365',
  OTHER: '#D9C4F0',
};

const formatCompact = (v, currency = 'USD') =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(v);

const CustomTooltip = ({ active, payload }) => {
  const baseCurrency = useSettingsStore((s) => s.baseCurrency) || 'USD';
  if (!active || !payload?.length) return null;
  const d = payload[0];
  return (
    <div style={{ background: '#fff', border: '1px solid #E9ECEF', borderRadius: '0.375rem', padding: '8px 12px', color: '#1C1E21', fontSize: 13 }}>
      <div style={{ fontWeight: 600 }}>{d.payload.type}</div>
      <div>{new Intl.NumberFormat('en-US', { style: 'currency', currency: baseCurrency, minimumFractionDigits: 2 }).format(d.value)}</div>
      <div style={{ color: '#868E96' }}>{d.payload.pct}% of portfolio</div>
    </div>
  );
};

export const AllocationChart = ({ data }) => {
  const baseCurrency = useSettingsStore((s) => s.baseCurrency) || 'USD';
  const yFmt = (v) => formatCompact(v, baseCurrency);
  const rawData = Array.isArray(data) ? data : [];
  const total = rawData.reduce((s, d) => s + (d.totalValue ?? 0), 0);
  const chartData = rawData.map((d) => ({
    ...d,
    pct: total > 0 ? ((d.totalValue / total) * 100).toFixed(1) : '0.0',
  }));

  return (
    <div className="bg-card rounded-lg p-6 h-96 flex flex-col">
      <h3 className="text-lg font-medium text-text-heading mb-4">Asset Allocation</h3>
      <div className="flex-1">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 8, right: 16, left: 8, bottom: 8 }} barCategoryGap="35%">
            <CartesianGrid strokeDasharray="3 3" stroke="#F0F0F0" vertical={false} />
            <XAxis
              dataKey="type"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#868E96', fontSize: 12 }}
            />
            <YAxis
              tickFormatter={yFmt}
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#868E96', fontSize: 11 }}
              width={60}
            />
            <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(0,0,0,0.04)' }} />
            <Bar dataKey="totalValue" radius={[6, 6, 0, 0]}>
              {chartData.map((entry) => (
                <Cell key={entry.type} fill={TYPE_COLORS[entry.type] ?? '#FFB3C6'} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};