# Build stage
FROM node:18-alpine as build

# Add necessary build tools for native dependencies
RUN apk add --no-cache python3 make g++

WORKDIR /app

# Copy package files first to leverage Docker cache
COPY package*.json ./

# Install all dependencies including devDependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM node:18-alpine

# Add a non-root user
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nestjs -u 1001

WORKDIR /app

# Copy only necessary files from build stage
COPY --chown=nestjs:nodejs package*.json ./

# Install only production dependencies
RUN npm ci --only=production && \
    npm cache clean --force

# Copy built application
COPY --chown=nestjs:nodejs --from=build /app/dist ./dist

# Switch to non-root user
USER nestjs

# Expose the port your app runs on
EXPOSE 3000

# Start the application
CMD ["npm", "run", "start:prod"]