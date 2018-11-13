package org.nuxeo.openshift.library

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.ArrayList
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.CloseableHttpResponse

import static org.apache.http.auth.AuthScope.ANY

def getClientIdByStudioProject(connectUsername, connectPassword, studioProject) {
  def applicableClients = queryConnectApplicableClients(connectUsername, connectPassword)
  def clientId = findClientIdByStudioProject(applicableClients, studioProject)

  return clientId
}

protected queryConnectApplicableClients(connectUsername, connectPassword, connectUrl='https://connect.nuxeo.com') {
  def applicableClients = null
  String url = connectUrl + "/nuxeo/api/v1/automation/Connect.applicableClients"
  String params = "{}"
  HttpPost post = new HttpPost(url)
  post.addHeader("Content-Type","application/json")
  post.setEntity(new StringEntity(params))

  CredentialsProvider provider = new BasicCredentialsProvider()
  UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(connectUsername, connectPassword)
  provider.setCredentials(ANY, credentials)

  HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build().withCloseable { httpClient ->
    httpClient.execute(post).withCloseable { response ->
      if (response.getEntity() && response.getStatusLine().getStatusCode() == 200 && response.getEntity().getContent() != null) {
        applicableClients = new ObjectMapper().readValue(response.getEntity().getContent(), ArrayList.class)
      }
    }
  }

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
