package com.dev.moneytransfer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.utils.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

public class ApplicationTest {

    @org.junit.Test
    public void run() {
        String testUrl = "/transfer/a1/a2?sum=31.43";

        TestResponse res = request("POST", testUrl, null);
        assertEquals(200, res.status);
        assertEquals("1", res.body);
    }

    @Before
    public void setUp() throws Exception {
        Application app = new Application();
        app.run(7687);
        awaitInitialization();
    }

    @After
    public void tearDown() throws Exception {
        stop();
    }

    public static TestResponse request(String method, String path, String requestBody) {

        try {
            URL url = new URL("http://localhost:7687" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.connect();
            String body = IOUtils.toString(connection.getInputStream());
            return new TestResponse(connection.getResponseCode(), body);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Sending request failed: " + e.getMessage());
            return null;
        }
    }

    public static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}