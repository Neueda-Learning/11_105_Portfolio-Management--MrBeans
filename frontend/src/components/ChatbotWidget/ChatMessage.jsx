import React from 'react';






export const ChatMessage = ({ message }) => {
  const isUser = message.role === 'user';

  return (
    <div className={`flex w-full mb-4 ${isUser ? 'justify-end' : 'justify-start'}`}>
            <div
        className={`max-w-[80%] px-4 py-2 rounded-2xl text-sm ${
        isUser ?
        'bg-card-alt text-text-body rounded-br-sm' :
        'bg-accent-blue text-[#2E6F99] rounded-bl-sm'}`
        }>
        
                {message.content}
            </div>
        </div>);

};