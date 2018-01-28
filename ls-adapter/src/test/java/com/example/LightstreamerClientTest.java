package com.example;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class LightstreamerClientTest {

    private SubscriptionListener subscriptionListener = new SystemOutSubscriptionListener();
    private ClientListener clientListener = new SystemOutClientListener();

    private static final String[] fields = {
        "BID", "ASK", "BID_SIZE", "ASK_SIZE", "LAST", "TIMESTAMP"
    };

    @Test
    public void test() throws InterruptedException {
        LightstreamerClient client = new LightstreamerClient("http://localhost:8080", "WELCOME");
        client.addListener(clientListener);

        client.connect();

        Subscription aapl = new Subscription("MERGE", new String[] { "STOCK.AAPL", "STOCK.EPAM", "STOCK.IBM" }, fields);
        aapl.setRequestedSnapshot("yes");
        aapl.setDataAdapter("STOCK");
        aapl.addListener(subscriptionListener);

//        Subscription ibm = new Subscription("MERGE", new String[] { "IBM" }, fields);
//        ibm.setRequestedSnapshot("yes");
//        ibm.setDataAdapter("STOCK");
//        ibm.addListener(subscriptionListener);

//        Subscription epam = new Subscription("MERGE", new String[] { "EPAM" }, fields);
//        epam.setRequestedSnapshot("yes");
//        epam.setDataAdapter("STOCK");
//        epam.addListener(subscriptionListener);

        System.out.println("Subscribing");
        client.subscribe(aapl);
        Thread.sleep(10_000);
//        client.subscribe(ibm);
//        Thread.sleep(2_000);
//        client.subscribe(epam);
//        Thread.sleep(2_000);

        System.out.println("Un-subscribing");
        client.unsubscribe(aapl);
        Thread.sleep(10_000);
//        client.unsubscribe(ibm);
//        Thread.sleep(2_000);
//        client.unsubscribe(epam);
//        Thread.sleep(2_000);

        client.disconnect();
    }
}