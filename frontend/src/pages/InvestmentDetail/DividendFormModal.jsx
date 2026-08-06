import React, { useState } from 'react';
import { DividendMode } from '../../types/enums';
import { dividendsApi } from '../../api/dividends';
import { Modal } from '../../components/ui/Modal';

const inputClass =
  'w-full px-3 py-2 border border-[#FFE6EE] rounded-md focus:outline-none focus:ring-1 focus:ring-accent-pink focus:border-accent-pink';

export const DividendFormModal = ({ isOpen, investmentId, investmentCurrency, onClose, onSubmit }) => {
  const [amount, setAmount] = useState('');
  const [dividendPerShare, setDividendPerShare] = useState('');
  const [currency, setCurrency] = useState(investmentCurrency || 'USD');
  const [withholdingTax, setWithholdingTax] = useState('0');
  const [mode, setMode] = useState(DividendMode.DISTRIBUTIVE);
  const [reinvestmentPrice, setReinvestmentPrice] = useState('');
  const [exDate, setExDate] = useState('');
  const [paymentDate, setPaymentDate] = useState('');

  // Simulate
  const [simulation, setSimulation] = useState(null);
  const [isSimulating, setIsSimulating] = useState(false);
  const [simulateError, setSimulateError] = useState('');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  if (!isOpen) return null;

  const resetForm = () => {
    setAmount(''); setDividendPerShare(''); setCurrency(investmentCurrency || 'USD');
    setWithholdingTax('0'); setMode(DividendMode.DISTRIBUTIVE); setReinvestmentPrice('');
    setExDate(''); setPaymentDate(''); setSimulation(null); setSimulateError(''); setErrorMsg('');
  };

  const handleSimulate = async () => {
    const dps = parseFloat(dividendPerShare);
    if (isNaN(dps) || dps <= 0) { setSimulateError('Enter dividend per share > 0 to simulate.'); return; }
    if (mode === DividendMode.ACCUMULATIVE) {
      const rp = parseFloat(reinvestmentPrice);
      if (isNaN(rp) || rp <= 0) { setSimulateError('Enter reinvestment price > 0 for accumulative mode.'); return; }
    }
    setIsSimulating(true); setSimulateError(''); setSimulation(null);
    try {
      const payload = { dividendPerShare: dps, mode };
      if (mode === DividendMode.ACCUMULATIVE) payload.reinvestmentPrice = parseFloat(reinvestmentPrice);
      setSimulation(await dividendsApi.simulate(investmentId, payload));
    } catch (err) {
      setSimulateError(err.message || 'Simulation failed.');
    } finally {
      setIsSimulating(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const amountNum = parseFloat(amount);
    if (isNaN(amountNum) || amountNum <= 0) { setErrorMsg('Enter a valid amount > 0.'); return; }
    if (!paymentDate) { setErrorMsg('Payment date is required.'); return; }
    if (mode === DividendMode.ACCUMULATIVE) {
      const rp = parseFloat(reinvestmentPrice);
      if (isNaN(rp) || rp <= 0) { setErrorMsg('Reinvestment price is required for Accumulative mode.'); return; }
    }
    setIsSubmitting(true); setErrorMsg('');
    const req = {
      amount: amountNum,
      dividendPerShare: dividendPerShare ? parseFloat(dividendPerShare) : undefined,
      currency: currency.trim().toUpperCase(),
      withholdingTax: parseFloat(withholdingTax) || 0,
      reinvestmentPrice: reinvestmentPrice ? parseFloat(reinvestmentPrice) : undefined,
      mode,
      exDate: exDate || null,
      paymentDate,
    };
    const result = await onSubmit(req);
    setIsSubmitting(false);
    if (result.success) { resetForm(); onClose(); }
    else setErrorMsg(result.error?.message || 'Failed to record dividend.');
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Record Dividend">
      <form onSubmit={handleSubmit} className="space-y-4">
        {errorMsg && <div className="p-3 text-sm text-loss bg-[#FFEAF1] rounded">{errorMsg}</div>}

        {/* Mode */}
        <div>
          <label className="block text-sm font-medium text-text-body mb-1">Dividend Mode</label>
          <select value={mode} onChange={(e) => { setMode(e.target.value); setSimulation(null); }} className={inputClass}>
            <option value={DividendMode.DISTRIBUTIVE}>Distributive (cash payout)</option>
            <option value={DividendMode.ACCUMULATIVE}>Accumulative (reinvested)</option>
          </select>
        </div>

        {/* Amount + Dividend per Share */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Total Gross Amount</label>
            <input required type="number" step="0.0001" min="0.0001" value={amount}
              onChange={(e) => setAmount(e.target.value)} placeholder="e.g. 250.00" className={inputClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Dividend per Share <span className="text-text-muted">(opt.)</span></label>
            <input type="number" step="0.00000001" min="0" value={dividendPerShare}
              onChange={(e) => { setDividendPerShare(e.target.value); setSimulation(null); }}
              placeholder="e.g. 0.50" className={inputClass} />
          </div>
        </div>

        {/* Currency + Withholding Tax */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Currency</label>
            <input required type="text" maxLength={10} value={currency}
              onChange={(e) => setCurrency(e.target.value)} placeholder="USD" className={inputClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Withholding Tax</label>
            <input type="number" step="0.01" min="0" value={withholdingTax}
              onChange={(e) => setWithholdingTax(e.target.value)} placeholder="0.00" className={inputClass} />
          </div>
        </div>

        {/* Reinvestment Price â€” only for ACCUMULATIVE */}
        {mode === DividendMode.ACCUMULATIVE && (
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">
              Reinvestment Price <span className="text-xs text-accent-pink">(required â€” auto-creates BUY)</span>
            </label>
            <input required type="number" step="0.01" min="0.01" value={reinvestmentPrice}
              onChange={(e) => { setReinvestmentPrice(e.target.value); setSimulation(null); }}
              placeholder="e.g. 120.00" className={inputClass} />
          </div>
        )}

        {/* Dates */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Ex-Date <span className="text-text-muted">(opt.)</span></label>
            <input type="date" value={exDate} onChange={(e) => setExDate(e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-text-body mb-1">Payment Date</label>
            <input required type="date" value={paymentDate} onChange={(e) => setPaymentDate(e.target.value)} className={inputClass} />
          </div>
        </div>

        {/* Simulate section */}
        <div className="border border-[#FFE6EE] rounded-md p-4 space-y-3 bg-page">
          <p className="text-xs font-semibold text-text-muted uppercase tracking-wider">Simulate payout (optional preview)</p>
          {simulateError && <p className="text-xs text-loss">{simulateError}</p>}
          <button type="button" onClick={handleSimulate} disabled={isSimulating}
            className="px-4 py-2 text-sm font-semibold text-white bg-accent-pink rounded-md hover:bg-accent-pink-strong disabled:opacity-50">
            {isSimulating ? 'Simulating...' : 'Simulate'}
          </button>
          {simulation && (
            <div className="grid grid-cols-2 gap-2 text-sm">
              <div className="bg-card rounded p-2"><p className="text-text-muted text-xs">Total Shares</p><p className="font-semibold text-text-heading">{Number(simulation.totalShares).toLocaleString()}</p></div>
              <div className="bg-card rounded p-2"><p className="text-text-muted text-xs">Gross Dividend</p><p className="font-semibold text-text-heading">{Number(simulation.totalDividendAmount).toLocaleString(undefined, { minimumFractionDigits: 2 })}</p></div>
              {mode === DividendMode.ACCUMULATIVE
                ? <div className="bg-card rounded p-2 col-span-2"><p className="text-text-muted text-xs">New Shares Acquired</p><p className="font-semibold text-text-heading">{Number(simulation.newSharesAcquired).toLocaleString(undefined, { maximumFractionDigits: 8 })}</p></div>
                : <div className="bg-card rounded p-2 col-span-2"><p className="text-text-muted text-xs">Cash Payout</p><p className="font-semibold text-text-heading">{Number(simulation.cashPayout).toLocaleString(undefined, { minimumFractionDigits: 2 })}</p></div>}
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="pt-2 flex justify-end space-x-3">
          <button type="button" onClick={() => { resetForm(); onClose(); }}
            className="px-5 py-3 text-[15px] font-semibold text-text-body bg-white border border-[#FFE6EE] rounded-md hover:bg-page">
            Cancel
          </button>
          <button type="submit" disabled={isSubmitting}
            className="px-5 py-3 text-[15px] font-semibold text-white bg-accent-pink rounded-md hover:bg-accent-pink-strong disabled:opacity-50">
            {isSubmitting ? 'Saving...' : 'Record Dividend'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
