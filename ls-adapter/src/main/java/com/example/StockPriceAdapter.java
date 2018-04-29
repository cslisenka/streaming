package com.example;

import com.exchange.IPricingListener;
import com.exchange.impl.RandomPriceGenerator;
import com.lightstreamer.interfaces.data.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockPriceAdapter implements SmartDataProvider, IPricingListener {

    private Logger logger;

    private ItemEventListener listener;

    private final RandomPriceGenerator gen = new RandomPriceGenerator(10);
    private final Map<String, Object> items = new ConcurrentHashMap<>();

    @Override
    public void init(Map params, File configDir) throws DataProviderException {
        logger = Logger.getLogger("LS_demos_Logger.StockQuotes");
        logger.info("Starting " + this.getClass().getName() + " adapter");

        gen.addListener(this);
        gen.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown adapter");
            gen.shutdown();
        }));
    }

    @Override
    public void subscribe(String itemName, Object itemHandle, boolean needsIterator)
            throws SubscriptionException, FailureException {
        logger.info("subscribing " + itemName);
        items.put(itemName, itemHandle);
        gen.subscribe(itemName);
    }

    @Override
    public void subscribe(String itemName, boolean needsIterator)
            throws SubscriptionException, FailureException {
        logger.info("subscribing " + itemName + " needsIterator=" + needsIterator);
        // TODO what is this?
    }

    @Override
    public void unsubscribe(String itemName) throws SubscriptionException,
            FailureException {
        logger.info("unsubscribing " + itemName);
        items.remove(itemName);
        gen.unsubscribe(itemName);
    }

    @Override
    public void onData(String symbol, Map<String, String> data) {
//        logger.info("sending " + symbol + " " + data);
        Object itemHandle = items.get(symbol);
        if (itemHandle != null) {
            listener.smartUpdate(itemHandle, data, false);
        }
    }

    @Override
    public boolean isSnapshotAvailable(String itemName) throws SubscriptionException {
        logger.info("Checking if snapshot available");
        return true;
    }

    @Override
    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
