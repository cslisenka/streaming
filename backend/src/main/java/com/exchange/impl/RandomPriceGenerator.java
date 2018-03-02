package com.exchange.impl;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RandomPriceGenerator implements IPricingClient {

    private final NumberFormat formatter = new DecimalFormat("#0.00");

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Map<String, Snapshot> subscriptions = new ConcurrentHashMap<>();
    private IPricingListener listener;

    static class Snapshot {
        double bid;
        int bidSize;
        double ask;
        int askSize;
        double last;
        int seqNum;
    }

    @Override
    public void subscribe(String symbol) {
        subscriptions.put(symbol, new Snapshot());
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
        Random r = new Random();
        executor.scheduleWithFixedDelay(() -> {
            subscriptions.forEach((symbol, s) -> {
                if (s.seqNum == 0) {
                    // New subscription
                    double basePrice = r.nextDouble() * 100;
                    s.bid = basePrice;
                    s.last = s.bid + r.nextDouble();
                    s.ask = s.last + r.nextDouble() * 10;
                    s.bidSize = Math.abs(r.nextInt(1000));
                    s.askSize = Math.abs(r.nextInt(1000));
                    s.seqNum = 1;
                } else {
                    s.seqNum++;
                    double change = 5 * (r.nextBoolean() ? r.nextDouble() : (-1) * r.nextDouble());
                    int sizeChange = r.nextBoolean() ? r.nextInt(100) : (-1) * r.nextInt(100);
                    if (r.nextBoolean()) {
                        s.bid = Math.abs(s.bid + change);
                        s.bidSize = Math.abs(s.bidSize + sizeChange);

                        if (s.bid > 100) {
                            s.bid = 100;
                        }
                        if (s.bidSize > 10_000) {
                            s.bidSize = 10000;
                        }
                    } else if (r.nextBoolean()) {
                        s.ask = Math.abs(s.bid + change);
                        s.askSize = Math.abs(s.askSize + sizeChange);

                        if (s.ask > 100) {
                            s.ask = 100;
                        }
                        if (s.askSize > 10_000) {
                            s.askSize = 10000;
                        }
                    } else if (r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) {
                        // Very rare update
                        s.last = Math.abs((s.ask + s.bid) / 2 + change);
                        if (s.last > 100) {
                            s.last = 100;
                        }
                    }
                }

                Map<String, String> data = new HashMap<>();
                data.put("BID", formatter.format(s.bid));
                data.put("ASK", formatter.format(s.ask));
                data.put("LAST", formatter.format(s.last));
                data.put("BID_SIZE",  s.bidSize + "");
                data.put("ASK_SIZE", s.askSize + "");
                data.put("SEQ_NUM", s.seqNum + "");
                data.put("TIMESTAMP", System.currentTimeMillis() + "");
                listener.onData(symbol, data);
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}