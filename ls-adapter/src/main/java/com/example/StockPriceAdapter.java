package com.example;

import com.lightstreamer.interfaces.data.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockPriceAdapter implements SmartDataProvider {

    private Logger logger;

    private ItemEventListener listener;
    private MarketDataClient client;
    private Map<String, Object> itemNameToHandle = new ConcurrentHashMap<>();

    @Override
    public void init(Map params, File configDir) throws DataProviderException {
        logger = Logger.getLogger("LS_demos_Logger.StockQuotes");
        logger.info("Starting " + this.getClass().getName() + " adapter");

        // TODO init multiple clients and load-balance between them
        client = new MarketDataClient();

        client.setListener((item, data) ->  {
            logger.info("sending " + item + " " + data);
            Object itemHandle = itemNameToHandle.get(item);
            if (itemHandle != null) {
                listener.smartUpdate(itemHandle, data, false);
            }
        });
    }

    @Override
    public boolean isSnapshotAvailable(String itemName) throws SubscriptionException {
        return false;
    }

    @Override
    public void setListener(ItemEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void subscribe(String itemName, Object itemHandle, boolean needsIterator)
            throws SubscriptionException, FailureException {
        itemNameToHandle.put(itemName, itemHandle);

        logger.info("subscribing " + itemName);

        client.subscribe(itemName);
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
        itemNameToHandle.remove(itemName);
        client.unsubscribe(itemName);
    }
}
