import React from 'react';

interface MedvoraLogoProps {
  className?: string;
  variant?: 'icon' | 'full';
  showDepth?: boolean;
}

/**
 * MedvoraLogo - Neumorphic SVG Logo
 * Features:
 * - Neumorphic dual-shadow styling adapted to matcha/coal palette
 * - Two variants: icon (symbol only) and full (symbol + text)
 * - Theme-aware with CSS custom properties
 * - Tactile depth with shadow play
 */
export const MedvoraLogo: React.FC<MedvoraLogoProps> = ({
  className = 'h-10 w-10',
  variant = 'icon',
  showDepth = true,
}) => {
  const iconSVG = (
    <svg
      viewBox="0 0 64 64"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`${className} transition-transform duration-300`}
    >
      <defs>
        <filter id="neomorphic-shadow" x="-50%" y="-50%" width="200%" height="200%">
          {showDepth && (
            <>
              <feGaussianBlur in="SourceAlpha" stdDeviation="2" />
              <feOffset dx="3" dy="3" result="offsetblur" />
              <feComponentTransfer>
                <feFuncA type="linear" slope="0.3" />
              </feComponentTransfer>
              <feMerge>
                <feMergeNode />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </>
          )}
        </filter>
        <linearGradient id="matcha-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#a8c66a" />
          <stop offset="100%" stopColor="#8cb04d" />
        </linearGradient>
      </defs>

      <g filter={showDepth ? 'url(#neomorphic-shadow)' : 'none'}>
        {/* Outer circle - extruded */}
        <circle
          cx="32"
          cy="32"
          r="28"
          fill="url(#matcha-gradient)"
          opacity="0.95"
          className="transition-all duration-300"
        />

        {/* Inner circle - inset effect */}
        <circle
          cx="32"
          cy="32"
          r="20"
          fill="none"
          stroke="currentColor"
          strokeWidth="0.5"
          opacity="0.2"
        />

        {/* Center glyph - M symbol */}
        <g transform="translate(32, 32)">
          {/* M shape - medical/learning symbol */}
          <path
            d="M -8 -6 L -2 6 L 2 6 L 8 -6 M -2 2 L 2 2"
            stroke="white"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
            className="transition-all duration-300"
          />
        </g>

        {/* Top accent - light source indicator */}
        <circle
          cx="20"
          cy="20"
          r="3"
          fill="white"
          opacity="0.4"
          className="transition-opacity duration-300"
        />
      </g>
    </svg>
  );

  if (variant === 'full') {
    return (
      <div className="flex items-center gap-2.5 group">
        <div className="flex items-center justify-center">
          {iconSVG}
        </div>
        <span className="font-display font-bold text-lg hidden md:inline bg-gradient-to-r from-[#2F3437] to-[#565f65] dark:from-[#8cb04d] dark:to-[#a8c66a] bg-clip-text text-transparent transition-colors duration-300">
          Medvora
        </span>
      </div>
    );
  }

  return iconSVG;
};

export default MedvoraLogo;
