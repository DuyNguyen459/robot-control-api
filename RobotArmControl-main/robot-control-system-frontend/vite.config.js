import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'https://robot-control-system-rmbw.onrender.com',
        changeOrigin: true,
        secure: true,
      },
      '/ws': {
        target: 'wss://robot-control-system-rmbw.onrender.com',
        ws: true,
        changeOrigin: true,
      },
    },
  },
})