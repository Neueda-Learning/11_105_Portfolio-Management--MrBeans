import React from 'react';
import { useHistory } from './hooks/useHistory';
import { Table } from '../../components/ui/Table';
import { Card } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Link } from 'react-router-dom';
import { TransactionType } from '../../types/enums';

export const HistoryPage = () => {
  const { history, isLoading, error } = useHistory();

  if (isLoading) {
    return <div className="text-neutral-500 animate-pulse flex justify-center p-12">Aggregating portfolio history...</div>;
  }

  if (error) {
    return <div className="text-accentRose bg-accentRose/10 p-4 rounded-md">Error: {error.message}</div>;
  }

  const headers = [
  'Date',
  'Asset',
  'Type',
  'Quantity',
  'Price',
  'Total Value'];


  return (
    <div className="animate-in fade-in duration-500">
            <Card>
                <Table headers={headers}>
                    {history.length === 0 ?
          <tr>
                            <td colSpan={6} className="px-6 py-12 text-center text-neutral-500">
                                No transactions found in your portfolio history.
                            </td>
                        </tr> :

          history.map((row) =>
          <tr key={row.id} className="hover:bg-card-alt transition-colors">
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-muted">
                                    {row.txnDate ? new Intl.DateTimeFormat('en-US', {
                month: 'short', day: '2-digit', year: 'numeric',
                hour: '2-digit', minute: '2-digit'
              }).format(new Date(row.txnDate)) : 'N/A'}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-text-heading">
                                    <Link to={`/investments/${row.investmentId}`} className="hover:text-accent-pink-strong transition-colors">
                                        {row.symbol}
                                    </Link>
                                    <span className="ml-2 font-normal text-text-muted text-xs hidden sm:inline-block">
                                        {row.investmentName}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm">
                                    <Badge variant={row.type === TransactionType.BUY ? 'blue' : 'orange'}>
                                        {row.type}
                                    </Badge>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading">
                                    {row.quantity.toLocaleString(undefined, { maximumFractionDigits: 4 })}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-text-heading">
                                    {new Intl.NumberFormat('en-US', { style: 'currency', currency: row.currency }).format(row.price)}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-text-heading">
                                    {new Intl.NumberFormat('en-US', { style: 'currency', currency: row.currency }).format(row.price * row.quantity)}
                                </td>
                            </tr>
          )
          }
                </Table>
            </Card>
        </div>);

};