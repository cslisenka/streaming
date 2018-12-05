package com.example.demo.handler.ex1;

import com.exchange.impl.RandomPriceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.demo.MessageUtil.*;

@SuppressWarnings("Duplicates")
@Component
public class BasicHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BasicHandler.class);

    private Map<String, SubscriptionMetadata> subscriptions = new ConcurrentHashMap<>();

    public static class SubscriptionMetadata {
        Map<WebSocketSession, ConsumerMetadata> sessions = new HashMap<>();
    }

    public static class ConsumerMetadata {
        // Nothing to store now
    }

    private RandomPriceGenerator gen;
    private ExecutorService exec = Executors.newCachedThreadPool();

    @Autowired
    public BasicHandler(RandomPriceGenerator gen) {
        this.gen = gen;
        gen.addListener((symbol, data) -> {
            // This code executed each time when pricing update generated
            log.debug("SEND {} {}", symbol, data);

            SubscriptionMetadata sub = subscriptions.get(symbol);
            if (sub != null) {
                synchronized (sub) {
                    sub.sessions.keySet().forEach(s -> send(s, symbol, data));
                }
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        // This code executed each time client sends message to server
        log.info("Received ({}) {}", s.getId(), m.getPayload());

        Map<String, Object> request = parseJson(m.getPayload());
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
        subscriptions.putIfAbsent(symbol, new SubscriptionMetadata());

        SubscriptionMetadata sub = subscriptions.get(symbol);
        synchronized (sub) {
            ConsumerMetadata info = new ConsumerMetadata();
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