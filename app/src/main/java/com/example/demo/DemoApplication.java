package com.example.demo;

import com.example.demo.handler.BandwidthAwareRateLimitHandler;
import com.example.demo.handler.BasicHandler;
import com.example.demo.handler.MaxFrequencyHandler;
import com.example.demo.handler.SchemaHandler;
import com.example.demo.protocol.JSONProtocol;
import com.exchange.IPricingClient;
import com.exchange.impl.RandomPriceGenerator;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;
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

    @Autowired
    private MaxFrequencyHandler maxFrequencyHandler;

    @Autowired
    private SchemaHandler schemaHandler;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
	    r.addHandler(new BasicHandler(client), "/ws/basic").setAllowedOrigins("*");
		r.addHandler(maxFrequencyHandler, "/ws/maxFrequency").setAllowedOrigins("*");
        r.addHandler(schemaHandler, "/ws/schema").setAllowedOrigins("*");
		r.addHandler(bandwidthAwareRateLimitHandler(), "/streaming/bandwidth").setAllowedOrigins("*");
    }

	@Bean(initMethod = "start")
    public BandwidthAwareRateLimitHandler bandwidthAwareRateLimitHandler() {
		BandwidthAwareRateLimitHandler handler = new BandwidthAwareRateLimitHandler(new JSONProtocol(), client);
		client.addListener(handler);
		return handler;
	}

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public SchemaHandler schemaHandler(@Autowired IPricingClient client) {
        return new SchemaHandler(client);
    }

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public MaxFrequencyHandler maxFrequencyHandler(@Autowired IPricingClient client) {
        return new MaxFrequencyHandler(client);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public IPricingClient client() {
		return new RandomPriceGenerator(10);
	}
}