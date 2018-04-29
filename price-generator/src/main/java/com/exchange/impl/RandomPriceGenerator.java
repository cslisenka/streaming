package com.exchange.impl;

import com.exchange.IPricingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RandomPriceGenerator {

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
        double open;
        double high;
        double low;
        double close;
        double prevClose;
        long marketCap;
        double dividendYild;
        double yearHigh;
        double yearLow;
        long volume;
        long avgVolume;
    }

    public void subscribe(String symbol) {
        subscriptions.put(symbol, new Snapshot());
        log.info("{}", subscriptions.toString());
    }

    public void unsubscribe(String symbol) {
        subscriptions.remove(symbol);
        log.info("{}", subscriptions.toString());
    }

    public void addListener(IPricingListener listener) {
        listeners.add(listener);
    }

    public void start() {
        Random r = new Random();
        executor.scheduleWithFixedDelay(() -> {
//            log.debug("subscriptions {}", subscriptions.size());
            subscriptions.forEach((symbol, s) -> {
//                log.debug("generating for {}", symbol);

                if (s.seqNum == 0) {
                    // New subscription
                    double basePrice = r.nextDouble() * 100;
                    s.bid = basePrice;
                    s.last = s.bid + r.nextDouble();
                    s.ask = s.last + r.nextDouble() * 10;
                    s.bidSize = Math.abs(r.nextInt(1000));
                    s.askSize = Math.abs(r.nextInt(1000));
                    s.seqNum = 1;
                    s.open = Math.abs(basePrice - r.nextDouble() * 100);
                    s.close = Math.abs(basePrice + r.nextDouble() * 100);
                    s.high = s.open + r.nextDouble() * 100;
                    s.low = s.high / 2;
                    s.prevClose = s.close + r.nextDouble() * 100;
                    s.marketCap = r.nextLong();
                    s.dividendYild = r.nextDouble() * 10;
                    s.yearHigh = Math.max(s.prevClose, s.high);
                    s.yearLow = Math.min(s.close, s.low);
                    s.volume = r.nextLong();
                    s.avgVolume = r.nextLong();
                } else {
                    s.seqNum++;
                    double change = 5 * (r.nextBoolean() ? r.nextDouble() : (-1) * r.nextDouble());
                    int sizeChange = r.nextBoolean() ? r.nextInt(100) : (-1) * r.nextInt(100);
                    if (highProbability(r)) {
                        s.bid = Math.abs(s.bid + change);
                        if (s.bid > 100) {
                            s.bid = 100;
                        }

                    }

                    if (highProbability(r)) {
                        s.ask = Math.abs(s.bid + change);
                        if (s.ask > 100) {
                            s.ask = 100;
                        }
                    }

                    if (lowProbability(r)) {
                        s.bidSize = Math.abs(s.bidSize + sizeChange);
                        if (s.bidSize > 10_000) {
                            s.bidSize = 10000;
                        }
                    }

                    if (lowProbability(r)) {
                        s.askSize = Math.abs(s.askSize + sizeChange);
                        if (s.askSize > 10_000) {
                            s.askSize = 10000;
                        }
                    }

                    if (veryLowProbability(r)) {
                        // Very low probability
                        s.last = Math.abs((s.ask + s.bid) / 2 + change);
                        if (s.last > 100) {
                            s.last = 100;
                        }
                    }

                    if (veryLowProbability(r)) {
                        // Very low probability
                        s.last = Math.abs((s.ask + s.bid) / 2 + change);
                        if (s.last > 100) {
                            s.last = 100;
                        }
                    }

                    if (highProbability(r)) {
                        s.volume = r.nextLong();
                        s.avgVolume = r.nextLong();
                        s.marketCap = r.nextLong();
                    }

                    if (highProbability(r)) {
                        s.open = Math.abs(s.open + change);
                        s.close = Math.abs(s.close + change);
                        s.high = s.open + change;
                        s.low = s.high / 2;
                        s.prevClose = s.close + change;

                    }

                    if (veryLowProbability(r)) {
                        s.dividendYild = r.nextDouble() * 10;
                    }

                    s.yearHigh = Math.max(s.prevClose, s.high);
                    s.yearLow = Math.min(s.close, s.low);
                }

                Map<String, String> data = new HashMap<>();
                data.put("BID", formatter.format(s.bid));
                data.put("ASK", formatter.format(s.ask));
                data.put("LAST", formatter.format(s.last));
                data.put("BID_SIZE",  s.bidSize + "");
                data.put("ASK_SIZE", s.askSize + "");
                data.put("SEQ_NUM", s.seqNum + "");
                data.put("TIMESTAMP", System.currentTimeMillis() + "");
                data.put("OPEN", s.open + "");
                data.put("CLOSE", s.close + "");
                data.put("HIGH", s.high + "");
                data.put("LOW", s.low + "");
                data.put("PREV_CLOSE", s.prevClose + "");
                data.put("MARKET_CAP", s.marketCap + "");
                data.put("DIVIDEND_YILD", s.dividendYild + "");
                data.put("YEAR_HIGH", s.yearHigh + "");
                data.put("YEAR_LOW", s.yearLow + "");
                data.put("VOLUME", s.volume + "");
                data.put("AVG_VOLUME", s.avgVolume + "");

                listeners.forEach(l -> {
                    try {
                        l.onData(symbol, new HashMap<>(data));
                    } catch (Exception e) {
                        log.error("Error in executor", e);
                    }
                });

            });
        }, 0, delay, TimeUnit.MILLISECONDS);
    }

    public boolean highProbability(Random r) {
        return r.nextBoolean();
    }

    private boolean averageProbability(Random r) {
        return highProbability(r) && r.nextBoolean();
    }

    private boolean lowProbability(Random r) {
        return averageProbability(r) && r.nextBoolean()  && r.nextBoolean() && r.nextBoolean();
    }

    private boolean veryLowProbability(Random r) {
        return lowProbability(r) && r.nextBoolean();
    }

    public void shutdown() {
        executor.shutdown();
    }
}