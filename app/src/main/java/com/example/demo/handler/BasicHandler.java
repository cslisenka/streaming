package com.example.demo.handler;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BasicHandler extends TextWebSocketHandler implements IPricingListener {

    private static final Logger log = LoggerFactory.getLogger(BasicHandler.class);

    private static final String SYMBOL = "symbol";
    private static final String COMMAND = "command";
    private static final String SUBSCRIBE = "subscribe";
    private static final String UNSUBSCRIBE = "unsubscribe";

    public static class SubscriptionInfo {
        Map<WebSocketSession, SessionInfo> sessions = new HashMap<>();
    }

    public static class SessionInfo {
        // Nothing to store now
    }

    private final IPricingClient client;
    private final Gson gson = new Gson();
    private final Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();

    public BasicHandler(IPricingClient client) {
        this.client = client;
        client.addListener(this);
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        log.info("Received ({}) {}", s.getId(), m.getPayload());

        Map<String, Object> request = gson.fromJson(m.getPayload(), HashMap.class);
        String symbol = request.get(SYMBOL).toString();
        String command = request.get(COMMAND).toString();

        if (SUBSCRIBE.equals(command)) {
            subscribe(symbol, s);
        }

        if (UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }
    }

    private void subscribe(String symbol, WebSocketSession s) {
        subscriptions.putIfAbsent(symbol, new SubscriptionInfo());

        SubscriptionInfo sub = subscriptions.get(symbol);
        synchronized (sub) {
            sub.sessions.put(s, new SessionInfo());
            if (sub.sessions.size() == 1) {
                log.info("subscribing {}", symbol);
                client.subscribe(symbol);
            }
        }
    }

    private void unsubscribe(String symbol, WebSocketSession s) {
        SubscriptionInfo sub = subscriptions.get(symbol);
        if (sub != null) {
            synchronized (sub) {
                sub.sessions.remove(s);
                if (sub.sessions.size() == 0) {
                    subscriptions.remove(symbol);
                    log.info("unsubscribing {}", symbol);
                    client.unsubscribe(symbol);
                }
            }
        }
    }

    @Override
    public void onData(String symbol, Map<String, String> data) {
        log.debug("SEND {} {}", symbol, data);

        SubscriptionInfo sub = subscriptions.get(symbol);
        if (sub != null) {
            Set<WebSocketSession> sessions = new HashSet<>();
            synchronized (sub) {
                sessions.addAll(sub.sessions.keySet());
            }

            sessions.forEach((s) -> {
                send(s, symbol, data);
            });
        }
    }

    private void send(WebSocketSession s, String symbol, Map<String, String> data) {
        try {
            HashMap<String, String> toSend = new HashMap<>();
            toSend.put(SYMBOL, symbol);
            // Beautifying JSON
            data.forEach((k, v) -> toSend.put(k.toLowerCase().replace("_", ""), v));
            s.sendMessage(new TextMessage(gson.toJson(toSend)));
        } catch (IOException e) {
            log.error("Failed to send data", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus st) throws Exception {
        log.info("WS connection closed {}, status {}", s.getId(), st);
        subscriptions.keySet().forEach(symbol -> unsubscribe(symbol, s));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        log.info("WS connection established {}", s.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession s, Throwable e) throws Exception {
        log.error("WS error (" + s.getId() + ")", e);
    }
}