package com.example.demo;

import com.example.demo.protocol.JSONProtocol;
import com.example.demo.protocol.PositionBasedProtocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@SpringBootApplication
public class DemoApplication implements WebSocketConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
        r.addHandler(new StreamingHandler(new JSONProtocol(), "com.example.demo.STREAMING"),
				"/streaming/json").setAllowedOrigins("*");

		r.addHandler(new StreamingHandler(new PositionBasedProtocol(), "com.example.demo.POSITION"),
				"/streaming/position").setAllowedOrigins("*");
    }
}