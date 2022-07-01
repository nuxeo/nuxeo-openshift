package org.nuxeo.openshift.library

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.ArrayList
import java.util.regex.Pattern
import groovy.lang.Closure
import org.apache.commons.io.FileUtils
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

import static org.apache.http.auth.AuthScope.ANY

// For now, this method only works with the Connect Password and does not with the Connect Token
def deployPackageToConnect(connectUsername, connectPassword, studioProject, packageFilePath, connectUrl='https://connect.nuxeo.com') {
  echo "Deploying Package to Connect (${connectUrl})"
  echo "  Attached Studio Project: ${studioProject}"
  echo "  Package: ${packageFilePath}"

  def uploadUrl = connectUrl + "/nuxeo/site/marketplace/uploadOS?studio_project=${studioProject}"
  echo "  URL: ${uploadUrl}"

  def packageFileArg = "package=@${packageFilePath}"
   sh '''
        http_response=$(curl -u ''' + connectUsername + ''':''' + connectPassword + ''' -sni -o output.txt -w '%{http_code}' -F ''' + packageFileArg + ''' ''' + uploadUrl + ''')
        cat output.txt
        if [ $http_response -ge 400 ] ; then exit 1 ; fi
    '''
}

protected getClientIdByStudioProject(connectUsername, connectPassword, studioProject) {
  def applicableClients = queryConnectApplicableClients(connectUsername, connectPassword)
  def clientId = findClientIdByStudioProject(applicableClients, studioProject)

  return clientId
}

protected handleResponse(connectResponseFile) {
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
  def applicableClients = null
  def url = connectUrl + "/nuxeo/api/v1/automation/Connect.applicableClients"
  def params = "{}"
  def post = new HttpPost(url)
  post.addHeader("Content-Type","application/json")
  post.setEntity(new StringEntity(params))

  def provider = new BasicCredentialsProvider()
  def credentials = new UsernamePasswordCredentials(connectUsername, connectPassword)
  provider.setCredentials(ANY, credentials)

  def httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()
  def response = httpClient.execute(post)
  if (response.getEntity() && response.getStatusLine().getStatusCode() == 200 && response.getEntity().getContent() != null) {
    applicableClients = new ObjectMapper().readValue(response.getEntity().getContent(), ArrayList.class)
  }
  response.close()
  httpClient.close()

  return applicableClients
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

return this
