# High-Performance, lightweight Nginx Alpine container (~25MB)
FROM nginx:alpine

# Copy all our HTML, CSS, and JS files to Nginx's default web directory
COPY . /usr/share/nginx/html

# Expose standard HTTP port
EXPOSE 80

# Start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]