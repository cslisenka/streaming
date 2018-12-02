package com.example.demo.handler.ex6;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.demo.MessageUtil.*;

@SuppressWarnings("Duplicates")
@Component
public class BackpressureHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BackpressureHandler.class);

    private static final double MAX_ALLOWED_FREQUENCY = 100; // max updates per second
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    public static class SubscriptionInfo {
        Map<WebSocketSession, SessionInfo> sessions = new HashMap<>();
        Map<String, String> snapshot = new HashMap<>();
    }

    public static class SessionInfo {
        RateLimiter rate; // frequency limiter
        List<String> schema; // If empty means sending full data
        Map<String, String> dataSent = new HashMap<>();
        AtomicBoolean ack = new AtomicBoolean(true);
    }

    private RandomPriceGenerator gen;
    private Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();

    @Autowired
    public BackpressureHandler(RandomPriceGenerator gen) {
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
                                if (info.ack.getAndSet(false)) {
                                    send(s, symbol, new HashMap<>(sub.snapshot), info);
                                }
                            }
                        });
                    }
                }
            });
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        log.debug("Received ({}) {}", s.getId(), m.getPayload());

        Map<String, Object> request = parsePosition(m.getPayload());
        String symbol = request.get(SYMBOL).toString();
        String command = request.get(COMMAND).toString();

        if (SUBSCRIBE.equals(command)) {
            double frequency = (double) request.get(MAX_FREQUENCY);
            List<String> schema = (List<String>) request.get(SCHEMA);
            subscribe(symbol, s, frequency, schema);
        }

        if (UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }

        if (ACK.equals(command)) {
            SubscriptionInfo sub = subscriptions.get(symbol);
            synchronized (sub) {
                SessionInfo info = sub.sessions.get(s);
                info.ack.set(true);
            }
        }
    }

    private void subscribe(String symbol, WebSocketSession s, double frequency, List<String> schema) {
        subscriptions.putIfAbsent(symbol, new SubscriptionInfo());

        SubscriptionInfo sub = subscriptions.get(symbol);
        synchronized (sub) {
            SessionInfo info = new SessionInfo();
            info.rate = RateLimiter.create(frequency < MAX_ALLOWED_FREQUENCY ? frequency : MAX_ALLOWED_FREQUENCY);
            info.schema = schema;
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

    private void send(WebSocketSession s, String symbol, Map<String, String> data, SessionInfo info) {
        exec.submit(() -> {
            try {
                HashMap<String, String> toSend = new HashMap<>();
                toSend.put(SYMBOL, symbol);

                synchronized (info.dataSent) {
                    data.forEach((k, v) -> {
                        boolean inSchema = info.schema.contains(k);
                        boolean notSent = !v.equals(info.dataSent.get(k));

                        if (inSchema && notSent) {
                            toSend.put(k, v);
                        }
                    });

                    info.dataSent.putAll(toSend);
                }

                synchronized (s) {
                    s.sendMessage(new TextMessage(toPositionBased(toSend, info.schema)));
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