# Nuxeo on OpenShift and AWS

This repository holds several definitions for k8s services in order to run a Nuxeo Cluster on top of OpenShift

In order to run it, you need a [configured OpenShift cluster on AWS](https://github.com/openshift/openshift-ansible-contrib/tree/master/reference-architecture/aws-ansible). 

This is a work in progress.



## How to run
   
    # oc create -f aws-storage-class.yaml
    storageclass "aws-fast" created

    # oc create -f es-scc.yaml
    securitycontextconstraints "es-scc" created
    # oc create -f nuxeo-backing-template.yaml -n openshift
    template "nuxeo-backings"
    # oc new-project nuxeo
    Now using project "nuxeo" on server "https://....:443".

    You can add applications to this project with the 'new-app' command. For example, try:

        oc new-app centos/ruby-22-centos7~https://github.com/openshift/ruby-ex.git

    to build a new example application in Ruby.
    # oc create sa elasticsearch
    serviceaccount "elasticsearch" created
    # oc adm policy add-scc-to-user es-scc system:serviceaccount:nuxeo:elasticsearch
    # oc adm policy add-role-to-user view system:serviceaccount:nuxeo:elasticsearch



    
## Current state

The template deploys a Nuxeo cluster based on persistent backing services (MongoDB, Elasticsearch and Kafka/Zookeeper). All storage is backed by AWS EBS which are provisionned dynamically (see [aws-storage-class.yaml](aws-storage-class.aml)


# Licensing

Most of the source code in the Nuxeo Platform is copyright Nuxeo and
contributors, and licensed under the Apache License, Version 2.0.

See [/licenses](/licenses) and the documentation page [Licenses](http://doc.nuxeo.com/x/gIK7) for details.

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).




