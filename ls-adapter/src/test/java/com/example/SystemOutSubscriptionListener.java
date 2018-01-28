package com.example;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SystemOutSubscriptionListener implements SubscriptionListener {

    @Override
    public void onClearSnapshot(@Nullable String s, int i) {
        System.out.println("onClearSnapshot");
    }

    @Override
    public void onCommandSecondLevelItemLostUpdates(int i, @Nonnull String s) {
        System.out.println("onCommandSecondLevelItemLostUpdates");
    }

    @Override
    public void onCommandSecondLevelSubscriptionError(int i, @Nullable String s, String s1) {
        System.out.println("onCommandSecondLevelSubscriptionError");
    }

    @Override
    public void onEndOfSnapshot(@Nullable String s, int i) {
        System.out.println("onEndOfSnapshot");
    }

    @Override
    public void onItemLostUpdates(@Nullable String s, int i, int i1) {
        System.out.println("onItemLostUpdates");
    }

    @Override
    public void onItemUpdate(@Nonnull ItemUpdate itemUpdate) {
        System.out.println("onItemUpdate " + itemUpdate.getItemName() + " " + itemUpdate.getFields());
    }

    @Override
    public void onListenEnd(@Nonnull Subscription subscription) {
        System.out.println("onListenEnd");
    }

    @Override
    public void onListenStart(@Nonnull Subscription subscription) {
        System.out.println("onListenStart");
    }

    @Override
    public void onSubscription() {
        System.out.println("onSubscription");
    }

    @Override
    public void onSubscriptionError(int i, @Nullable String s) {
        System.out.println("onSubscriptionError " + s);
    }

    @Override
    public void onUnsubscription() {
        System.out.println("onUnsubscription");
    }

    @Override
    public void onRealMaxFrequency(@Nullable String s) {
        System.out.println("onRealMaxFrequency");
    }
}
