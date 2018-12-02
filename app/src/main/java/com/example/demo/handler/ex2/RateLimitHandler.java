package com.example.demo.handler.ex2;

import com.exchange.impl.RandomPriceGenerator;
import com.google.common.util.concurrent.RateLimiter;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.demo.MessageUtil.*;

@SuppressWarnings("Duplicates")
@Component
public class RateLimitHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(RateLimitHandler.class);

    private static final double MAX_ALLOWED_FREQUENCY = 20; // max updates per second
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    public static class SubscriptionInfo {
        Map<WebSocketSession, SessionInfo> sessions = new HashMap<>();
        Map<String, String> snapshot = new HashMap<>();
    }

    public static class SessionInfo {
        RateLimiter rate; // frequency limiter
    }

    private RandomPriceGenerator gen;
    private Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();

    @Autowired
    public RateLimitHandler(RandomPriceGenerator gen) {
        this.gen = gen;
        gen.addListener((symbol, data) -> {
            log.debug("SEND {} {}", symbol, data);
            toLowerCase(data);

            SubscriptionInfo sub = subscriptions.get(symbol);
            if (sub != null) {
                synchronized (sub) {
                    sub.snapshot.putAll(data);
                }
            }
        });
    }

    @PostConstruct
    public void start() {
        exec.scheduleAtFixedRate(() -> {
            subscriptions.forEach((symbol, sub) -> {
                synchronized (sub) {
                    if (!sub.snapshot.isEmpty()) {
                        sub.sessions.forEach((s, info) -> {
                            if (info.rate.tryAcquire()) {
                                send(s, symbol, new HashMap<>(sub.snapshot));
                            }
                        });
                    }
                }
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        log.info("Received ({}) {}", s.getId(), m.getPayload());

        Map<String, Object> request = parseJson(m.getPayload());
        String symbol = request.get(SYMBOL).toString();
        String command = request.get(COMMAND).toString();

        if (SUBSCRIBE.equals(command)) {
            double frequency = (double) request.get(MAX_FREQUENCY);
            subscribe(symbol, s, frequency);
        }

        if (UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }
    }

    private void subscribe(String symbol, WebSocketSession s, double frequency) {
        subscriptions.putIfAbsent(symbol, new SubscriptionInfo());

        SubscriptionInfo sub = subscriptions.get(symbol);
        synchronized (sub) {
            SessionInfo info = new SessionInfo();
            info.rate = RateLimiter.create(frequency < MAX_ALLOWED_FREQUENCY ? frequency : MAX_ALLOWED_FREQUENCY);
            sub.sessions.put(s, info);
            if (sub.sessions.size() == 1) {
                gen.subscribe(symbol);
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
                    gen.unsubscribe(symbol);
                }
            }
        }
    }

    private void send(WebSocketSession s, String symbol, Map<String, String> data) {
        exec.submit(() -> {
            try {
                data.put(SYMBOL, symbol);

                synchronized (s) {
                    s.sendMessage(new TextMessage(toJson(data)));
                }
            } catch (Exception e) {
                log.error("Failed to send data", e);
            }
        });
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