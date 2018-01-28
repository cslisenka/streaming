package com.example;

import org.junit.Test;

public class TestMarketDataClient {

    private MarketDataClient client = new MarketDataClient();

    @Test
    public void test() throws InterruptedException {
        client.setListener((symbol, data) -> System.out.println(symbol + " " + data));

        client.subscribe("AAPL");
        client.subscribe("IBM");

        Thread.sleep(5000);

        System.out.println("Unsubscribing");
        client.unsubscribe("AAPL");
        client.unsubscribe("IBM");

        Thread.sleep(5000);
    }
}