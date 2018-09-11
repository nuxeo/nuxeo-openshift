FROM nginx

RUN chmod g+rwx /var/cache/nginx /var/run /var/log/nginx && \
    sed -i.bak 's/listen\(.*\)80;/listen 8081;/' /etc/nginx/conf.d/default.conf && \
    sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf
EXPOSE 8081
COPY www /usr/share/nginx/html
