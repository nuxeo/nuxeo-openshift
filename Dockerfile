FROM nuxeo:8.10
USER root
RUN apt-get update && apt-get install -y vim dnsutils
USER 1000