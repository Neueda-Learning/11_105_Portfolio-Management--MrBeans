import React from 'react';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';
import { ChatbotWidget } from '../ChatbotWidget/ChatbotWidget';





export const AppShell = ({ children }) => {
  return (
    <div className="min-h-screen bg-page text-text-body font-sans">
            <Sidebar />
            <TopBar />
            
            {/* Main content offset by sidebar width and topbar height */}
            <main className="ml-64 pt-16 min-h-screen">
                <div className="p-8 max-w-7xl mx-auto">
                    {children}
                </div>
            </main>

            <ChatbotWidget />
        </div>);

};