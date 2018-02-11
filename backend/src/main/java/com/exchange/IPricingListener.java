package com.exchange;

import java.util.Map;

public interface IPricingListener {

    void onData(String symbol, Map<String, String> data);
}