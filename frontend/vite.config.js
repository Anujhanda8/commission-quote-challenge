import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    // Forward API calls to the Spring Boot backend, so the browser only ever talks to one origin.
    proxy: {
      '/api': process.env.API_PROXY_TARGET ?? 'http://localhost:8080',
    },
    // File change events don't always reach a container from a mounted macOS folder, so dev mode polls instead.
    watch: {
      usePolling: process.env.VITE_USE_POLLING === 'true',
    },
  },
  test: {
    environment: 'jsdom',
    // Test files run in parallel, each in its own isolated environment.
    fileParallelism: true,
  },
})
