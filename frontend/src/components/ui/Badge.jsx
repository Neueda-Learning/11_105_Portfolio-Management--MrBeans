import React from 'react';









export const Badge = ({ children, variant = 'neutral', className = '' }) => {
  const variants = {
    neutral: 'bg-card-alt text-text-muted',
    blue: 'bg-accent-blue text-[#2E6F99]',
    orange: 'bg-accent-plum text-[#7A4F99]',
    green: 'bg-[#EAFBDD] text-gain-text',
    red: 'bg-[#FFEAF1] text-loss-text'
  };

  return (
    <span className={`px-2.5 py-0.5 inline-flex text-[13px] leading-5 font-semibold rounded-full ${variants[variant]} ${className}`}>
            {children}
        </span>);

};