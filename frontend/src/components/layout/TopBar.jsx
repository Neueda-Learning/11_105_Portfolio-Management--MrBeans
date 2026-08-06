import React from 'react';
import { useLocation } from 'react-router-dom';
import { useTopBarStore } from '../../store/useTopBarStore';

export const TopBar = () => {
  const location = useLocation();
  const action = useTopBarStore((s) => s.action);

  const getPageTitle = () => {
    if (location.pathname === '/') return 'Dashboard';
    if (location.pathname.startsWith('/investments')) return 'Investments';
    if (location.pathname.startsWith('/history')) return 'Transaction History';
    if (location.pathname.startsWith('/settings')) return 'Settings';
    if (location.pathname.startsWith('/analogies')) return 'Wealth Analogies';
    return 'Finora';
  };

  return (
    <header className="h-16 bg-card flex items-center px-8 fixed top-0 right-0 left-64 z-10 border-b border-neutral-100">
      <h2 className="text-2xl font-bold text-text-heading">{getPageTitle()}</h2>
      {action && <div className="ml-auto">{action}</div>}
    </header>
  );
};