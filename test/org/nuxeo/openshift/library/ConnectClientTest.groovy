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
    File jsonFile = new File("test/resources/applicableClients.json");
    ArrayList applicableClients = new ObjectMapper().readValue(new FileInputStream(jsonFile), ArrayList.class)
    Mockito.when(connectClient.queryConnectApplicableClients("myusername", "mypassword")).thenReturn(applicableClients)
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
}
