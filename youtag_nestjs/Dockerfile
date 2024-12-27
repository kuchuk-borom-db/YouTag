# Dockerfile
FROM node:18-alpine

WORKDIR /app

COPY . .

RUN npm install

# Expose application port
EXPOSE 3000

# Start the application
CMD ["npm", "run", "start"]



