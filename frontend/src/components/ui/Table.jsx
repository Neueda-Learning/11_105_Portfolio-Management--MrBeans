import React from 'react';






export const Table = ({ headers, children }) => {
  return (
    <div className="w-full overflow-x-auto">
            <table className="w-full text-left border-collapse">
                <thead>
                    <tr className="bg-card-alt">
                        {headers.map((header, idx) =>
            <th key={idx} className="px-6 py-3 text-[13px] font-semibold text-text-muted uppercase tracking-wider">
                                {header}
                            </th>
            )}
                    </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200">
                    {children}
                </tbody>
            </table>
        </div>);

};