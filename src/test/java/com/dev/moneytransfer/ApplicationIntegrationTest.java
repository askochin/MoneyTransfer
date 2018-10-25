package com.dev.moneytransfer;

import com.dev.moneytransfer.dao.Account;
import com.dev.moneytransfer.dao.TransferDaoTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.utils.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.google.common.io.Resources.getResource;
import static java.lang.String.valueOf;
import static java.lang.System.setProperty;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jdbi.v3.core.Jdbi.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static spark.Spark.awaitInitialization;
import static spark.Spark.awaitStop;
import static spark.Spark.stop;

public class ApplicationIntegrationTest {

    private static final int PORT = 9797;
    private static final String JDBC_URL = "jdbc:h2:mem:accts;DB_CLOSE_DELAY=-1;MULTI_THREADED=TRUE";

    @Test
    public void shouldProcessTransferRequest() {

        String url = "/transfer/a1/a2?amount=31.43";

        TestResponse res = request("POST", url);
        assertEquals(200, res.status);
        assertEquals("1", res.body);
    }

    @Test
    public void shouldHandleBadRequest() {

        String url = "/transfer/a1/unknown?amount=31.43";

        TestResponse res = request("POST", url);
        assertEquals(400, res.status);
        assertEquals("Account not found: unknown", res.body);
    }

    @BeforeEach
    public void setUp() throws InterruptedException, ExecutionException, TimeoutException {

        // init application properties
        setProperty("port", valueOf(PORT));
        setProperty("jdbc.url", JDBC_URL);
        setProperty("db.init.script", getResource("schema.sql").getPath());
        setProperty("connection.pool.max.size", "100");

        // run application
        Application app = new Application();
        app.run();
        runAsync(() -> awaitInitialization()).get(15, SECONDS);

        // add test data
        new TransferDaoTestHelper(create(JDBC_URL))
                .addAccounts(new Account("a1", new BigDecimal("100")), new Account("a2", new BigDecimal("200")));
    }

    @AfterEach
    public void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
        new TransferDaoTestHelper(create(JDBC_URL)).clearDb();
        stop();
        runAsync(() -> awaitStop()).get(15, SECONDS);
    }

    public static TestResponse request(String method, String path) {

        try {
            URL url = new URL("http://localhost:" + PORT + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.connect();
            String body = IOUtils.toString(
                connection.getResponseCode() == 200 ?
                    connection.getInputStream() : connection.getErrorStream());
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