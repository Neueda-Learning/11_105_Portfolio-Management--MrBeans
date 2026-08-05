import React from 'react';
import { ScatterChart, Scatter, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';











export const PerformanceScatter = ({ data }) => {
  return (
    <div className="bg-card rounded-lg p-6 h-96 flex flex-col">
            <h3 className="text-lg font-medium text-text-heading mb-4">Risk vs Return</h3>
            <div className="flex-1">
                <ResponsiveContainer width="100%" height="100%">
                    <ScatterChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#FFEAF1" />
                        <XAxis
              type="number"
              dataKey="risk"
              name="Risk"
              unit="%"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#868E96', fontSize: 12 }}
              label={{ value: 'Risk (Volatility)', position: 'insideBottom', offset: -10, fill: '#868E96' }} />
            
                        <YAxis
              type="number"
              dataKey="return"
              name="Return"
              unit="%"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#868E96', fontSize: 12 }}
              label={{ value: 'Return', angle: -90, position: 'insideLeft', offset: -10, fill: '#868E96' }} />
            
                        <Tooltip
              cursor={{ strokeDasharray: '3 3', stroke: '#ADB5BD' }}
              contentStyle={{ borderRadius: '0.375rem', border: '1px solid #E9ECEF', color: '#1C1E21' }}
              formatter={(val, name) => [`${val}%`, name]} />
            
                        <Scatter name="Assets" data={data}>
                            {data.map((entry, index) =>
              <Cell
                key={`cell-${index}`}
                fill={entry.return >= 0 ? '#8FE365' : '#F0645A'} />

              )}
                        </Scatter>
                    </ScatterChart>
                </ResponsiveContainer>
            </div>
        </div>);

};