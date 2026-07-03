import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  variant?: 'standard' | 'sm' | 'elevated' | 'inset';
  interactive?: boolean;
}

export default function Card({ 
  children, 
  className = '', 
  variant = 'standard',
  interactive = false 
}: CardProps) {
  const variants = {
    standard: 'card-neumorphic',
    sm: 'card-neumorphic-sm',
    elevated: 'card-neumorphic shadow-neumorphic-ext-hover',
    inset: 'card-neumorphic-inset',
  };

  const interactiveClass = interactive ? 'cursor-pointer hover:-translate-y-1' : '';

  return (
    <div className={`${variants[variant]} ${interactiveClass} ${className}`}>
      {children}
    </div>
  );
}
