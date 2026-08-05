import React from 'react';
import { X } from 'lucide-react';








export const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-30 p-4 animate-in fade-in duration-200">
            <div className="bg-white rounded-lg w-full max-w-md shadow-lg border border-neutral-200">
                <div className="flex justify-between items-center p-6 border-b border-neutral-200">
                    <h2 className="text-xl font-medium text-neutral-900">{title}</h2>
                    <button onClick={onClose} className="text-neutral-400 hover:text-neutral-700 transition-colors">
                        <X className="w-5 h-5" />
                    </button>
                </div>
                <div className="p-6">
                    {children}
                </div>
            </div>
        </div>);

};