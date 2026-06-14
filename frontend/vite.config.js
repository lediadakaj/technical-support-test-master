import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Production build is embedded into the Spring Boot jar by Maven.
// `npm run dev` starts a hot-reload dev server on :5173 that proxies
// API calls to the Spring Boot backend on :8080.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
