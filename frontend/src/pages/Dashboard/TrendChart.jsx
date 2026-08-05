import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';










export const TrendChart = ({ data }) => {
  // Determine overall trend to strictly map line color to gain/loss token
  const isGain = data.length > 1 && data[data.length - 1].value >= data[0].value;
  const lineColor = isGain ? '#8FE365' : '#F0645A'; // gain or loss

  return (
    <div className="bg-card rounded-lg p-6 h-96 flex flex-col">
            <h3 className="text-lg font-medium text-text-heading mb-4">Portfolio Trend</h3>
            <div className="flex-1">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={data}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E9ECEF" />
                        <XAxis
              dataKey="date"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#868E96', fontSize: 12 }}
              dy={10} />
            
                        <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#868E96', fontSize: 12 }}
              tickFormatter={(value) => `$${value.toLocaleString()}`}
              dx={-10} />
            
                        <Tooltip
              formatter={(value) => [`$${value.toLocaleString()}`, 'Value']}
              contentStyle={{ borderRadius: '0.375rem', border: '1px solid #E9ECEF', color: '#1C1E21' }}
              itemStyle={{ color: lineColor }} />
            
                        <Line
              type="monotone"
              dataKey="value"
              stroke={lineColor}
              strokeWidth={3}
              dot={false}
              activeDot={{ r: 6, fill: lineColor, stroke: '#fff', strokeWidth: 2 }} />
            
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>);

};