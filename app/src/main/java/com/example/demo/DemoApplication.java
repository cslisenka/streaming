package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalTime;

@Configuration
@EnableWebSocket
@SpringBootApplication
public class DemoApplication implements WebSocketConfigurer {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

//	@Autowired
//	private StreamingHandler streaming;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

//	@Bean
//	public IPricingClient client() {
//	    return new RandomPriceGenerator();
//    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
        r.addHandler(new MyHandler(), "/websocket");
//        r.addHandler(streaming, "/streaming");
    }

    static class MyHandler extends TextWebSocketHandler {

		@Override
		public void afterConnectionEstablished(WebSocketSession s) throws Exception {
            log.info("WS connection established {}", s.getId());
		}

		@Override
		protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
            log.info("WS ({}) {}", s.getId(), m);
            s.sendMessage(new TextMessage("responce " + LocalTime.now()));
		}

		@Override
		protected void handlePongMessage(WebSocketSession s, PongMessage m) throws Exception {
            log.info("WS PONG ({}) {}", s.getId(), m);
		}

		@Override
		public void handleTransportError(WebSocketSession s, Throwable e) throws Exception {
            log.error("WS error (" + s.getId() + ")", e);
		}

		@Override
		public void afterConnectionClosed(WebSocketSession s, CloseStatus st) throws Exception {
            log.info("WS connection closed {}, status {}", s.getId(), st);
		}
	}
}