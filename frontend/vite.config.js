import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Keep the /api prefix when proxying so backend controllers mapped to '/api' work unchanged.
        // Previously the rewrite removed /api, causing Vite to request '/categories' on the backend
        // while the controller was mapped to '/api/categories' → 404. Use identity rewrite.
        rewrite: (path) => path
      }
    }
  }
})
