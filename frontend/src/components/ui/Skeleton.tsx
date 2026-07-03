import React from 'react';

interface SkeletonProps {
  className?: string;
}

export default function Skeleton({ className = '' }: SkeletonProps) {
  return (
    <div
      className={`animate-pulse rounded-xl bg-gray-300/30 dark:bg-gray-700/30 ${className}`}
    ></div>
  );
}
