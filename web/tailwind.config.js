/** @type {import('tailwindcss').Config} */

module.exports = {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: 'var(--primary)',
        teal: 'var(--teal)',
        green: 'var(--grean)',
        yellow: 'var(--yellow)',
        'light-red': 'var(--light-red)',
        'medium-shade': 'var(--medium-shade)',
        'dark-pink': 'var(--dark-pink)',
        'dark-gray-bule': 'var(--dark-gray-bule)',
      },
      fontFamily: {
        regular: 'var(--font-regular)',
      },
      height: {
        'common-header-h': 'var(--common-header-height)',
        'output-items-item-tab-h': 'var(--output-items-tab-height)',
        'cds-toolbar-h': 'var(--cds-toolbar-height)',
        'sql-editor-output-items-h': 'var(--sql-editor-output-items-height)',
      },
    },
  },
  prefix: 'tw-',
  plugins: [],
}
