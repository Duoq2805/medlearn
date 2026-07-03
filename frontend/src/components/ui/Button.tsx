import React from 'react';
import { Loader2 } from 'lucide-react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  children: React.ReactNode;
  className?: string;
}

export default function Button({ 
  variant = 'primary', 
  size = 'md', 
  isLoading = false, 
  children, 
  className = '',
  ...props 
}: ButtonProps) {
  const variantClasses = {
    primary: 'btn-neumorphic-primary',
    secondary: 'btn-neumorphic-secondary',
    ghost: 'bg-white dark:bg-[var(--surface-primary)] shadow-neumorphic-inset-sm text-[var(--text-primary)] hover:shadow-neumorphic-inset rounded-2xl',
    danger: 'btn-neumorphic-primary',
  };

  const sizeClasses = {
    sm: 'py-2 px-4 text-sm min-h-[44px]',
    md: 'py-3 px-6 text-base min-h-[48px]',
    lg: 'py-4 px-8 text-lg min-h-[52px]',
  };

  return (
    <button
      className={`${variantClasses[variant]} ${sizeClasses[size]} ${className}`}
      disabled={isLoading || props.disabled}
      {...props}
    >
      {isLoading && <Loader2 size={18} className="animate-spin mr-2" />}
      {children}
    </button>
  );
}
