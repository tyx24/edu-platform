import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/edu': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})