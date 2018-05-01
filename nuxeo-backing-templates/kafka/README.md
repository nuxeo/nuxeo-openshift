# Kafka templates


Here are two flavors of Openshift/k8s Kafka templates:
 * `kafka-template.yaml` : Kafka cluster with 3 nodes
 * `kafka-template-sasl.yaml` : Kafka cluster with 3 nodes and a SASL endpoint 


## Kafka SASL

For the SASL cluster to work, it needs the creation of JKS stores with certificates. In the `certs` folder, you can find a script that generates a `certs.yaml` files that contains the declaration of a secret resource that holds all the stores and passwords.

Before launching a Kafka SASL template, review the `create-certs.sh` script to update the values and run it: it will generate a `certs.yaml` file that has just to be executed on the Openshift project.
Review also the `kafka-template-sasl.yaml` for the line:

```
export KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://${POD_IP}:9092,SASL_SSL://oso-dev-app-node0${KAFKA_BROKER_ID+1}.dev.nuxeo.io:${NODEPORT} && \
```

the adress of the advertised SASL_SSL listener has to be updated.
