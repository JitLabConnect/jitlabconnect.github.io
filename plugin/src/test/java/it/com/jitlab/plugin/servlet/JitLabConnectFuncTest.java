package it.com.jitlab.plugin.servlet;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


public class JitLabConnectFuncTest {

    HttpClient httpClient;
    String baseUrl;
    String servletUrl;

    @Before
    public void setup() {
        httpClient = new DefaultHttpClient();
        baseUrl = System.getProperty("baseurl");
        servletUrl = baseUrl + "/plugins/servlet/jitlab";
    }

    @After
    public void tearDown() {
        httpClient.getConnectionManager().shutdown();
    }

    @Test
    public void testSomething() throws IOException {
        HttpGet httpget = new HttpGet(servletUrl);

        // Create a response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(httpget, responseHandler);
        assertTrue(null != responseBody && !"".equals(responseBody));
    }
}
