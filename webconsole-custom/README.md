# Openshift Webconsole custom


Docker container image that hold customization for Openshift Web Console

# Installation

Create a new application in the `openshift-web-console` project and expose the created service:

```
oc new-app https://github.com/nuxeo/nuxeo-openshift --context-dir=webconsole-custom --name webconsole-custom -n openshift-web-console
oc expose svc/webconsole-custom -n openshift-web-console
```

Update the route configuration to change it to a secure route (SSL).
After that, retrieve the DNS of the created route and update the `webconsole-config` configuration map:

```
oc edit cm webconsole-config -n openshift-web-console
```

And edit the `scriptURLs` and `stylesheetURLs` value to point to the newly created service:
```
apiVersion: v1
data:
  webconsole-config.yaml: |

   ...

    extensions:
      properties: {}
      scriptURLs: [ "https://webconsole-custom-openshift-web-console.apps.prod.nuxeo.io/openshift-extension.js" ]
      stylesheetURLs: [ "https://webconsole-custom-openshift-web-console.apps.prod.nuxeo.io/openshift-extension.css" ]
   ...

```

