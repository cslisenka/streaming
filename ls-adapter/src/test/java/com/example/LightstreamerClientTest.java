package com.example;

import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class LightstreamerClientTest {

    private static final Logger log = Logger.getLogger(LightstreamerClient.class);

    private static final String[] fields = {
        "BID", "ASK", "BID_SIZE", "ASK_SIZE", "LAST"
    };

    private LightstreamerClient ls = new LightstreamerClient("http://localhost:8080", "WELCOME");
    private Subscription aapl;
    private Subscription ibm;

    @Before
    public void setUp() {
        ls.addListener(new LoggingClientListener(ls));

        aapl = new Subscription("MERGE", "STOCK.AAPL", fields);
        aapl.setRequestedSnapshot("yes");
        aapl.setDataAdapter("STOCK");
        aapl.addListener(new LoggingSubscriptionListener("AAPL"));

        ibm = new Subscription("MERGE", "STOCK.IBM", fields);
        ibm.setRequestedSnapshot("yes");
        ibm.setDataAdapter("STOCK");
        ibm.addListener(new LoggingSubscriptionListener("IBM"));

        ls.connect();
    }

    @Test
    public void test() throws InterruptedException {
        log.info("Subscribing");

        ls.subscribe(aapl);

        Thread.sleep(10_000);

        ls.subscribe(ibm);

        Thread.sleep(2_000);

        log.info("Un-subscribing");
        ls.unsubscribe(aapl);

        Thread.sleep(10_000);

        ls.unsubscribe(ibm);

        Thread.sleep(2_000);
    }

    @After
    public void teadDown() {
        ls.disconnect();
    }
}