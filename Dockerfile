FROM nuxeo:8.10

ENV NUXEO_TEMPLATES=default,mongodb
ENV NUXEO_CUSTOM_PARAM=nuxeo.mongodb.server=mongodb://mongo-0.mongo.nuxeo.svc:27017,mongo-1.mongo.nuxeo.svc:27017,mongo-2.mongo.nuxeo.svc:27017
ENV NUXEO_PACKAGES="nuxeo-jsf-ui nuxeo-web-ui"
ENV NUXEO_ES_HOSTS=elasticsearch-discovery.nuxeo.svc:9300
ENV NUXEO_ES_CLUSTER_NAME: elasticsearch
ENV NUXEO_ES_REPLICAS: 2
ENV NUXEO_REDIS_HOST=redis-0.redis.nuxeo.svc