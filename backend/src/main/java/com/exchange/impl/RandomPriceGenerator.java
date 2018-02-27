package com.exchange.impl;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RandomPriceGenerator implements IPricingClient {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, AtomicLong> subscriptions = new ConcurrentHashMap<>();
    private IPricingListener listener;

    @Override
    public void subscribe(String symbol) {
        subscriptions.put(symbol, new AtomicLong(0));
    }

    @Override
    public void unsubscribe(String symbol) {
        subscriptions.remove(symbol);
    }

    @Override
    public void setListener(IPricingListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            Random r = new Random();
            subscriptions.keySet().forEach(s -> {
                Map<String, String> data = new HashMap<>();
                double bid = r.nextDouble() * 100;
                double last = bid + r.nextDouble();
                double ask = last + r.nextDouble() + 5;

                data.put("BID", bid + "");
                data.put("ASK", ask + "");
                data.put("LAST", last + "");
                data.put("BID_SIZE", Math.abs(r.nextInt()) + "");
                data.put("ASK_SIZE", Math.abs(r.nextInt()) + "");
                data.put("SEQ_NUM", subscriptions.get(s).incrementAndGet() + "");
                data.put("TIMESTAMP", LocalTime.now() + "");

                listener.onData(s, data);
            });
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}