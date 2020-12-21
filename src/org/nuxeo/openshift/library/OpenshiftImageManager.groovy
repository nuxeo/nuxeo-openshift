package org.nuxeo.openshift.library

def build(imageBuild, fromDir=null) {
  def buildConfig = openshift.selector("bc", imageBuild)
  if (fromDir) {
    buildConfig.startBuild("--from-dir='${fromDir}'", "--wait")
  } else {
    buildConfig.startBuild("--wait")
  }
  def buildLastVersion = openshift.selector("bc", imageBuild).object().status.lastVersion
  def lastBuild = openshift.selector("builds", "${imageBuild}-${buildLastVersion}")
  timeout(120) {
    waitUntil {
      lastBuild.object().status.phase == "Complete"
    }
  }
}

def tag(imageName, tag) {
  openshift.tag("${imageName}:latest", "${imageName}:${tag}")
}

def deploy(deploymentConfigName) {
  openshiftDeploy(deploymentConfig: deploymentConfigName)
  def dc = openshift.selector("dc", deploymentConfigName)

  timeout(120) {
    waitUntil {

      return dc.object().status.replicas.equals(dc.object().status.readyReplicas)
    }
  }
}

return this
