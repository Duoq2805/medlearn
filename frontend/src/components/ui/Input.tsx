import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

export default function Input({ className, error, ...props }: InputProps) {
  const baseClasses = 'input-neumorphic';
  const errorClasses = error ? 'border-red-500 focus:shadow-red-500/30' : '';

  return (
    <input
      className={`${baseClasses} ${errorClasses} ${className}`}
      {...props}
    />
  );
}
