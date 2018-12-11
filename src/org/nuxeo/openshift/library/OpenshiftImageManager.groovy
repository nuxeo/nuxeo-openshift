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
  timeout(10) {
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
  timeout(5) {
    openshift.selector("dc", deploymentConfigName).related('pods').untilEach(1) {
      return (it.object().status.phase == "Running")
    }
  }
}

return this
