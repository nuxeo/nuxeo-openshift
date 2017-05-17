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

# Licensing

Most of the source code in the Nuxeo Platform is copyright Nuxeo and
contributors, and licensed under the Apache License, Version 2.0.

See [/licenses](/licenses) and the documentation page [Licenses](http://doc.nuxeo.com/x/gIK7) for details.

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).




