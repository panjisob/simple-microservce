# Gunakan image Node untuk build
FROM node:20 AS builder

# Set working directory
WORKDIR /app

# Copy package.json dan package-lock.json
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy semua kode proyek
COPY . .

# Build aplikasi
RUN npm run build


# === Stage 2: Serve dengan Nginx ===
FROM nginx:1.25 AS runner

# Hapus konfigurasi default Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copy file konfigurasi Nginx ke dalam container
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy hasil build ke folder yang akan dilayani oleh Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80 untuk akses aplikasi
EXPOSE 80

# Jalankan Nginx
CMD ["nginx", "-g", "daemon off;"]
