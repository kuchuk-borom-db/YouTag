# Build stage
FROM node:18-alpine AS build

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

WORKDIR /app

# Do not switch to a non-root user (default is root)

# Copy only necessary files from build stage
COPY package*.json ./

# Install only production dependencies
ENV NODE_ENV=production
RUN npm ci && npm cache clean --force

# Copy built application
COPY --from=build /app/dist ./dist

# Expose the port your app runs on
EXPOSE 3000

# Start the application
CMD ["npm", "run", "start:prod"]
