import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development, Vite trimite tot ce incepe cu /api catre Spring Boot,
// asa browserul vede un singur "site" si nu avem nevoie de CORS.
const backend = process.env.API_TARGET || 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': backend,
    },
  },
});
