package com.example;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class LoggingSubscriptionListener implements SubscriptionListener {

    private static final Logger log = Logger.getLogger(LoggingSubscriptionListener.class);

    private final String label;

    public LoggingSubscriptionListener(String label) {
        this.label = label;
    }

    @Override
    public void onClearSnapshot(String s, int i) {
        log.info("onClearSnapshot");
    }

    @Override
    public void onCommandSecondLevelItemLostUpdates(int i, String s) {
        log.info("onCommandSecondLevelItemLostUpdates");
    }

    @Override
    public void onCommandSecondLevelSubscriptionError(int i, String s, String s1) {
        log.info("onCommandSecondLevelSubscriptionError");
    }

    @Override
    public void onEndOfSnapshot(String s, int i) {
        log.info("onEndOfSnapshot");
    }

    @Override
    public void onItemLostUpdates(String s, int i, int i1) {
        log.info("onItemLostUpdates");
    }

    @Override
    public void onItemUpdate(ItemUpdate itemUpdate) {
        log.info("onItemUpdate " + itemUpdate.getItemName() + " " + itemUpdate.getFields());
    }

    @Override
    public void onListenEnd(Subscription subscription) {
        log.info("onListenEnd " + Arrays.toString(subscription.getItems()));
    }

    @Override
    public void onListenStart(Subscription subscription) {
        log.info("onListenStart " + Arrays.toString(subscription.getItems()));
    }

    @Override
    public void onSubscription() {
        log.info("onSubscription (" + label + ")");
    }

    @Override
    public void onSubscriptionError(int i, String s) {
        log.info("onSubscriptionError " + s);
    }

    @Override
    public void onUnsubscription() {
        log.info("onUnsubscription (" + label + ")");
    }

    @Override
    public void onRealMaxFrequency(String s) {
        log.info("onRealMaxFrequency (" + label + ")=" + s);
    }
}