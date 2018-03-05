package com.example.demo.handler;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class SchemaHandler extends TextWebSocketHandler implements IPricingListener {

    private static final Logger log = LoggerFactory.getLogger(SchemaHandler.class);

    private static final String SYMBOL = "symbol";
    private static final String SCHEMA = "schema";
    private static final String MAX_FREQUENCY = "maxFrequency";
    private static final String COMMAND = "command";
    private static final String SUBSCRIBE = "subscribe";
    private static final String UNSUBSCRIBE = "unsubscribe";

    public static class SubscriptionInfo {
        Map<WebSocketSession, SessionInfo> sessions = new HashMap<>();
        Map<String, String> snapshot = new HashMap<>();
    }

    public static class SessionInfo {
        RateLimiter rm; // frequency limiter
        Set<String> schema = new HashSet<>(); // If empty means sending full data
    }

    private IPricingClient client;
    private Gson gson = new Gson();
    private Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    public SchemaHandler(IPricingClient client) {
        this.client = client;
        client.addListener(this);
    }

    public void start() {
        exec.scheduleAtFixedRate(() -> {
            subscriptions.forEach((symbol, sub) -> {
                Map<String, String> data = new HashMap<>();
                Map<WebSocketSession, SessionInfo> sessions = new HashMap<>();
                synchronized (sub) {
                    data.putAll(sub.snapshot);
                    sub.sessions.forEach((s, info) -> {
                        if (info.rm.tryAcquire()) {
                            sessions.put(s, info);
                        }
                    });
                }

                sessions.forEach((s, info) -> send(s, symbol, data, info.schema));
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        log.info("Received ({}) {}", s.getId(), m.getPayload());

        Map<String, Object> request = gson.fromJson(m.getPayload(), HashMap.class);
        String symbol = request.get(SYMBOL).toString();
        String command = request.get(COMMAND).toString();

        if (SUBSCRIBE.equals(command)) {
            double frequency = request.containsKey(MAX_FREQUENCY) ?
                    (double) request.get(MAX_FREQUENCY) : Double.MAX_VALUE;

            String[] schema = request.containsKey(SCHEMA) ?
                    request.get(SCHEMA).toString().split(",") : null;

            subscribe(symbol, s, frequency, schema);
        }

        if (UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }
    }

    private void subscribe(String symbol, WebSocketSession s, double frequency, String[] schema) {
        subscriptions.putIfAbsent(symbol, new SubscriptionInfo());

        SubscriptionInfo sub = subscriptions.get(symbol);
        synchronized (sub) {
            SessionInfo info = new SessionInfo();
            info.rm = RateLimiter.create(frequency);
            if (schema != null) {
                info.schema.addAll(Arrays.asList(schema));
            }
            sub.sessions.put(s, info);
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
            synchronized (sub) {
                sub.snapshot.putAll(data);
            }
        }
    }

    private void send(WebSocketSession s, String symbol, Map<String, String> data, Set<String> schema) {
        try {
            HashMap<String, String> toSend = new HashMap<>();
            toSend.put(SYMBOL, symbol);

            // Beautifying JSON
            data.forEach((k, v) -> toSend.put(k.toLowerCase().replace("_", ""), v));

            // Removing fields not defined in schema
            if (schema != null) {
                Set<String> notInSchema = new HashSet<>();
                toSend.keySet().forEach(field -> {
                    if (!schema.contains(field)) {
                        notInSchema.add(field);
                    }
                });

                notInSchema.forEach(field -> toSend.remove(field));
            }

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

    public void shutdown() {
        exec.shutdownNow();
    }
}