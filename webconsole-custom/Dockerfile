FROM nginx:mainline

RUN sed -i.bak 's/listen\(.*\)80;/listen 8081;/' /etc/nginx/conf.d/default.conf && \
    sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf
RUN  touch /var/run/nginx.pid && \
     chown -R root /var/cache/nginx /var/run/nginx.pid
RUN  chmod -R 770 /var/cache/nginx /var/run /var/log/nginx
EXPOSE 8081

COPY www /usr/share/nginx/html

