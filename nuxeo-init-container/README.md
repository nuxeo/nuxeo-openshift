# Nuxeo init container

This container is uses as a k8s init container that will generate some binding configuration in `/etc/nuxeo/conf.d` based on some volume mounts that are done.


## How to build

```shell
$ docker build -t nuxeo-init .
```

## How to test

Install `shunit2` ([https://github.com/kward/shunit2](https://github.com/kward/shunit2))

Then:

```shell
$ ./test.sh
```

## How to use

Here is a sample Openshift DeploymentConfig.


```yaml
apiVersion: apps.openshift.io/v1
kind: DeploymentConfig
metadata:
    component: nuxeo
  name: nuxeo
spec:
  replicas: 1
  selector:
    component: nuxeo
    deploymentconfig: nuxeo
  template:
    metadata:
      labels:
        component: nuxeo
        deploymentconfig: nuxeo
    spec:
      initContainers:
        - image: 'dmetzler/nuxeo-init-container:latest'
          name: bind
          volumeMounts:
            - mountPath: /etc/nuxeo/conf.d
              name: nuxeoconfd
            - mountPath: /var/lib/nuxeo/data
              name: data
            - mountPath: /opt/nuxeo/bindings/mongodb
              name: mongodb-credentials
            - mountPath: /opt/nuxeo/bindings/elasticsearch
              name: elasticsearch-credentials
      containers:
        - env:
            - name: JAVA_OPTS
              value: '-Djava.net.preferIPv4Stack=true'
            - name: NUXEO_BINARY_STORE
              value: /var/lib/nuxeo/binaries/binaries
            - name: NUXEO_URL
              value: nuxeo.mynuxeo.com
            - name: NUXEO_ENV_NAME
              value: nuxeo
            - name: NUXEO_CLID
              value: >-
                XXXXXXX
          image: nuxeo11
          livenessProbe:
            failureThreshold: 5
            httpGet:
              path: /nuxeo/runningstatus
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 60
            periodSeconds: 10
            successThreshold: 1
            timeoutSeconds: 5
          name: nuxeo
          ports:
            - containerPort: 8080
              protocol: TCP
          readinessProbe:
            failureThreshold: 5
            httpGet:
              path: /nuxeo/runningstatus
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 60
            periodSeconds: 10
            successThreshold: 1
            timeoutSeconds: 5
          volumeMounts:
            - mountPath: /etc/nuxeo/conf.d
              name: nuxeoconfd
            - mountPath: /var/lib/nuxeo/binaries
              name: binaries
            - mountPath: /var/log/nuxeo
              name: logs
            - mountPath: /var/lib/nuxeo/data
              name: data
            - mountPath: /opt/nuxeo/server/tmp
              name: nuxeotmp
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      schedulerName: default-scheduler
      securityContext: {}
      terminationGracePeriodSeconds: 30
      volumes:
        - name: binaries
          persistentVolumeClaim:
            claimName: nuxeo11-sandbox-dev-binaries-pvc
        - emptyDir: {}
          name: logs
        - emptyDir: {}
          name: data
        - emptyDir: {}
          name: nuxeotmp
        - emptyDir: {}
          name: nuxeoconfd
        - name: mongodb-credentials
          secret:
            defaultMode: 420
            secretName: nuxeo11-sandbox-dev-mongodb-credentials
        - name: elasticsearch-credentials
          secret:
            defaultMode: 420
            secretName: nuxeo11-sandbox-dev-elasticsearch-credentials
  test: false
  triggers:
    - type: ConfigChange
```