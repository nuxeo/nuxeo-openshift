# DRP Setup


## Prerequisite

Have two Openshift environment:

 - active: the one that is used as primary and that will be shut down
 - passive: the one on which we replicate all datasources

## Steps

### Nuxeo stack installation

- Launch a Nuxeo backing template (with default values) on active and passive openshift
- Launch a Nuxeo template (with default values) on active and passive openshift
- Launch a Nuxeo-s2i template with https://github.com/nuxeo-sandbox/nuxeo-ha-test
- Access Nuxeo on both environment WITHOUT creating any document
- In the PASSIVE environment, set the number of pods of the nuxeo-nuxeo deployment config to 0

### Expose ACTIVE cluster to passive

#### Setup 

On ACTIVE, expose a fixed Kafka port on the app node:

    oc create -f drp/kafka-mirror-source.yaml

#### Test

On ACTIVE create a `test` topic and fill with messages

    oc rsh nuxeo-backings-zoo-0 bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic test

    oc rsh nuxeo-backings-kafka-0 bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
    > one
    > two

On PASSIVE, verify that you can read that topic with the different messages.

     oc rsh nuxeo-backings-kafka-0 bin/kafka-console-consumer.sh --bootstrap-server 10.20.5.4:32094,10.20.6.43:32094,10.20.4.22:32094  --topic test --from-beginning



### Kafka mirror setup

On PASSIVE, edit the `kafka-mirror-destination.yaml` to edit the adress of the ACTIVE app nodes seen from the PASSIVE node.

    oc create -f drp/kafka-mirror-destination.yaml

#### Test

On PASSIVE, launch a consumer:

    oc rsh nuxeo-backings-kafka-0 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning

In parallel launch a producer on ACTIVE:

    oc rsh nuxeo-backings-kafka-0 bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
    > This is a test message

On PASSIVE, the message should appear in the console.


### Kafka Connect for Mongo

#### Build the image

On bothe ACTIVE and PASSIVE, we have to build a Kafka image with the needed Connect plugin:

    oc create -f drp/mongodb-connect-build.yaml

#### Setup

On ACTIVE and PASSIVE, create the common topic 

    oc rsh nuxeo-backings-kafka-0 bin/kafka-topics.sh --create --zookeeper nuxeo-backings-zookeeper-0.nuxeo-backings-zookeeper:2181 --replication-factor 1 --partitions 1 --topic mongo-connect_nuxeo_default

On ACTIVE create the `MongoSourceConnector`:

    oc create -f drp/kafka-connect-source.yaml

On PASSIVE create the `MongoSinkConnector`:

    oc create -f drp/kafka-connect-destination.yaml


Restart the `MirrorMaker` deployment so that it mirrors the new topic.



#### Test

On ACTIVE, in MongoDB create a document in the nuxeo collections.
On PASSIVE, check that the document has been created.

### Kafka Connect for ElasticSearch

TODO

### Final test

On ACTIVE, go the Nuxeo application and create some documents
On PASSIVE, set the number of pods of the `nuxeo-nuxeo` deployment config to 1. After deployment check on the PASSIVE nuxeo app that the documents have been replicated.

