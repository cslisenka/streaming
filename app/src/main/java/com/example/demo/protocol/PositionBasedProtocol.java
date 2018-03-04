package com.example.demo.protocol;

import java.util.HashMap;
import java.util.Map;

public class PositionBasedProtocol implements IProtocol {

    /**
     * @param request S|SYMBOL - subscribe, U|SYMBOL - unsubscribe
     */
    @Override
    public Map<String, String> fromString(String request) {
        Map<String, String> dto = new HashMap<>();
        if (request.startsWith("S|")) {
            dto.put(COMMAND, SUBSCRIBE);
        } else if (request.startsWith("U|")) {
            dto.put(COMMAND, UNSUBSCRIBE);
        }

        dto.put(SYMBOL, request.split("\\|")[1]);
        return dto;
    }

    /**
     * @param response SYMBOL|SEQ_NUM|TIMESTAMP|BID|ASK|BID_SIZE|ASK_SIZE|LAST
     */
    @Override
    public String toString(Map<String, String> response) {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s",
                response.get(SYMBOL),
                response.get("SEQ_NUM"),
                response.get("TIMESTAMP"),
                response.get("BID"),
                response.get("ASK"),
                response.get("BID_SIZE"),
                response.get("ASK_SIZE"),
                response.get("LAST"));
    }
}