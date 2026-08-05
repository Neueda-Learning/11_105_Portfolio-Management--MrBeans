import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Wallet, History, Settings } from 'lucide-react';

export const Sidebar = () => {
  const navItems = [
  { name: 'Dashboard', path: '/', icon: <LayoutDashboard className="w-6 h-6 mr-3" /> },
  { name: 'Investments', path: '/investments', icon: <Wallet className="w-6 h-6 mr-3" /> },
  { name: 'History', path: '/history', icon: <History className="w-6 h-6 mr-3" /> },
  { name: 'Settings', path: '/settings', icon: <Settings className="w-6 h-6 mr-3" /> }];


  return (
    <aside className="w-64 bg-card text-text-body min-h-screen flex flex-col fixed left-0 top-0">
            <div className="p-6">
                <h1 className="text-2xl font-bold tracking-tight text-text-heading">PoMa</h1>
            </div>
            
            <nav className="flex-1 px-4 space-y-2 mt-4">
                {navItems.map((item) =>
        <NavLink
          key={item.path}
          to={item.path}
          className={({ isActive }) =>
          `flex items-center px-4 py-3 rounded-md transition-colors ${
          isActive ?
          'bg-accent-pink text-text-onFill' :
          'text-[#E888A8] hover:text-accent-pink-strong hover:bg-card-alt'}`

          }>
          
                        {item.icon}
                        <span className="font-semibold text-[15px]">{item.name}</span>
                    </NavLink>
        )}
            </nav>

            <div className="p-6 text-xs text-text-muted">
                &copy; {new Date().getFullYear()} Portfolio Manager
            </div>
        </aside>);

};