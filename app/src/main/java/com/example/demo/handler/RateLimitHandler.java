package com.example.demo.handler;

import com.example.demo.protocol.IProtocol;
import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
import com.google.common.util.concurrent.RateLimiter;
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
import java.util.concurrent.*;

public class RateLimitHandler extends TextWebSocketHandler implements IPricingListener {

    private static final Logger log = LoggerFactory.getLogger(RateLimitHandler.class);

    private IPricingClient client;
    private IProtocol proto;
    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);

    static class Subscription {
        Map<WebSocketSession, RateLimiter> sessions = new HashMap<>();
        Map<String, String> snapshot = new HashMap<>();
    }

    public RateLimitHandler(IProtocol proto, IPricingClient client) {
        this.client = client;
        this.proto = proto;
        client.setListener(this);
        log.info("created");
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            subscriptions.forEach((symbol, sub) -> {
                Set<WebSocketSession> sessions = new HashSet<>();
                Map<String, String> data = new HashMap<>();

                synchronized (sub) {
                    sub.sessions.forEach(((session, rateLimiter) -> {
                        if (rateLimiter.tryAcquire()) {
                            sessions.addAll(sub.sessions.keySet());
                            data.put(IProtocol.SYMBOL, symbol);
                            data.putAll(sub.snapshot);
                        }
                    }));
                }

                sessions.forEach((s) -> {
                    try {
                        s.sendMessage(new TextMessage(proto.toString(data)));
                    } catch (IOException e) {
                        log.error("Failed toString send message", e);
                    }
                });
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        log.info("WS connection established {}", s.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        log.info("WS ({}) {}", s.getId(), m.getPayload());

        Map<String, Object> request = proto.fromString(m.getPayload());
        String symbol = request.get(IProtocol.SYMBOL).toString();
        String command = request.get(IProtocol.COMMAND).toString();
        double frequency = Double.MAX_VALUE;
        if (request.containsKey("maxFrequency")) {
            frequency = (Double) request.get("maxFrequency");
        }

        if (IProtocol.SUBSCRIBE.equals(command)) {
            subscribe(symbol, s, frequency);
        }

        if (IProtocol.UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }
    }

    private void subscribe(String symbol, WebSocketSession session, double frequency) {
        Subscription newSub = new Subscription();
        Subscription sub = subscriptions.putIfAbsent(symbol, newSub);
        if (sub == null) {
            sub = newSub;
        }

        boolean doSusbcribe = false;
        synchronized (sub) {
            sub.sessions.put(session, RateLimiter.create(frequency));
            if (sub.sessions.size() == 1) {
                doSusbcribe = true;
            }
        }

        if (doSusbcribe) {
            log.info("subscribing {}", symbol);
            client.subscribe(symbol);
        }
    }

    private void unsubscribe(String symbol, WebSocketSession session) {
        boolean doUnsubscribe = false;

        Subscription sub = subscriptions.get(symbol);
        if (sub != null) {
            synchronized (sub) {
                sub.sessions.remove(session);
                if (sub.sessions.size() == 0) {
                    subscriptions.remove(symbol);
                    doUnsubscribe = true;
                }
            }
        }

        if (doUnsubscribe) {
            log.info("unsubscribing {}", symbol);
            client.unsubscribe(symbol);
        }
    }

    @Override
    public void onData(String symbol, Map<String, String> data) {
        log.debug("SEND {} {}", symbol, data);

        // Instead of sending data back to client, we storing it into snapshot
        Subscription sub = subscriptions.get(symbol);
        if (sub != null) {
            synchronized (sub) {
                // Aggregation functions may be applied here
                sub.snapshot.putAll(data);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession s, Throwable e) throws Exception {
        log.error("WS error (" + s.getId() + ")", e);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus st) throws Exception {
        log.info("WS connection closed {}, status {}", s.getId(), st);

        try {
            subscriptions.forEach((symbol, sub) -> unsubscribe(symbol, s));
        } catch (Exception e) {
            log.error("error", e);
        }
    }
}