/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1976d2',
          dark: '#1565c0',
          light: '#e3f2fd',
        },
        secondary: {
          DEFAULT: '#4caf50',
          dark: '#388e3c',
        },
        accent: {
          DEFAULT: '#ff9800',
          dark: '#f57c00',
        },
        danger: {
          DEFAULT: '#f44336',
          dark: '#d32f2f',
        },
      },
    },
  },
  plugins: [],
}
