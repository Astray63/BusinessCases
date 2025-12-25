### Stage 1: build the Angular app
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
COPY . .
RUN npm run build -- --configuration production

### Stage 2: serve with nginx
FROM nginx:alpine
COPY --from=build /app/dist/frontend-stable /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"] 