# Nuxeo development template APB

This APB setups a development environment for Nuxeo in Openshift


## How to install

### For development

Create a dedicated Openshift project:

```
oc new-project int-apb-dev
```

Update the configuration of the Ansible Service broker:

```
oc edit configmap asdfa -n openshift-ansible-service-broker
```

Create a new build:

```
oc new-build https://github.com/nuxeo/nuxeo-openshift --context-dir=nuxeo-dev-apb --name=nuxeo-dev-apb
```


### For production


Update the configuration of the Ansible Service broker to point to the Ansible Playbook Bundle catalog:

```
oc edit configmap asdfa -n openshift-ansible-service-broker
```
