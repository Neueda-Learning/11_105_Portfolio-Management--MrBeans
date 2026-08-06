import type { Config } from 'tailwindcss'

export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        page: '#FFFDFB',
        card: {
          DEFAULT: '#FFFEFC',
          alt: '#F9F6F7'
        },
        accent: {
          pink: {
            DEFAULT: '#FFB3C6',
            strong: '#F48FB1'
          },
          yellow: '#FFE59A',
          blue: '#C9E4F6',
          plum: '#D9C4F0'
        },
        gain: {
          DEFAULT: '#8FE365',
          text: '#5C9A2E'
        },
        loss: {
          DEFAULT: '#F0645A',
          text: '#C23B32'
        },
        text: {
          heading: '#C2447B',
          body: '#A8637D',
          muted: '#8A7B85',
          onFill: '#FFFFFF'
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      spacing: {
        '18': '4.5rem',
      },
      borderRadius: {
        'sm': '0.125rem',
        'md': '0.25rem',
        'lg': '0.375rem',
      },
      boxShadow: {
        'sm': '0 1px 2px 0 rgb(0 0 0 / 0.05)',
        'md': '0 4px 6px -1px rgb(0 0 0 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.05)',
      }
    },
  },
  plugins: [],
} satisfies Config
