#!/bin/bash

NUXEO_INIT_IMAGE_NAME="nuxeo-init"

setUp() {
    rm -rf target
    mkdir target
    cp -r test/{elasticsearch,mongodb,kafka} ./target
    mkdir -p target/conf.d
}


# Runs the init container with the minimum mounted volume
runInit() {
  docker run --rm \
       -e NUXEO_DATA=/var/lib/nuxeo/data \
       -v $(pwd)/target/data:/var/lib/nuxeo/data \
       -v $(pwd)/target/conf.d:/etc/nuxeo/conf.d \
       $* \
    ${NUXEO_INIT_IMAGE_NAME}
}


testGenerateConfFiles() {
    runInit \
       -v $(pwd)/target/elasticsearch:/opt/nuxeo/bindings/elasticsearch \
       -v $(pwd)/target/mongodb:/opt/nuxeo/bindings/mongodb \
       -v $(pwd)/target/kafka:/opt/nuxeo/bindings/kafka


    assertTrue "Elastic configuration is found" '[[ -f "target/conf.d/20-elasticsearch.conf" ]]'
    assertTrue "Elastic configuration is correct" 'diff test/expected/20-elasticsearch.conf target/conf.d/20-elasticsearch.conf'
    assertTrue "MongoDB configuration is found" '[[ -f "target/conf.d/20-mongodb.conf" ]]'
    assertTrue "MongoDB configuration is correct" 'diff test/expected/20-mongodb.conf target/conf.d/20-mongodb.conf'
    assertTrue "Kafka configuration is found" '[[ -f "target/conf.d/20-kafka.conf" ]]'
    assertTrue "Kafka configuration is correct" 'diff test/expected/20-kafka.conf target/conf.d/20-kafka.conf'
}

testGenerateConfFilesWithSSL() {
    for i in mongodb kafka elasticsearch; do cp test/tls_cacert ./target/$i/tls_cacert; done

    runInit \
       -v $(pwd)/target/elasticsearch:/opt/nuxeo/bindings/elasticsearch \
       -v $(pwd)/target/mongodb:/opt/nuxeo/bindings/mongodb \
       -v $(pwd)/target/kafka:/opt/nuxeo/bindings/kafka


    assertTrue "Elastic configuration is found" '[[ -f "target/conf.d/20-elasticsearch.conf" ]]'
    assertTrue "Elastic configuration is correct" 'diff test/expected/20-elasticsearch-ssl.conf target/conf.d/20-elasticsearch.conf'
    assertTrue "MongoDB configuration is found" '[[ -f "target/conf.d/20-mongodb.conf" ]]'
    assertTrue "MongoDB configuration is correct" 'diff test/expected/20-mongodb-ssl.conf target/conf.d/20-mongodb.conf'
    assertTrue "Kafka configuration is found" '[[ -f "target/conf.d/20-kafka.conf" ]]'
    assertTrue "Kafka configuration is correct" 'diff test/expected/20-kafka-ssl.conf target/conf.d/20-kafka.conf'

    assertTrue "Certificate are copied in Nuxeo Data" '[[ -f "target/data/cacerts" ]]'
}


testGenerateConfESOnly() {
    runInit -v $(pwd)/target/elasticsearch:/opt/nuxeo/bindings/elasticsearch


    assertTrue "Elastic configuration is found" '[[ -f "target/conf.d/20-elasticsearch.conf" ]]'
    assertFalse "MongoDB configuration is found" '[[ -f "target/conf.d/20-mongodb.conf" ]]'
    assertFalse "Kafka configuration is found" '[[ -f "target/conf.d/20-kafka.conf" ]]'
}

testGenerateConfMongoOnly() {
    runInit -v $(pwd)/target/mongodb:/opt/nuxeo/bindings/mongodb


    assertFalse "Elastic configuration is found" '[[ -f "target/conf.d/20-elasticsearch.conf" ]]'
    assertTrue "MongoDB configuration is found" '[[ -f "target/conf.d/20-mongodb.conf" ]]'
    assertFalse "Kafka configuration is found" '[[ -f "target/conf.d/20-kafka.conf" ]]'
}


testGenerateConfKafkaOnly() {
    runInit -v $(pwd)/target/kafka:/opt/nuxeo/bindings/kafka


    assertFalse "Elastic configuration is found" '[[ -f "target/conf.d/20-elasticsearch.conf" ]]'
    assertFalse "MongoDB configuration is found" '[[ -f "target/conf.d/20-mongodb.conf" ]]'
    assertTrue "Kafka configuration is found" '[[ -f "target/conf.d/20-kafka.conf" ]]'
}


testGenerateUserAndPasswordForES() {
    echo "user" > target/elasticsearch/username
    echo "p@ssw0rd" > target/elasticsearch/password

    runInit -v $(pwd)/target/elasticsearch:/opt/nuxeo/bindings/elasticsearch


    assertTrue "Elastic configuration is found" '[[ -f "target/conf.d/20-elasticsearch.conf" ]]'
    conf="$(cat target/conf.d/20-elasticsearch.conf)"
    assertContains "Elastic configuration contains username"  "$conf" "elasticsearch.restClient.username"
    assertContains "Elastic configuration contains username"  "$conf" "elasticsearch.restClient.password"


}
