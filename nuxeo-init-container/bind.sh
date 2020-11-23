#!/bin/bash

BINDINGS_DIR=${BINDINGS_DIR:-/opt/nuxeo/bindings}
CONFD_DIR=${CONFD_DIR:-/etc/nuxeo/conf.d}


NUXEO_ENV_NAME=${NUXEO_ENV_NAME:-nuxeo}
TRUSTSTORE_PATH=$NUXEO_DATA/cacerts


importCert() {
  local tlsFile=${1}
  local aliasName=${2}


  # Remove cert if already set.
    if [ -f $TRUSTSTORE_PATH ]; then
      set +e
      keytool -list -keystore $TRUSTSTORE_PATH -alias ${aliasName} -storepass changeit -noprompt > /dev/null 2>&1
      if [ "$?" == "0" ]; then
        keytool -delete -keystore $TRUSTSTORE_PATH -alias ${aliasName} -storepass changeit -noprompt
      fi
      set -e
    fi

    base64 -d ${tlsFile} > /tmp/cert
    keytool -import -file /tmp/cert -alias ${aliasName} -keystore $TRUSTSTORE_PATH -storepass changeit -noprompt
}



# Configure MongoDB bindings
if [ -f $BINDINGS_DIR/mongodb/uri ]; then
  echo "--> Generating bindings for MongoDB"

  MONGODB_URI=$(< $BINDINGS_DIR/mongodb/uri)
  MONGODB_DBNAME=$(< $BINDINGS_DIR/mongodb/dbname)
  cat > $CONFD_DIR/20-mongodb.conf <<EOT
nuxeo.mongodb.server=${MONGODB_URI}
nuxeo.mongodb.dbname=${MONGODB_DBNAME}
EOT

  if [ -f $BINDINGS_DIR/mongodb/tls_cacert ]; then

    # Temporary hack: Nuxeo launcher doesnt allow to specify Truststore https://jira.nuxeo.com/browse/NXP-25095
    # So we have to deactivate the mongodb check that require a specific truststore in SSL
    sed -i /mongodb.check/d /opt/nuxeo/server/templates/mongodb/nuxeo.defaults

    importCert $BINDINGS_DIR/mongodb/tls_cacert MongoDBCaCert

    cat >> $CONFD_DIR/20-mongodb.conf <<EOT
nuxeo.mongodb.ssl=true
nuxeo.mongodb.truststore.path=${TRUSTSTORE_PATH}
nuxeo.mongodb.truststore.password=changeit
nuxeo.mongodb.truststore.type=jks
EOT

  fi

fi


# Configure Elasticsearch bindings
if [ -d $BINDINGS_DIR/elasticsearch ]; then
  echo "--> Generating bindings for Elasticsearch"
  ELASTICSEARCH_CLUSTERNAME=$(< $BINDINGS_DIR/elasticsearch/clustername)
  ELASTICSEARCH_URI=$(< $BINDINGS_DIR/elasticsearch/uri)

  cat > $CONFD_DIR/20-elasticsearch.conf <<EOT
elasticsearch.addressList=${ELASTICSEARCH_URI}
elasticsearch.client=RestClient
elasticsearch.httpReadOnly.baseUrl=${ELASTICSEARCH_URI}
elasticsearch.clusterName=${ELASTICSEARCH_CLUSTERNAME}
elasticsearch.indexName=${NUXEO_ENV_NAME}
EOT

  if [ -f $BINDINGS_DIR/elasticsearch/username ]; then
      ELASTICSEARCH_USERNAME=$(< $BINDINGS_DIR/elasticsearch/username)
      ELASTICSEARCH_PASSWORD=$(< $BINDINGS_DIR/elasticsearch/password)
      cat >> $CONFD_DIR/20-elasticsearch.conf <<EOT
elasticsearch.restClient.username=${ELASTICSEARCH_USERNAME}
elasticsearch.restClient.password=${ELASTICSEARCH_PASSWORD}
EOT
  fi


  if [ -f $BINDINGS_DIR/elasticsearch/tls_cacert ]; then
    importCert $BINDINGS_DIR/elasticsearch/tls_cacert ElasticsearchCaCert

    cat >> $CONFD_DIR/20-elasticsearch.conf <<EOT
elasticsearch.restClient.truststore.path=${TRUSTSTORE_PATH}
elasticsearch.restClient.truststore.password=changeit
elasticsearch.restClient.truststore.type=jks
EOT

  fi

fi


# Configure Kafka bindings
if [ -d $BINDINGS_DIR/kafka ]; then
  echo "--> Generating bindings for Kafka"
  KAFKA_URI=$(< $BINDINGS_DIR/kafka/uri)

  cat > $CONFD_DIR/20-kafka.conf <<EOT
kafka.enabled=true
nuxeo.stream.work.enabled=true
kafka.bootstrap.servers=${KAFKA_URI}
kafka.topicPrefix=${NUXEO_ENV_NAME}-
kafka.offsets.retention.minutes=20160
nuxeo.pubsub.provider=stream
EOT


  if [ -f $BINDINGS_DIR/kafka/tls_cacert ]; then
    importCert $BINDINGS_DIR/kafka/tls_cacert KafkaCaCert

    cat >> $CONFD_DIR/20-kafka.conf <<EOT
kafka.ssl=true
kafka.truststore.path=${TRUSTSTORE_PATH}
kafka.truststore.password=changeit
kafka.truststore.type=jks
EOT

  fi

fi

