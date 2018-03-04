package com.example.demo.protocol;

import java.util.Map;

public interface IProtocol {

    String SYMBOL = "symbol";
    String COMMAND = "command";
    String SUBSCRIBE = "subscribe";
    String UNSUBSCRIBE = "unsubscribe";

    Map<String, Object> fromString(String request);
    String toString(Map<String, String> response);
}