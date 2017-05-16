FROM nuxeo:8.10

ENV NUXEO_TEMPLATES=default,mongodb
ENV NUXEO_CUSTOM_PARAM=nuxeo.mongodb.server=mongodb://admin:nuxeo@mongodb:27017
ENV NUXEO_PACKAGES=nuxeo-jsf-ui
ENV NUXEO_ES_HOSTS=elasticsearch.nuxeo.svc
ENV NUXEO_ES_CLUSTER_NAME: nuxeo_cluster
ENV NUXEO_ES_REPLICAS: 2
ENV REDIS_HOST=redis.nuxeo.svc