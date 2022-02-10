package org.jumpmind.pos.service.compatibility;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jumpmind.pos.service.EndpointInvoker;
import org.jumpmind.pos.service.TestServiceConfig;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestServiceConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BackwardCompatibilityTest {

    @Autowired
    ITestingCustomerService testingCustomerService;

    @Autowired
    ClientContext clientContext;

    @Autowired
    EndpointInvoker endpointInvoker;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private ObjectMapper objectMapper = DefaultObjectMapper.defaultObjectMapper();

    @Test
    public void testNoVersionGet() {
        clientContext.remove("version.nu-commerce");
        TestingGetCustomerResponse response = testingCustomerService.getCustomer("1234");
        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
        Assert.assertNotNull("Result not specifying a version should default to V2", response.getCustomerModel().getCustomerAccounts());
    }

    @Test
    public void testResponseChildObjectReplace() throws Exception {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "0.9.0");
                    return execution.execute(request, body);
                }));

        String jsonResponse =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/saveCustomer",
                        "{\"customerId\":\"1234\"}", String.class);

        TestingV09SaveCustomerResponse response =
                objectMapper.readValue(jsonResponse, TestingV09SaveCustomerResponse.class);

        Assert.assertEquals("V09 field set", response.getCustomerModel().getCustomerV09Field());
    }

    @Test
    public void testRequestChildObjectReplace() throws Exception {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "0.8.2");
                    return execution.execute(request, body);
                }));

        String jsonResponse =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/saveCustomer",
                        "{\"customerId\":\"1234\", \"customerV09Field\":\"V09\"}", String.class);

        TestingV09SaveCustomerResponse response =
                objectMapper.readValue(jsonResponse, TestingV09SaveCustomerResponse.class);

        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
        Assert.assertNull(response.getCustomerModel().getCustomerV09Field());
    }

    @Test
    public void testDevVersionGet() {
        clientContext.put("version.nu-commerce", null);
        TestingGetCustomerResponse response = testingCustomerService.getCustomer("1234");
        Assert.assertEquals("1234",response.getCustomerModel().getCustomerId());
        Assert.assertNotNull("Result not specifying a version should default to V2", response.getCustomerModel().getCustomerAccounts());
    }

    @Test
    public void testV1Get() {
        clientContext.put("version.nu-commerce", "1.10.0");
        TestingGetCustomerResponse response = testingCustomerService.getCustomer("1234");
        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
        Assert.assertNotNull("Result should V1 style", response.getCustomerModel().getLoyaltyCustomerId());
        Assert.assertNull("Result should V1 style", response.getCustomerModel().getCustomerAccounts());
    }

    @Test
    public void testV2Get() {
        clientContext.put("version.nu-commerce", "2.11.0");
        TestingGetCustomerResponse response = testingCustomerService.getCustomer("1234");
        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
        Assert.assertNotNull("Result should V2 style", response.getCustomerModel().getCustomerAccounts());
    }

    @Test
    public void testNoVersionSave() {
        clientContext.remove("version.nu-commerce");
        TestingSaveCustomerRequest request = new TestingSaveCustomerRequest();
        TestingCustomerModel customer = buildCustomer();
        request.setCustomerModel(customer);
        TestingSaveCustomerResponse response = testingCustomerService.saveCustomer(request);
        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
        Assert.assertNotNull("Result not specifying a version should default to V2", response.getCustomerModel().getCustomerAccounts());
    }

    @Test
    public void testV1Save() throws Throwable {
        clientContext.put("version.nu-commerce", "1.10.0");

        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "1.10.0");
                    return execution.execute(request, body);
                }));

        TestingSaveCustomerResponse response =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/saveCustomer",
                        "{\"customerId\":\"1234\"}", TestingSaveCustomerResponse.class);

        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
        Assert.assertNotNull("Result should V1 style", response.getCustomerModel().getLoyaltyCustomerId());
        Assert.assertNull("Result should V1 style", response.getCustomerModel().getCustomerAccounts());
    }

    @Test
    public void testPathSpecificResponse() throws Throwable {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "1.10.0");
                    return execution.execute(request, body);
                }));

        TestingSaveCustomerResponse response =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/unlinkCustomer",
                        "{\"customerId\":\"1234\"}", TestingSaveCustomerResponse.class);

        Assert.assertEquals("visited unlinkCustomer response filter", response.getCustomerModel().getCustomerId());
    }

    @Test
    public void testPathSpecificRequest() throws Throwable {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "0.8.0");
                    return execution.execute(request, body);
                }));

        TestingSaveCustomerResponse response =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/unlinkCustomer",
                        "{\"customerModel\":{\"customerId\":\"1234\"}}", TestingSaveCustomerResponse.class);

        Assert.assertEquals("visited unlinkCustomer request filter", response.getCustomerModel().getCustomerId());
    }

    @Test
    public void testPathSpecificResponseVersionMiss() throws Throwable {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "100.1");
                    return execution.execute(request, body);
                }));

        TestingSaveCustomerResponse response =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/unlinkCustomer",
                        "{\"customerModel\":{\"customerId\":\"1234\"}}", TestingSaveCustomerResponse.class);

        Assert.assertEquals("1234", response.getCustomerModel().getCustomerId());
    }

    @Test
    public void testNestedListObject() throws Exception {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("ClientContext-version.nu-commerce", "2.1");
                    return execution.execute(request, body);
                }));

        String jsonResponse =
                restTemplate.postForObject("http://localhost:" + port + "/testingCustomer/getCustomer",
                        "\"1234\"", String.class);

        System.out.println(jsonResponse);

        // checking that rewardAmount got converted into a Money.
        Assert.assertTrue(jsonResponse.contains("\"rewardAmount\":{\""));
    }

    private TestingCustomerModel buildCustomer() {
        TestingCustomerModel customer = new TestingCustomerModel();
        customer.setCustomerId("1234");
        List<TestingCustomerAccount> accounts = new ArrayList<>();
        TestingCustomerAccount account = new TestingCustomerAccount();
        account.setCustomerAccountId("5678");
        account.setAccountType("LOYALTY");
        accounts.add(account);
        customer.setCustomerAccounts(accounts);
        return customer;
    }

}