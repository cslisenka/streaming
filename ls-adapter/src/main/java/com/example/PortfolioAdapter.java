package com.example;

import com.lightstreamer.interfaces.data.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PortfolioAdapter implements SmartDataProvider {

    private Logger logger;
    private ItemEventListener listener;
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    private final Map<String, Subscription> items = new ConcurrentHashMap<>();

    public static class Subscription {
        private final Object handle;
        private final Map<String, String> values = new HashMap<>();

        public Subscription(Object handle) {
            this.handle = handle;
        }

        public Map<String, String> getItems() {
            return values;
        }

        public Object getHandle() {
            return handle;
        }
    }

    @Override
    public void subscribe(String itemName, Object itemHandle, boolean needsIterator) throws SubscriptionException, FailureException {
        items.put(itemName, new Subscription(itemHandle));

        // Sending part of initial snapshot
        exec.schedule(() -> {
            Map<String, String> values = items.get(itemName).values;
            values.put("AAPL", "1");
            values.put("IBM", "1");

            values.forEach((k, v) -> {
                Map<String, String> update = new HashMap<>();
                update.put("command", "ADD");
                update.put("key", k);
                update.put("value", v);
                listener.smartUpdate(itemHandle, update, true); // Snapshot
//                listener.smartEndOfSnapshot(itemHandle); // Indicating end of snapshot
            });
        }, 1000, TimeUnit.MILLISECONDS);

        // Send end of initial snapshot
        exec.schedule(() -> {
            Map<String, String> values = items.get(itemName).values;
            values.put("QQQ", "1");

            Map<String, String> update = new HashMap<>();
            update.put("command", "ADD");
            update.put("key", "QQQ");
            update.put("value", "1");
            listener.smartUpdate(itemHandle, update, true); // Snapshot
//            listener.smartEndOfSnapshot(itemHandle); // Indicating end of snapshot, called automatically
        }, 1500, TimeUnit.MILLISECONDS);

        // Update - add new position
        exec.schedule(() -> {
            Map<String, String> values = items.get(itemName).values;
            values.put("EPAM", "1");

            Map<String, String> update = new HashMap<>();
            update.put("command", "ADD");
            update.put("key", "EPAM");
            update.put("value", "1");
            listener.smartUpdate(itemHandle, update, false); // Update
        }, 2000, TimeUnit.MILLISECONDS);

        // Update - change value
        exec.schedule(() -> {
            Map<String, String> values = items.get(itemName).values;
            values.put("AAPL", "2");

            Map<String, String> update = new HashMap<>();
            update.put("command", "UPDATE");
            update.put("key", "AAPL");
            update.put("value", "2");
            listener.smartUpdate(itemHandle, update, false); // Update
        }, 3000, TimeUnit.MILLISECONDS);

        // Update - change value
        exec.schedule(() -> {
            Map<String, String> values = items.get(itemName).values;
            values.remove("IBM");

            Map<String, String> update = new HashMap<>();
            update.put("command", "DELETE");
            update.put("key", "AAPL");
            listener.smartUpdate(itemHandle, update, false); // Update
        }, 4000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void subscribe(String itemName, boolean needsIterator) throws SubscriptionException, FailureException {
        // TODO what to do here?
    }

    @Override
    public void unsubscribe(String itemName) throws SubscriptionException, FailureException {
        items.remove(itemName);
    }

    @Override
    public boolean isSnapshotAvailable(String itemName) throws SubscriptionException {
        return true;
    }

    @Override
    public void init(Map params, File configDir) throws DataProviderException {
        logger = Logger.getLogger("LS_demos_Logger.StockQuotes");
        logger.info("Starting " + this.getClass().getName() + " adapter");
    }

    @Override
    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }
}