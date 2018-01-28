package com.example;

import java.util.Map;

public interface MarketDataListener {

    void onDataReceived(String symbol, Map<String, String> data);
}