package com.example.demo;

import com.example.demo.handler.BandwidthAwareRateLimitHandler;
import com.example.demo.handler.RateLimitHandler;
import com.example.demo.handler.StreamingHandler;
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
		StreamingHandler jsonHandler = new StreamingHandler(new JSONProtocol(), gen1());
        r.addHandler(jsonHandler, "/streaming/json").setAllowedOrigins("*");

		StreamingHandler positionHandler = new StreamingHandler(new PositionBasedProtocol(), gen2());
		r.addHandler(positionHandler, "/streaming/position").setAllowedOrigins("*");

		RateLimitHandler rlHandler = new RateLimitHandler(new JSONProtocol(), gen3());
		rlHandler.start();
		r.addHandler(rlHandler, "/streaming/ratelimit").setAllowedOrigins("*");

		BandwidthAwareRateLimitHandler rlAckHandler = new BandwidthAwareRateLimitHandler(new JSONProtocol(), gen4());
		rlAckHandler.start();
		r.addHandler(rlAckHandler, "/streaming/bandwidth").setAllowedOrigins("*");
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient gen1() {
		return new RandomPriceGenerator(10);
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public IPricingClient gen2() {
		return new RandomPriceGenerator(10);
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public IPricingClient gen3() {
		return new RandomPriceGenerator(10);
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public IPricingClient gen4() {
		return new RandomPriceGenerator(10);
	}
}