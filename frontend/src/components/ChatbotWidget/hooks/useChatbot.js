import { useState, useCallback } from 'react';
import { chatApi } from '../../../api/chat';







export const useChatbot = () => {
  const [messages, setMessages] = useState([
  { id: '1', role: 'assistant', content: 'Hello! I am your portfolio assistant. How can I help you today?' }]
  );
  const [isLoading, setIsLoading] = useState(false);

  const sendMessage = useCallback(async (text) => {
    if (!text.trim()) return;

    const userMsg = {
      id: Date.now().toString(),
      role: 'user',
      content: text
    };

    setMessages((prev) => [...prev, userMsg]);
    setIsLoading(true);

    try {
      const response = await chatApi.sendMessage({ message: text });

      const botMsg = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: response.reply
      };

      setMessages((prev) => [...prev, botMsg]);
    } catch (error) {
      const errorMsg = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: 'Sorry, I encountered an error communicating with the server.'
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  return { messages, isLoading, sendMessage };
};