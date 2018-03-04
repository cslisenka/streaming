package com.example.demo;

import com.example.demo.protocol.JSONProtocol;
import com.example.demo.protocol.PositionBasedProtocol;
import com.exchange.IPricingClient;
import com.exchange.impl.RandomPriceGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
        r.addHandler(
			new StreamingHandler(new JSONProtocol(), gen1(), "com.example.demo.STREAMING"),
	"/streaming/json").setAllowedOrigins("*");

		r.addHandler(new StreamingHandler(new PositionBasedProtocol(), gen2(), "com.example.demo.POSITION"),
				"/streaming/position").setAllowedOrigins("*");
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient gen1() {
		return new RandomPriceGenerator(10);
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public IPricingClient gen2() {
		return new RandomPriceGenerator(10);
	}
}