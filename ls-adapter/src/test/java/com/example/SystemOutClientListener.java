package com.example;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;

import javax.annotation.Nonnull;

public class SystemOutClientListener implements ClientListener {

    @Override
    public void onListenEnd(@Nonnull LightstreamerClient lightstreamerClient) {
        System.out.println("onListenEnd" + lightstreamerClient);
    }

    @Override
    public void onListenStart(@Nonnull LightstreamerClient lightstreamerClient) {
        System.out.println("onListenStart" + lightstreamerClient);
    }

    @Override
    public void onServerError(int i, @Nonnull String s) {
        System.out.println("onServerError " + s);
    }

    @Override
    public void onStatusChange(@Nonnull String s) {
        System.out.println("onStatusChange " + s);
    }

    @Override
    public void onPropertyChange(@Nonnull String s) {
        System.out.println("onPropertyChange " + s);
    }
}
