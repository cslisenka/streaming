package com.example.demo;

import com.example.demo.handler.BandwidthAwareRateLimitHandler;
import com.example.demo.handler.RateLimitHandler;
import com.example.demo.handler.StreamingHandler;
import com.example.demo.protocol.JSONProtocol;
import com.example.demo.protocol.PositionBasedProtocol;
import com.exchange.IPricingClient;
import com.exchange.impl.RandomPriceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private IPricingClient client;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
        r.addHandler(streamingHandler(), "/streaming/json").setAllowedOrigins("*");
		r.addHandler(positionHandler(), "/streaming/position").setAllowedOrigins("*");
		r.addHandler(rateLimitHandler(), "/streaming/ratelimit").setAllowedOrigins("*");
		r.addHandler(bandwidthAwareRateLimitHandler(), "/streaming/bandwidth").setAllowedOrigins("*");
    }

	@Bean(initMethod = "start")
    public BandwidthAwareRateLimitHandler bandwidthAwareRateLimitHandler() {
		BandwidthAwareRateLimitHandler handler = new BandwidthAwareRateLimitHandler(new JSONProtocol(), client);
		client.addListener(handler);
		return handler;
	}

    @Bean(initMethod = "start")
    public RateLimitHandler rateLimitHandler() {
		RateLimitHandler handler = new RateLimitHandler(new JSONProtocol(), client);
		client.addListener(handler);
		return handler;
	}

    @Bean
    public StreamingHandler positionHandler() {
		StreamingHandler handler = new StreamingHandler(new PositionBasedProtocol(), client);
		client.addListener(handler);
		return handler;
	}

    @Bean
    public StreamingHandler streamingHandler() {
		StreamingHandler handler = new StreamingHandler(new JSONProtocol(), client);
		client.addListener(handler);
		return handler;
	}

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient client() {
		return new RandomPriceGenerator(10);
	}
}