package com.exchange;

public interface IPricingClient {

    void subscribe(String symbol);
    void unsubscribe(String symbol);
    void setListener(IPricingListener listener);
    void start();
    void shutdown();
}