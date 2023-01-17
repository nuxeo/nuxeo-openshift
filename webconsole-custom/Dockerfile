FROM nginx:mainline

RUN sed -i.bak 's/listen\(.*\)80;/listen 8081;/' /etc/nginx/conf.d/default.conf && \
    sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf
RUN  touch /var/run/nginx.pid && \
     chown -R nginx:nginx /var/cache/nginx /var/run/nginx.pid
EXPOSE 8081
USER nginx

COPY www /usr/share/nginx/html
RUN chown -R nginx:nginx /usr/share/nginx/html

