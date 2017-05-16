FROM nuxeo:8.10

ENV NUXEO_TEMPLATES=default,mongodb
ENV NUXEO_CUSTOM_PARAM=nuxeo.mongodb.server=mongodb://mongoO.nuxeo.svc:27017,mongo1.nuxeo.svc:27017,mongo2.nuxeo.svc:27017
ENV NUXEO_PACKAGES=nuxeo-jsf-ui
ENV NUXEO_ES_HOSTS=elasticsearch-discovery.nuxeo.svc:9300
ENV NUXEO_ES_CLUSTER_NAME: myesdb
ENV NUXEO_ES_REPLICAS: 2
ENV NUXEO_REDIS_HOST=redis.nuxeo.svc