import { Routes, Route } from 'react-router-dom';
import { AppShell } from './components/layout/AppShell';
import { DashboardPage } from './pages/Dashboard/DashboardPage';
import { InvestmentListPage } from './pages/Investments/InvestmentListPage';
import { InvestmentDetailPage } from './pages/InvestmentDetail/InvestmentDetailPage';
import { HistoryPage } from './pages/History/HistoryPage';
import { SettingsPage } from './pages/Settings/SettingsPage';

function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/investments" element={<InvestmentListPage />} />
        <Route path="/investments/:id" element={<InvestmentDetailPage />} />
        <Route path="/history" element={<HistoryPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Routes>
    </AppShell>);

}

export default App;