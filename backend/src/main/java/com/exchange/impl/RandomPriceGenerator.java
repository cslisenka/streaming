package com.exchange.impl;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;

public class RandomPriceGenerator implements IPricingClient {

    private static final Logger log = LoggerFactory.getLogger(RandomPriceGenerator.class);

    private final NumberFormat formatter = new DecimalFormat("#0.00");
    private final int delay;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Snapshot> subscriptions = new ConcurrentHashMap<>();
    private List<IPricingListener> listeners = new ArrayList<>();

    public RandomPriceGenerator(int delay) {
        this.delay = delay;
    }

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
        log.info("{}", subscriptions.toString());
    }

    @Override
    public void unsubscribe(String symbol) {
        subscriptions.remove(symbol);
        log.info("{}", subscriptions.toString());
    }

    @Override
    public void addListener(IPricingListener listener) {
        listeners.add(listener);
    }

    @Override
    public void start() {
        log.info("starting");
        Random r = new Random();
        executor.scheduleWithFixedDelay(() -> {
            log.debug("subscriptions {}", subscriptions.size());
            subscriptions.forEach((symbol, s) -> {
                log.debug("generating for {}", symbol);

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
                    } else if (r.nextBoolean() &&
                            r.nextBoolean() &&
                            r.nextBoolean() &&
                            r.nextBoolean() &&
                            r.nextBoolean()) {
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

                listeners.forEach(l -> {
                    try {
                        l.onData(symbol, data);
                    } catch (Exception e) {
                        log.error("Error in executor", e);
                    }
                });

            });
        }, 0, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}