import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 8080,
    proxy: {
      '/api': 'http://localhost:28080',
      '/sse': {
        target: 'http://localhost:28080',
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      treeshake: false,
    },
    minify: 'terser',
    terserOptions: {
      format: {
        comments: false,
      },
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
  },
  resolve: {
    alias: {
      'devextreme/ui': 'devextreme/esm/ui',
    },
  },
})
