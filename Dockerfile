# Multi-stage build: Node.js + Python runtime
FROM node:22-alpine AS base

# Install Python and required system dependencies
RUN apk add --no-cache python3 py3-pip make g++ openssl-dev libffi-dev

# Set working directory
WORKDIR /app

# Copy package files
COPY package.json pnpm-lock.yaml ./

# Install Node dependencies
RUN npm install -g pnpm && pnpm install --frozen-lockfile

# Copy Python requirements and install
COPY requirements.txt ./
RUN pip3 install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Build the TypeScript
RUN pnpm build

# Expose port
EXPOSE 3000

# Start the application
CMD ["node", "dist/index.js"]
