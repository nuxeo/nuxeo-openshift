# MongoDB template


This part of the repository defines the template to deploy two flavors of MongoDB:

 * Simple MongoDB cluster with Replicaset
 * MongoDB cluster with Replicasets, authentication and TLS encryption

## Simple MongoDB cluster

This deploys a MongoDB Statefulset, that acts as a cluster with 3 Replicasets. 
 
To launch it, simply install the template and process it.

```
oc create -f mongodb.yaml
```

## MongoDB + Auth + TLS

### Generation of certificates

TLS configuration requires the generation of certificates. In the `certs` folder, launch the `create-certs.sh` script. After answering the questions, it will generate a `certs.yaml` file and a `key.yaml` files that have to be deployed in the Openshift project. It basically creates a k8s secret that contains all certificates needed by TLS.

### Template deployment

Deploy the OS template by processing the template `mongodb-secure.yaml`.

