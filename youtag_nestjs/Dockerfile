# Dockerfile
FROM node:18-alpine as build
WORKDIR /app

COPY package*.json .
RUN npm install
COPY . .
RUN npm run build --webpack

FROM node:18-alpine
WORKDIR /app
COPY package.json .
RUN npm install --only=production
COPY --from=build /app/dist ./dist
CMD npm run start:prod



