import React, { useState, useRef, useEffect } from 'react';
import { MessageCircle, X } from 'lucide-react';
import { useChatbot } from './hooks/useChatbot';
import { ChatMessage } from './ChatMessage';
import { ChatInput } from './ChatInput';

export const ChatbotWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { messages, isLoading, sendMessage } = useChatbot();
  const messagesEndRef = useRef(null);

  // Auto-scroll to bottom when messages change
  useEffect(() => {
    if (isOpen && messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isOpen]);

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end">
            {/* The Chat Window */}
            {isOpen &&
      <div className="w-80 h-96 bg-card rounded-xl shadow-xl flex flex-col mb-4 overflow-hidden animate-in slide-in-from-bottom-5 fade-in duration-200">
                    {/* Header */}
                    <div className="bg-accent-pink text-text-onFill p-4 flex justify-between items-center">
                        <div className="flex items-center">
                            <MessageCircle className="w-5 h-5 mr-2" />
                            <h3 className="font-medium text-sm">Finora Assistant</h3>
                        </div>
                        <button
            onClick={() => setIsOpen(false)}
            className="text-white/80 hover:text-white transition-colors">
            
                            <X className="w-4 h-4" />
                        </button>
                    </div>

                    {/* Messages Area */}
                    <div className="flex-1 overflow-y-auto p-4 bg-page">
                        {messages.map((msg) =>
          <ChatMessage key={msg.id} message={msg} />
          )}
                        {isLoading &&
          <div className="flex justify-start mb-4">
                                <div className="bg-card-alt text-text-muted px-4 py-2 rounded-2xl rounded-bl-sm text-[15px] flex space-x-1">
                                    <span className="animate-bounce">.</span>
                                    <span className="animate-bounce delay-100">.</span>
                                    <span className="animate-bounce delay-200">.</span>
                                </div>
                            </div>
          }
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input Area */}
                    <ChatInput onSend={sendMessage} disabled={isLoading} />
                </div>
      }

            {/* Floating Action Button */}
            <button
        onClick={() => setIsOpen(!isOpen)}
        className={`w-14 h-14 rounded-full flex items-center justify-center transition-transform hover:scale-105 active:scale-95 ${
        isOpen ? 'bg-accent-pink-strong text-text-onFill' : 'bg-accent-pink text-text-onFill'}`
        }
        aria-label="Toggle Chat">
        
                {isOpen ? <X className="w-6 h-6" /> : <MessageCircle className="w-6 h-6" />}
            </button>
        </div>);

};