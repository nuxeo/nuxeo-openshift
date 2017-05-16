# Nuxeo on OpenShift and AWS

This repository holds several definitions for k8s services in order to run a Nuxeo Cluster on top of OpenShift

In order to run it, you need a [configured OpenShift cluster on AWS](https://github.com/openshift/openshift-ansible-contrib/tree/master/reference-architecture/aws-ansible). 

This is a work in progress.


## How to run
   
    # Create a specific Security Context Constraint needed for ES
    oc create -f es-scc.yaml
    oc create -f backing/es-discovery-svc.yaml
    oc create -f backing/es-svc.yaml
    oc adm policy add-scc-to-user es-scc system:serviceaccount:nuxeo:default
    oc create -f backing/es-master.yaml
    
    # Define a aws-fast storage class used by the stateful sets
    oc create -f aws-storage-class.yaml
    oc create -f backing/es-data-svc.yaml
    oc create -f backing/mongo-statefulset.yaml
    oc create -f backing/redis.yaml
    
    oc new-app https://github.com/nuxeo-sandbox/nuxeo-openshift

# Licensing

Most of the source code in the Nuxeo Platform is copyright Nuxeo and
contributors, and licensed under the Apache License, Version 2.0.

See [/licenses](/licenses) and the documentation page [Licenses](http://doc.nuxeo.com/x/gIK7) for details.

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).




