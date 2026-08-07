import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { SummaryCards } from '../SummaryCards';

describe('SummaryCards', () => {
  it('strictly separates Realised and Unrealised PnL in the DOM per Section 7', () => {
    const mockSummary = {
      totalValue: 10000,
      totalCostBasis: 8000,
      totalRealisedPnl: 1500,
      totalUnrealisedPnl: 500
    };

    render(<SummaryCards summary={mockSummary} />);

    // Ensure both distinct labels exist
    expect(screen.getByTestId('label-realised-pnl')).toHaveTextContent('Realised PnL');
    expect(screen.getByTestId('label-unrealised-pnl')).toHaveTextContent('Unrealised PnL');

    // Ensure they render their distinct numbers
    expect(screen.getByTestId('value-realised-pnl')).toHaveTextContent('$1,500.00');
    expect(screen.getByTestId('value-unrealised-pnl')).toHaveTextContent('$500.00');

    // Ensure "Total PnL" does NOT exist anywhere in the DOM
    const textNodes = screen.queryAllByText(/Total PnL/i);
    expect(textNodes.length).toBe(0);
  });

  it('renders large total value amounts without losing the summary content', () => {
    const mockSummary = {
      totalValue: 9876543210,
      totalCostBasis: 1234567890,
      totalRealisedPnl: 222222222,
      totalUnrealisedPnl: 333333333,
      dividendIncomeThisYear: 44444444
    };

    render(<SummaryCards summary={mockSummary} />);

    expect(screen.getByText('$9,876,543,210.00')).toBeInTheDocument();
    expect(screen.getByText(/Invested:/i)).toBeInTheDocument();
    expect(screen.getByTestId('value-realised-pnl')).toHaveTextContent('$222,222,222.00');
    expect(screen.getByTestId('value-unrealised-pnl')).toHaveTextContent('$333,333,333.00');
  });
});