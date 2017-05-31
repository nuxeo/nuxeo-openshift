# Nuxeo on OpenShift and AWS

This repository holds several definitions for k8s services in order to run a Nuxeo Cluster on top of OpenShift

In order to run it, you need a [configured OpenShift cluster on AWS](https://github.com/openshift/openshift-ansible-contrib/tree/master/reference-architecture/aws-ansible). 

This is a work in progress.



## How to run
   
    # Create a specific Security Context Constraint needed for ES
    oc adm policy add-cluster-role-to-user cluster-admin system:serviceaccount:nuxeo:default       
    oc create -f es-scc.yaml
    # Defines the `aws-fast` storage class, update file to fit your needs
    oc create -f aws-storage-class.yaml
    
    # Creates the nuxeo template
    oc create -f nuxeo-template -n openshift    
    
    # Creates a new project and deploys the nuxeo template in it
    oc new-project nuxeo
    oc new-app nuxeo
    
## Current state

The template deploys a Nuxeo cluster based on persistent backing services (MongoDB, Elasticsearch and Redis). The application is then available at http://nuxeo.apps.io.nuxeo.com. All storage is backed by AWS EBS which are provisionned dynamically (see [aws-storage-class.yaml](aws-storage-class.aml)

Several limitations for now :

 * Stateful services use EBS storage which is provisionned by the AWS Storage class. Unfortunately it's not possible to specify several AZ for the EBS. As a result, all storage pods are scheduled on the AZ where the storage is present. [PR is waiting](https://github.com/kubernetes/kubernetes/pull/38505) to be able to specify several zones in Kubernetes.
 * Blob storage is not managed in the cluster. It should be mounted on a GlusterFS or NFS storage.
 * Log should be written on a volume and well managed (retention time etc...)

## TODO

 * ~~Add obvious paremeters (size of disks, DNS etc...)~~
 * Add dependencies between services
 * ~~Test rolling upgrade~~
 * ~~Add GlusterFS storage (aka CNS in OpenShift) to handle BlobManager~~
 * ~~Put env variables in resources definitions rather than in Dockerfile~~
 * ~~Add health and readiness checks~~
 * ~~Harmonize object labels~~
 * Idea: put part of nuxeo.conf in ConfigMap to make it more easily editable
 * Provide a Dockerfile to allow studio project installation with Nuxeo Connect credentials.



# Licensing

Most of the source code in the Nuxeo Platform is copyright Nuxeo and
contributors, and licensed under the Apache License, Version 2.0.

See [/licenses](/licenses) and the documentation page [Licenses](http://doc.nuxeo.com/x/gIK7) for details.

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).




