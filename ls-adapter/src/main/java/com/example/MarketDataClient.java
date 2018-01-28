package com.example;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataClient {

    private Map<String, Boolean> subscriptions = new ConcurrentHashMap<>();
    private MarketDataListener listener;

    public void subscribe(String symbol) {
        subscriptions.put(symbol, true);
        new MarketDataThread(symbol).start();
    }

    public void unsubscribe(String symbol) {
        subscriptions.remove(symbol);
    }

    public void setListener(MarketDataListener listener) {
        this.listener = listener;
    }

    class MarketDataThread extends Thread {

        private final String symbol;

        public MarketDataThread(String symbol) {
            setName("MD-" + symbol);
            this.symbol = symbol;
        }

        public void run() {
            Random r = new Random();
            while (subscriptions.containsKey(symbol)) {
                Map<String, String> data = new HashMap<>();
                double bid = r.nextDouble() * 100;
                double last = bid + r.nextDouble() * 10;
                double ask = last + r.nextDouble() * 10;
                data.put("BID", bid + "");
                data.put("ASK", ask + "");
                data.put("LAST", last + "");
                data.put("BID_SIZE", bid + "");
                data.put("ASK_SIZE", ask + "");
                data.put("TIMESTAMP", new Date().toString());
                listener.onDataReceived(symbol, data);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}