import React, { useState } from 'react';
import { Send } from 'lucide-react';






export const ChatInput = ({ onSend, disabled }) => {
  const [text, setText] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (text.trim() && !disabled) {
      onSend(text.trim());
      setText('');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex items-center p-3 border-t border-[#FFE6EE] bg-card">
            <input
        type="text"
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="Ask something..."
        disabled={disabled}
        className="flex-1 bg-page px-3 py-2 rounded-full text-sm focus:outline-none focus:ring-1 focus:ring-accent-pink border border-transparent focus:border-accent-pink disabled:opacity-50 transition-all" />
      
            <button
        type="submit"
        disabled={!text.trim() || disabled}
        className={`ml-2 p-2 rounded-full transition-colors ${
        text.trim() && !disabled ?
        'bg-accent-pink text-white hover:bg-accent-pink-strong' :
        'bg-card-alt text-text-muted cursor-not-allowed'}`
        }>
        
                <Send className="w-4 h-4" />
            </button>
        </form>);

};