import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';






// Strictly enforced brand colors mapping
const COLORS = ['#FFB3C6', '#FFE59A', '#C9E4F6', '#8FE365', '#D9C4F0'];

export const AllocationChart = ({ data }) => {
  return (
    <div className="bg-card rounded-lg p-6 h-96 flex flex-col">
            <h3 className="text-lg font-medium text-text-heading mb-4">Asset Allocation</h3>
            <div className="flex-1">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={80}
              outerRadius={120}
              paddingAngle={2}
              dataKey="value"
              nameKey="type">
              
                            {data.map((entry, index) =>
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              )}
                        </Pie>
                        <Tooltip
              formatter={(value) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)}
              contentStyle={{ borderRadius: '0.375rem', border: '1px solid #E9ECEF', color: '#1C1E21' }}
              itemStyle={{ color: '#1C1E21' }} />
            
                        <Legend verticalAlign="bottom" height={36} wrapperStyle={{ fontSize: '14px', color: '#495057' }} />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>);

};