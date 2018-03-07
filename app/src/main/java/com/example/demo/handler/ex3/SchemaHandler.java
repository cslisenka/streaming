package com.example.demo.handler.ex3;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@Component
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
        List<String> schema = new ArrayList<>(); // If empty means sending full data
    }

    private IPricingClient client;
    private Gson gson = new Gson();
    private Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    @Autowired
    public SchemaHandler(IPricingClient client) {
        this.client = client;
        client.addListener(this);
    }

    @PostConstruct
    public void start() {
        exec.scheduleAtFixedRate(() -> {
            subscriptions.forEach((symbol, sub) -> {
                Map<String, String> data = new HashMap<>();
                Map<WebSocketSession, SessionInfo> sessions = new HashMap<>();
                synchronized (sub) {
                    if (!sub.snapshot.isEmpty()) {
                        data.putAll(sub.snapshot);
                        sub.sessions.forEach((s, info) -> {
                            if (info.rm.tryAcquire()) {
                                sessions.put(s, info);
                            }
                        });
                    }
                }

                sessions.forEach((s, info) -> send(s, symbol, data, info));
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

            List<String> schema = request.containsKey(SCHEMA) ?
                    (List<String>) request.get(SCHEMA) : new ArrayList<>();

            subscribe(symbol, s, frequency, schema);
        }

        if (UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }
    }

    private void subscribe(String symbol, WebSocketSession s, double frequency, List<String> schema) {
        subscriptions.putIfAbsent(symbol, new SubscriptionInfo());

        SubscriptionInfo sub = subscriptions.get(symbol);
        synchronized (sub) {
            SessionInfo info = new SessionInfo();
            info.rm = RateLimiter.create(frequency);
            info.schema = schema;
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

    private void send(WebSocketSession s, String symbol, Map<String, String> data, SessionInfo info) {
        try {
            HashMap<String, String> toSend = new HashMap<>();
            toSend.put(SYMBOL, symbol);

            data.forEach((k, v) -> {
                // Beautifying JSON
                String key = k.toLowerCase().replace("_", "");

                boolean inSchema = info.schema == null || info.schema.contains(key);
                if (inSchema) {
                    toSend.put(key, v);
                }
            });

            log.info("{} {}", symbol, toSend);
            s.sendMessage(new TextMessage(gson.toJson(toSend)));
        } catch (Exception e) {
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

    @PreDestroy
    public void shutdown() {
        exec.shutdownNow();
    }
}