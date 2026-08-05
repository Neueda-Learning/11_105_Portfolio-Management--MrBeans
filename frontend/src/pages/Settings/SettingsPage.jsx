import React, { useState, useEffect } from 'react';
import { useSettingsStore } from '../../store/useSettingsStore';
import { settingsApi } from '../../api/settings';
import { Card } from '../../components/ui/Card';


export const SettingsPage = () => {
  const { baseCurrency, setBaseCurrency } = useSettingsStore();
  const [selectedCurrency, setSelectedCurrency] = useState(baseCurrency);
  const [isLoading, setIsLoading] = useState(false);
  const [statusMsg, setStatusMsg] = useState(null);

  useEffect(() => {
    setSelectedCurrency(baseCurrency);
  }, [baseCurrency]);

  const handleSave = async () => {
    setIsLoading(true);
    setStatusMsg(null);
    try {
      await settingsApi.updateSettings({ baseCurrency: selectedCurrency });
      setBaseCurrency(selectedCurrency);
      setStatusMsg({ type: 'success', text: 'Settings saved successfully.' });
    } catch (err) {
      setStatusMsg({ type: 'error', text: err.message || 'Failed to save settings.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-2xl animate-in fade-in duration-500">
            <Card className="p-8 border-0 shadow-none">
                <h3 className="text-lg font-medium text-text-heading mb-6">Application Settings</h3>
                
                {statusMsg &&
        <div className={`p-4 rounded-md mb-6 ${statusMsg.type === 'success' ? 'bg-[#EAFBDD] text-gain-text' : 'bg-loss/10 text-loss'}`}>
                        {statusMsg.text}
                    </div>
        }

                <div className="space-y-6">
                    <div>
                        <label className="block text-sm font-bold text-text-body mb-2">
                            Base Currency
                        </label>
                        <p className="text-sm text-text-muted mb-4">
                            All dashboard PnL calculations will be converted and displayed in this base currency using real-time FX rates.
                        </p>
                        <select
              value={selectedCurrency}
              onChange={(e) => setSelectedCurrency(e.target.value)}
              className="w-full max-w-xs px-3 py-2 border border-[#FFE6EE] rounded-md bg-white focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink">
              
                            <option value="USD">USD - US Dollar</option>
                            <option value="EUR">EUR - Euro</option>
                            <option value="GBP">GBP - British Pound</option>
                            <option value="JPY">JPY - Japanese Yen</option>
                            <option value="CAD">CAD - Canadian Dollar</option>
                            <option value="AUD">AUD - Australian Dollar</option>
                        </select>
                    </div>

                    <div className="pt-6 border-t border-[#FFE6EE] flex justify-end">
                        <button
              onClick={handleSave}
              disabled={isLoading || selectedCurrency === baseCurrency}
              className="px-6 py-2 text-sm font-medium text-white bg-accent-pink rounded-md hover:bg-accent-pink-strong transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              
                            {isLoading ? 'Saving...' : 'Save Changes'}
                        </button>
                    </div>
                </div>
            </Card>
        </div>);

};