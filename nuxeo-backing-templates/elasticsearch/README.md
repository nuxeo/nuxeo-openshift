# Elasticsearch backings

This part of the repository defines the template to deploy two flavors of Elasticsearch:

 * Simple elasticsearch cluster
 * Elasticsearch cluster with Searchguard which enables SSL + authentication

## Simple elasticsearch cluster

This deploys 3 ES Statefulset :

 * Data cluster with 3 nodes: holds the real ES data. 
 * Master cluster with 3 nodes: does not hold data and is here to maintain the cluster aurum and discovery.
 * Client cluster: exposes the ES rest API


To launch it, simply install the template and process it.

```
oc create -f elasticsearch.yaml
```

## Elasticsearch + Searchguard

### Build of Searchguard image

First, the Docker image for SearchGuard has to be built. This can be done by creating the `sg-build.yml`:

```
oc create -f sg-build.yaml
```

After that, get the address of the Docker image repository by getting the image stream:

```
oc get is elasticsearch-searchguard
```

The address of the repo will be needed in the later steps.

### Generation of certificates

This deploys the same cluster than above but with SearchGuard enabled. This requires the generation of certificates. In the `certs` folder, launch the `create-certs.sh` script. After answering the questions, it will generate a `certs.yaml` file that has to be deployed in the Openshift project. It basically creates a k8s secret that contains all certificates needed by SearchGuard.

### Template deployment


For the template, we need a few parameters:

The bcrypt encoded password for the `elastic` user in SearchGuard is needed in the template. It can be retrieved by the following command:

```
htpasswd -bnBC 10 "" password | tr -d ':\n'
``` 

Replace `password` by a better passsword.

The Base64 encoded form of the password used for authentication in the OS probes. It can be retrieved by launching:

```
echo -n "elastic:password" | base64
```

Replace `password` with the password used above.


Then deploy the OS template by processing the template `elasticsearch-sg.yaml` by using the following parameters:

 * Hashed password for elastic user (bcrypt): the bcrypt encoded passrod computed earlier.
 * SearchGuard basic auth: The Base64 form computed earlier.
 * IMAGE_REPOSITORY: The adress of the docker repository of the ImageStream.
 * IMAGE_TAG: replace the version by `latest`

### Initiating search guard

Now we can initiate SearchGuard by launching the `sg_admin.sh` script on the master.

```
# oc exec -ti nuxeo-backings-elasticsearch-master-0 bash
[elasticsearch@nuxeo-backings-elasticsearch-master-0 ~]$ chmod +x /usr/share/elasticsearch/plugins/search-guard-5/tools/sgadmin.sh
[elasticsearch@nuxeo-backings-elasticsearch-master-0 ~]$ /usr/share/elasticsearch/plugins/search-guard-5/tools/sgadmin.sh -cd /usr/share/elasticsearch/config/searchguard -icl -nhnv -ts /usr/share/elasticsearch/config/searchguard-ssl/searchguard-truststore.p12 -tspass password -ks /usr/share/elasticsearch/config/searchguard-ssl/client.admin.keystore.p12 -kspass password -tst PKCS12
```


Now the cluster should converge to a stable state and protected with the user `elastic` and the password provided in the previous steps.

