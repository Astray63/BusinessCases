### Étape 1 : construire l'application Angular
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
COPY . .
RUN npm run build -- --configuration production

### Étape 2 : servir avec nginx
FROM nginx:alpine
COPY --from=build /app/dist/frontend-stable /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"] 