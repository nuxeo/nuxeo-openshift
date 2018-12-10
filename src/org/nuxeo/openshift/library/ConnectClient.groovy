package org.nuxeo.openshift.library

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.ArrayList
import java.util.regex.Pattern
import groovy.lang.Closure
import org.apache.commons.io.FileUtils
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

import static org.apache.http.auth.AuthScope.ANY

// For now, this method only works with the Connect Password and does not with the Connect Token
def deployPackageToConnect(connectUsername, connectPassword, studioProject, packageFilePath, connectUrl='https://connect.nuxeo.com') {
  def clientId = getClientIdByStudioProject(connectUsername, connectPassword, studioProject)
  if (clientId == null) {
    error("No clientId found with user '" + connectUsername + "' for Studio project '" + studioProject + "'.").
    return
  }

  def uploadUrl = connectUrl + "/nuxeo/site/marketplace/upload?batch=true\\&project=${studioProject}\\&owner=${clientId}\\&client=${clientId}"
  def format = "CONNECT_HTTP_STATUS: %{http_code}"
  def packageFileArg = "package=@${packageFilePath}"
  def connectResponseFileName = "connectResponse"
  def connectResponseFile = new File(connectResponseFileName)

  sh "curl -w '${format}' -u ${connectUsername}:${connectPassword} -i -n -F ${packageFileArg} ${uploadUrl} > ${connectResponseFileName}"
  handleHttpResponse(connectResponseFile)
}

def releaseStudioProject(connectUsername, connectPassword, studioProject, version, branch='master', connectUrl='https://connect.nuxeo.com') {
  if (isStudioProjectVersionReleased(connectUsername, connectPassword, studioProject, version)) {
    return version
  }

  def url = connectUrl + "nuxeo/site/studio/v2/project/${studioProject}/releases"
  def paramsMap = new HashMap<String, String>()
  paramsMap.put("versionName", version)
  paramsMap.put("revision", branch)
  def params = new ObjectMapper().writeValueAsString(paramsMap)
  def response = executePostRequest(url, params, connectUsername, connectPassword, HashMap.class)

  if (response != null && response.get("status") == 200) {
    return version
  }

  return null
}

protected getClientIdByStudioProject(connectUsername, connectPassword, studioProject) {
  def applicableClients = queryConnectApplicableClients(connectUsername, connectPassword)
  def clientId = findClientIdByStudioProject(applicableClients, studioProject)

  return clientId
}

protected handleHttpResponse(connectResponseFile) {
  def httpStatus = null
  def connectResponse = FileUtils.readFileToString(connectResponseFile, "UTF-8")
  def pattern = Pattern.compile("(CONNECT_HTTP_STATUS:\\s)(\\d{3})")
  def matcher = pattern.matcher(connectResponse)
  if (matcher.find()) {
    httpStatus = matcher.group(2)
  }
  if (httpStatus != "200") {
    error(connectResponse)
  }

  return httpStatus
}

protected queryConnectApplicableClients(connectUsername, connectPassword, connectUrl='https://connect.nuxeo.com') {
  def url = connectUrl + "/nuxeo/api/v1/automation/Connect.applicableClients"
  def params = "{}"
  def response = executePostRequest(url, params, connectUsername, connectPassword, ArrayList.class)

  if (response != null && response.get("status") == 200) {
    return response.get("content")
  }

  return null
}

protected findClientIdByStudioProject(applicableClients, studioProject) {
  if (applicableClients != null) {
    for (int i = 0; i < applicableClients.size(); i++) {
      def client = applicableClients[i]
      for (int j = 0; j < client.projects.size(); j++) {
        def project = client.projects[j]
        if (project.symbolicName == studioProject) {
          return client.id
        }
      }
    }
  }

  return null
}

protected isStudioProjectVersionReleased(connectUsername, connectPassword, studioProject, version, connectUrl='https://connect.nuxeo.com') {
  def url = connectUrl + "/nuxeo/site/studio/ide/studio/servlet/releases/${version}?project=${studioProject}"
  def response = executeGetRequest(url, connectUsername, connectPassword, String.class)

  if (response != null) {
    return response.get("status") == 200
  }

  return false
}


protected executePostRequest(url, params, connectUsername, connectPassword, responseTypeClass) {
  def request = new HttpPost(url)
  request.addHeader("Content-Type","application/json")
  request.setEntity(new StringEntity(params))

  return executeRequest(request, connectUsername, connectPassword, responseTypeClass)
}

protected executeGetRequest(url, connectUsername, connectPassword, responseTypeClass) {
  def request = new HttpGet(url)

  return executeRequest(request, connectUsername, connectPassword, responseTypeClass)
}

protected executeRequest(request, connectUsername, connectPassword, responseTypeClass) {
  def responseMap = null
  def provider = new BasicCredentialsProvider()
  def credentials = new UsernamePasswordCredentials(connectUsername, connectPassword)
  provider.setCredentials(ANY, credentials)
  def httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()
  def response = httpClient.execute(request)

  if (response.getEntity() && response.getEntity().getContent() != null) {
    responseMap = new HashMap<String, Object>()
    def statusCode = response.getStatusLine().getStatusCode()
    responseMap.put("status", statusCode)
    if (statusCode == 200) {
      responseMap.put("content", new ObjectMapper().readValue(response.getEntity().getContent(), responseTypeClass))
    }
  }

  response.close()
  httpClient.close()

  return responseMap
}

return this
