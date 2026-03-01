# Estágio de Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Estágio de Execução (Servidor Nginx para servir os estáticos)
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Configuração opcional do Nginx para lidar com React Router (SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
