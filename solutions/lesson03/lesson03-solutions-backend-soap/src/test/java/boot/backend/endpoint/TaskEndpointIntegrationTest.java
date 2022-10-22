package boot.backend.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.*;

@SpringBootTest
public class TaskEndpointIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient mockClient;

    @BeforeEach
    public void init() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    public void testCreateAndGetTask() {
        Source requestPayload = new StringSource(
                "<ns2:createTaskRequest xmlns:ns2=\"http://www.anderscore.com/soap\">" +
                        "<ns2:task><ns2:id>1</ns2:id><ns2:description>TestTask</ns2:description><ns2:state>DONE</ns2:state></ns2:task>" +
                        "</ns2:createTaskRequest>");

        mockClient
                .sendRequest(withPayload(requestPayload))
                .andExpect(noFault());

        requestPayload = new StringSource(
                "<as:getTaskRequest xmlns:as=\"http://www.anderscore.com/soap\">" +
                        "<as:id>1</as:id>" +
                        "</as:getTaskRequest>");

        Source responsePayload = new StringSource(
                "<ns2:getTaskResponse xmlns:ns2=\"http://www.anderscore.com/soap\">" +
                        "<ns2:task><ns2:id>1</ns2:id><ns2:description>TestTask</ns2:description><ns2:state>DONE</ns2:state></ns2:task>" +
                        "</ns2:getTaskResponse>");

        mockClient
                .sendRequest(withPayload(requestPayload))
                .andExpect(noFault())
                .andExpect(payload(responsePayload));
    }
}