package com.example.demo.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeltaDeliveryProtocol implements IProtocol {

    private ConcurrentHashMap<String, Map<String, String>> snapshots = new ConcurrentHashMap<>();

    private final IProtocol transport;

    public DeltaDeliveryProtocol(IProtocol transport) {
        this.transport = transport;
    }

    @Override
    public Map<String, String> fromString(String request) {
        return transport.fromString(request);
    }

    @Override
    public String toString(Map<String, String> response) {


        return null;
    }

    // After we unsubscribe
    public void clear(String symbol) {
        snapshots.remove(symbol);
    }
}