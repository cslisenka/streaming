package com.example.demo.handler;

import com.example.demo.protocol.IProtocol;
import com.exchange.IPricingClient;
import com.exchange.IPricingListener;
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

public class StreamingHandler extends TextWebSocketHandler implements IPricingListener {

    private static final Logger log = LoggerFactory.getLogger(StreamingHandler.class);

    private IPricingClient client;
    private IProtocol proto;
    private Map<String, Set<WebSocketSession>> subscriptions = new HashMap<>();

    public StreamingHandler(IProtocol proto, IPricingClient client) {
        this.client = client;
        this.proto = proto;
        log.info("created");
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

        if (IProtocol.SUBSCRIBE.equals(command)) {
            subscribe(symbol, s);
        }

        if (IProtocol.UNSUBSCRIBE.equals(command)) {
            unsubscribe(symbol, s);
        }

        log.info(subscriptions.toString());
    }

    private void subscribe(String symbol, WebSocketSession session) {
        boolean doSusbcribe = false;
        synchronized (subscriptions) {
            Set<WebSocketSession> sessions = subscriptions.get(symbol);
            if (sessions == null) {
                sessions = new HashSet<>();
                subscriptions.put(symbol, sessions);
            }

            sessions.add(session);
            if (sessions.size() == 1) {
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
        synchronized (subscriptions) {
            Set<WebSocketSession> sessions = subscriptions.get(symbol);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.size() == 0) {
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

        Set<WebSocketSession> sessions = new HashSet<>();
        synchronized (subscriptions) {
            if (subscriptions.containsKey(symbol)) {
                sessions.addAll(subscriptions.get(symbol));
            }
        }

        sessions.forEach((s) -> {
            try {
                data.put(IProtocol.SYMBOL, symbol);
                s.sendMessage(new TextMessage(proto.toString(data)));
            } catch (IOException e) {
                log.error("Failed toString send message", e);
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

        try {
            Set<String> subscribedSymbols = new HashSet<>();
            synchronized (subscriptions) {
                subscriptions.forEach((symbol, sessions) -> {
                    if (sessions.contains(s)) {
                        subscribedSymbols.add(symbol);
                    }
                });
            }

            subscribedSymbols.forEach(symbol -> unsubscribe(symbol, s));
            log.info(subscriptions.toString());
        } catch (Exception e) {
            log.error("error", e);
        }
    }
}