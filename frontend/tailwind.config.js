/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        matcha: {
          50: '#f9fef7',
          100: '#f0fce8',
          200: '#e1f9d1',
          300: '#dce8d5',
          400: '#c8dab3',
          500: '#b4cc91',
          600: '#a0be6f',
          700: '#8cb04d',
          800: '#78a22b',
          900: '#649409',
          950: '#4a6a05',
        },
        coal: {
          50: '#f8f9f9',
          100: '#eff1f2',
          200: '#d3d8db',
          300: '#b7bfc4',
          400: '#7a8893',
          500: '#565f65',
          600: '#3d4650',
          700: '#2f3437',
          800: '#252b2f',
          900: '#1b2127',
          950: '#0f1214',
        },
        primary: {
          50: '#f9fef7',
          100: '#dce8d5',
          200: '#c8dab3',
          300: '#b4cc91',
          400: '#a0be6f',
          500: '#8cb04d',
          600: '#78a22b',
          700: '#649409',
          800: '#4a6a05',
          900: '#2f3437',
        },
        accent: {
          50: '#f9fef7',
          100: '#eff1f2',
          200: '#d3d8db',
          300: '#b7bfc4',
          400: '#8cb04d',
          500: '#2f3437',
          600: '#1b2127',
          700: '#0f1214',
          800: '#2f3437',
          900: '#1b2127',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-in-out',
        'slide-up': 'slideUp 0.5s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'pulse-soft': 'pulseSoft 2s infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        pulseSoft: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.7' },
        },
      },
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
}
