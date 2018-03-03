package com.example.demo;

import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StreamingHandler extends TextWebSocketHandler implements IPricingListener {

    private static final Logger log = LoggerFactory.getLogger(StreamingHandler.class);

    @Autowired
    private IPricingClient client;

    private Map<String, Set<WebSocketSession>> subscriptions = new HashMap<>();

    @PostConstruct
    public void init() {
        client.setListener(this);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        log.info("WS connection established {}", s.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        log.info("WS ({}) {}", s.getId(), m);

        // TODO handle unsubscribe
        // TODO parse symbol
        String symbol = m.getPayload();
        boolean isSubscribe = true;

        boolean doSubscribe = false;
        boolean doUnsubscribe = false;

        synchronized (subscriptions) {
            Set<WebSocketSession> sessions = subscriptions.get(symbol);

            if (isSubscribe) {
                if (sessions == null) {
                    sessions = new HashSet<>();
                    subscriptions.put(symbol, sessions);
                }

                sessions.add(s);

                if (sessions.size() == 1) {
                    doSubscribe = true;
                }
            } else {
                if (sessions != null) {
                    sessions.remove(s);
                    if (sessions.size() == 0) {
                        subscriptions.remove(symbol);
                        doUnsubscribe = true;
                    }
                }
            }
        }

        if (doSubscribe) {
            client.subscribe(symbol);
        }

        if (doUnsubscribe) {
            client.unsubscribe(symbol);
        }
    }

    @Override
    public void onData(String symbol, Map<String, String> d) {
        log.debug("SEND {} {}", symbol, d);

        Set<WebSocketSession> sessions = new HashSet<>();
        synchronized (subscriptions) {
            sessions.addAll(subscriptions.get(symbol));
        }

        sessions.forEach((s) -> {
            try {
                s.sendMessage(new TextMessage(symbol + "|" + d));
            } catch (IOException e) {
                log.error("Failed to send message", e);
            }
        });
    }

    @Override
    public void handleTransportError(WebSocketSession s, Throwable e) throws Exception {
        log.error("WS error (" + s.getId() + ")", e);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus st) throws Exception {
        log.info("WS connection closed {}, status {}", s.getId(), st);

        synchronized (subscriptions) {
            subscriptions.values().forEach(sessions -> {
                sessions.remove(s);
            });
        }
    }
}