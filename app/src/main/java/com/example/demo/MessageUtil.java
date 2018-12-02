package com.example.demo;

import com.google.gson.Gson;

import java.util.*;

public class MessageUtil {

    public static final double MAX_RATE = 20; // max updates per second

    public static final String SYMBOL = "symbol";
    public static final String SCHEMA = "schema";
    public static final String RATE = "rate";
    public static final String COMMAND = "command";

    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String ACK = "ack";
    public static final String LOGIN = "login";
    public static final String USER = "user";

    public static Map<String, Object> parseJson(String message) {
       Map<String, Object> result = new Gson().fromJson(message, HashMap.class);
       result.putIfAbsent(RATE, Double.MAX_VALUE);
       result.putIfAbsent(SCHEMA, new ArrayList<>());
       return result;
    }

    public static String toJson(Map<String, String> message) {
        Map<String, String> toSend = new HashMap<>();
        // Beautifying JSON
        message.forEach((k, v) -> {
            toSend.put(k.toLowerCase().replace("_", ""), v);
        });

        return new Gson().toJson(toSend);
    }

    public static Map<String, Object> parsePosition(String message) {
        Map<String, Object> result = new HashMap<>();

        // S - subscribe, U - unsubscribe | symbol | rate | schema
        // S|AAPL|1.5|bid,ask,bidsize,asksize
        // A - acknowledge | symbol
        // A|STOCK_EPAM
        // L - login | user
        // L|user1

        String[] parts = message.split("\\|");
        switch (parts[0]) {
            case "S":
                result.put(COMMAND, SUBSCRIBE);
                break;
            case "U":
                result.put(COMMAND, UNSUBSCRIBE);
                break;
            case "A":
                result.put(COMMAND, ACK);
                break;
            case "L":
                result.put(COMMAND, LOGIN);
                result.put(USER, parts[1]);
                break;
        }

        if (!LOGIN.equals(result.get(COMMAND))) {
            result.put(SYMBOL, parts[1]);

            if (parts.length > 2) {
                result.put(SCHEMA, Arrays.asList(parts[2].split(",")));
            } else {
                result.put(SCHEMA, new ArrayList<>());
            }

            if (parts.length > 3) {
                result.put(RATE, Double.parseDouble(parts[3]));
            } else {
                result.put(RATE, Double.MAX_VALUE);
            }
        }

        return result;
    }

    public static String toPositionBased(Map<String, String> message, List<String> schema) {
        StringBuilder result = new StringBuilder();
        schema.forEach(field -> {
            String value = message.get(field) != null ? message.get(field) : "";
            result.append(value).append("|");
        });

        return result.toString();
    }
}