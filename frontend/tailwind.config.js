/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // navy scale anchored on the top bar (#0f2747 == navy-900) so the darkest
        // text ties into the masthead
        navy: {
          50: '#f1f5f9',
          100: '#e2e8f0',
          200: '#c5d2e0',
          300: '#9db0c8',
          400: '#6a86a8',
          500: '#436288',
          600: '#2f4d72',
          700: '#213d5e',
          800: '#173151',
          900: '#0f2747',
          950: '#081a32',
        },
        // the brand orange, anchored on the logo's coral-orange (#ee543c == coral-500).
        // Replaces amber everywhere so every "orange" on the site matches the logo.
        coral: {
          50: '#fdf1ee',
          100: '#fbded7',
          200: '#f7bfb2',
          300: '#f2988a', // was amber-300/400 range
          400: '#f16f56',
          500: '#ee543c',
          600: '#d83f28',
          700: '#b4311d', // was amber-600/700 range
          800: '#8f2818',
          900: '#762418',
        },
      },
      fontFamily: {
        // Quicksand = the default app font (Tailwind applies `sans` to <html>)
        sans: ['"Quicksand Variable"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        // Defined but intentionally NOT used anywhere — apply manually with
        // `font-caveat` / `font-shadows` where you want them.
        caveat: ['"Caveat Variable"', 'cursive'],
        shadows: ['"Shadows Into Light"', 'cursive'],
      },
    },
  },
  plugins: [],
};
