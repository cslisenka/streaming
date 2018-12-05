package com.example.demo.handler.ex5;

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

import static com.example.demo.MessageUtil.*;

@SuppressWarnings("Duplicates")
@Component
public class PositionProtocolHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PositionProtocolHandler.class);

    private Map<String, SubscriptionMetadata> subscriptions = new ConcurrentHashMap<>();

    public static class SubscriptionMetadata {
        Map<WebSocketSession, ConsumerMetadata> sessions = new HashMap<>();
        Map<String, String> snapshot = new HashMap<>();
    }

    public static class ConsumerMetadata {
        RateLimiter rate;
        List<String> schema; // If empty means sending full data
        Map<String, String> dataSent = new HashMap<>();
    }

    private RandomPriceGenerator gen;
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    @Autowired
    public PositionProtocolHandler(RandomPriceGenerator gen) {
        this.gen = gen;
        gen.addListener((symbol, data) -> {
            // This code executed each time when pricing update generated
            log.debug("SEND {} {}", symbol, data);

            SubscriptionMetadata sub = subscriptions.get(symbol);
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
                                send(s, symbol, new HashMap<>(sub.snapshot), info);
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

        Map<String, Object> request = parsePosition(m.getPayload());
        String symbol = request.get(SYMBOL).toString();
        String command = request.get(COMMAND).toString();

        if (SUBSCRIBE.equals(command)) {
            double rate = (double) request.get(RATE);
            List<String> schema = (List<String>) request.get(SCHEMA);
            subscribe(symbol, s, rate, schema);
        }

        if (UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }
    }

    private void subscribe(String symbol, WebSocketSession s, double rate, List<String> schema) {
        subscriptions.putIfAbsent(symbol, new SubscriptionMetadata());

        SubscriptionMetadata sub = subscriptions.get(symbol);
        synchronized (sub) {
            ConsumerMetadata info = new ConsumerMetadata();
            info.rate = RateLimiter.create(rate < MAX_RATE ? rate : MAX_RATE);
            info.schema = schema;
            sub.sessions.put(s, info);
            if (sub.sessions.size() == 1) {
                gen.start(symbol);
            }
        }
    }

    private void unsubscribe(String symbol, WebSocketSession s) {
        SubscriptionMetadata sub = subscriptions.get(symbol);
        if (sub != null) {
            synchronized (sub) {
                sub.sessions.remove(s);
                if (sub.sessions.size() == 0) {
                    subscriptions.remove(symbol);
                    gen.stop(symbol);
                }
            }
        }
    }

    private void send(WebSocketSession s, String symbol, Map<String, String> data, ConsumerMetadata info) {
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