import React from 'react';






export const Card = ({ children, className = '' }) => {
  return (
    <div className={`bg-card rounded-xl overflow-hidden ${className}`}>
            {children}
        </div>);

};