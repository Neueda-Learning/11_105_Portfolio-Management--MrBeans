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
});