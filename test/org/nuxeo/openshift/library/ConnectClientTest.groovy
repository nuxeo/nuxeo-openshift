package org.nuxeo.openshift.library

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.ArrayList
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static groovy.test.GroovyAssert.assertEquals

class ConnectClientTest extends GroovyTestCase {

  private ConnectClient connectClient

  private String clientId

	@Before
  void setUp() {
    connectClient = Mockito.spy(new ConnectClient())

    // Mock Http response when querying Connect application clients
    File applicableClientsFile = new File("test/resources/applicableClients.json");
    ArrayList applicableClients = new ObjectMapper().readValue(new FileInputStream(applicableClientsFile), ArrayList.class)
    Mockito.when(connectClient.queryConnectApplicableClients("myusername", "mypassword")).thenReturn(applicableClients)

    // Mock Jenkins error() method
    connectClient.metaClass.error = { message ->
      print(message + "\n")
    }
  }

  @Test
  void test_should_return_client_id() {
    // Given good Connect credentials and Studio project name
    // When trying to get the clientId
    clientId = connectClient.getClientIdByStudioProject("myusername", "mypassword", "my-project")
    // Then getting the good clientId
    assertEquals "1b979653-5483-4329-16bf-a2fe259ac7dk", clientId
  }

  @Test
  void test_should_return_null_when_wrong_connect_credentials() {
    // Given wrong Connect credentials
    // When trying to get the clientId
    clientId = connectClient.getClientIdByStudioProject("myusername", "wrongpassword", "my-project")
    // Then no result
    assertEquals null, clientId
  }

  @Test
  void test_should_return_null_when_wrong_studio_project() {
    // Given a wrong Studio project name
    // When trying to get the clientId
    clientId = connectClient.getClientIdByStudioProject("myusername", "mypassword", "wrong-project")
    // Then no result
    assertEquals null, clientId
  }

  @Test
  void test_should_return_http_status_200() {
    // Given an HTTP response from Connect
    def connectResponseFile = new File("test/resources/connectResponse")
    // When trying to handle the response
    def httpStatus = connectClient.handleHttpResponse(connectResponseFile)
    // Then getting the HTTP status
    assertEquals "200", httpStatus
  }
}
